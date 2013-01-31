package com.dianping.puma.syncserver.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.sync.model.mapping.ColumnMapping;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;
import com.dianping.puma.syncserver.util.SyncConfigPatternParser;

public class MysqlExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(MysqlExecutor.class);

    private MysqlMapping mysqlMapping;

    public static final int INSERT = 0;
    public static final int DELETE = 1;
    public static final int UPDATE = 2;
    public static final int REPLACE_INTO = 3;//插入，如果已存在则更新
    private static final int UPDTAE_TO_NULL = 4;//将对应的列都设置为null

    private final JdbcTemplate jdbcTemplate;
    private final SingleConnectionDataSource dataSource;

    public MysqlExecutor(String host, String username, String password) {
        dataSource = new SingleConnectionDataSource("jdbc:mysql://" + host + "/", username, password, true);
        dataSource.setAutoCommit(false);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void execute(ChangedEvent event) {
        LOG.info("********************Received " + event);
        if (event instanceof DdlEvent) {
            String sql = ((DdlEvent) event).getSql();
            if (StringUtils.isNotBlank(sql)) {
                //ddl不做命名的替换！直接执行
                LOG.info("[Not Execute]execute ddl sql: " + sql);
                //ddl不做执行
                //                jdbcTemplate.update(sql);
            }
        } else if (event instanceof RowChangedEvent) {
            _execute(mysqlMapping, (RowChangedEvent) event);
        }
    }

    public void commit() throws SQLException {
        dataSource.getConnection().commit();
    }

    private void _execute(MysqlMapping mysqlMapping, RowChangedEvent rowChangedEvent) {
        MysqlUpdateStatement mus = convert(mysqlMapping, rowChangedEvent);
        LOG.info("execute dml sql statement: " + mus);
        jdbcTemplate.update(mus.getSql(), mus.getArgs());
    }

    /**
     * 将RowChangedEvent转化成MysqlUpdateStatement(Mysql操作对象)
     */
    private MysqlUpdateStatement convert(MysqlMapping mysqlMapping, RowChangedEvent rowChangedEvent) {
        MysqlUpdateStatement mus = new MysqlUpdateStatement();
        RowChangedEvent event = rowChangedEvent.clone();

        //获取来源的database,column
        String databaseName = rowChangedEvent.getDatabase();
        String tableName = rowChangedEvent.getTable();
        Map<String, ColumnInfo> columnMap = rowChangedEvent.getColumns();
        int srcActionType = rowChangedEvent.getActionType();//actionType
        //2.根据"来源actionType,database,table,column"和Sync，得到dest的actionType,database,table,column
        List<DatabaseMapping> databases = mysqlMapping.getDatabases();
        DatabaseMapping database = findDatabaseMapping(databases, databaseName);
        if (database.getTo().equals("*")) {//如果是database匹配*
            //event就是rowChangedEvent;
        } else {//如果是database不匹配*
            event.setDatabase(database.getTo());
            List<TableMapping> tables = database.getTables();
            TableMapping table = findTableConfig(tables, tableName);
            if (table.getTo().equals("*")) {//如果是table匹配*
                //如果是*，则和原来的一致
            } else {
                if (table.getTo().startsWith("#partition")) {//如果是自定义#partition，则计算出table名称
                    //根据自定义规则(如分表)，算出table名称
                    event.setTable(SyncConfigPatternParser.partition(table.getTo(), rowChangedEvent.getColumns()));
                } else {
                    event.setTable(table.getTo());//如果不是*也不是#partition，则destTableName为to的值
                }
                //处理column
                List<ColumnMapping> columnConfigs = table.getColumns();
                for (Map.Entry<String, ColumnInfo> columnEntry : columnMap.entrySet()) {
                    String srcColumnName = columnEntry.getKey();
                    ColumnInfo srcColumn = columnEntry.getValue();
                    ColumnMapping columnConfig = findColumnMapping(columnConfigs, srcColumnName);
                    String destColumnName = columnConfig.getTo();
                    //替换event的column
                    event.getColumns().remove(srcColumnName);
                    event.getColumns().put(destColumnName, srcColumn);
                }
                //如果partOf为true，则修改actionType
                if (table.isPartOf()) {
                    switch (srcActionType) {
                        case INSERT:
                            event.setActionType(REPLACE_INTO);
                            break;
                        case UPDATE:
                            event.setActionType(REPLACE_INTO);
                            break;
                        case DELETE:
                            event.setActionType(UPDTAE_TO_NULL);
                            break;
                    }
                }
            }
        }
        String sql = parseSql(event);
        //构造args
        Object[] args = parseArgs(event);
        mus.setSql(sql);
        mus.setArgs(args);
        return mus;

    }

    private String parseSql(RowChangedEvent event) {
        String sql = null;
        int actionType = event.getActionType();
        switch (actionType) {
            case INSERT:
                sql = SqlBuildUtil.buildInsertSql(event);
                break;
            case UPDATE:
                sql = SqlBuildUtil.buildUpdateSql(event);
                break;
            case DELETE:
                sql = SqlBuildUtil.buildDeleteSql(event);
                break;
            case UPDTAE_TO_NULL:
                sql = SqlBuildUtil.buildUpdateToNullSql(event);
                break;
            case REPLACE_INTO:
                sql = SqlBuildUtil.buildReplaceSql(event);
                break;
        }
        return sql;
    }

    private Object[] parseArgs(RowChangedEvent event) {
        int actionType = event.getActionType();
        Map<String, ColumnInfo> columnMap = event.getColumns();
        List<Object> args = new ArrayList<Object>();
        switch (actionType) {
            case REPLACE_INTO:
                for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                    args.add(columnName2ColumnInfo.getValue().getNewValue());
                }
                for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                    if (!columnName2ColumnInfo.getValue().isKey()) {
                        args.add(columnName2ColumnInfo.getValue().getNewValue());
                    }
                }
                break;
            case INSERT:
                for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                    args.add(columnName2ColumnInfo.getValue().getNewValue());
                }
                break;
            case UPDATE:
                for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                    args.add(columnName2ColumnInfo.getValue().getNewValue());
                }
                for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                    args.add(columnName2ColumnInfo.getValue().getOldValue());
                }
                break;
            case DELETE:
                for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                    args.add(columnName2ColumnInfo.getValue().getOldValue());
                }
                break;
            case UPDTAE_TO_NULL:
                for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                    if (!columnName2ColumnInfo.getValue().isKey()) { //primery key 不能update to null
                        args.add(columnName2ColumnInfo.getValue().getNewValue());
                    }
                }
                for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                    args.add(columnName2ColumnInfo.getValue().getOldValue());
                }
                break;
        }
        return args.toArray();
    }

    /**
     * 从columnConfigs中找出from为srcColumnName的ColumnMapping <br>
     * (如果找不到，而有from=*，则返回to(to也等于*)，否则返回null)
     */
    private ColumnMapping findColumnMapping(List<ColumnMapping> columnConfigs, String srcColumnName) {
        for (ColumnMapping columnConfig : columnConfigs) {
            if (StringUtils.equals(srcColumnName, columnConfig.getFrom())) {
                return columnConfig;
            } else if (StringUtils.equals("*", columnConfig.getFrom())) {
                ColumnMapping c = new ColumnMapping();
                c.setFrom(srcColumnName);
                c.setTo(srcColumnName);
                return c;
            }
        }
        return null;
    }

    /**
     * 从tableConfigs中找出from为srcTableName的TableConfig <br>
     * (如果找不到，而有from=*，则返回to(to也等于*)，否则返回null)
     */
    private TableMapping findTableConfig(List<TableMapping> tables, String srcTableName) {
        for (TableMapping tableConfig : tables) {
            if (StringUtils.equals(srcTableName, tableConfig.getFrom()) || StringUtils.equals("*", tableConfig.getFrom())) {
                return tableConfig;
            }
        }
        return null;
    }

    /**
     * 从databaseConfigs中找出from为srcDatabaseName的DatabaseMapping<br>
     * (如果找不到，而有from=*，则返回to(to也等于*)，否则返回null)
     */
    private DatabaseMapping findDatabaseMapping(List<DatabaseMapping> databases, String srcDatabaseName) {
        for (DatabaseMapping databaseConfig : databases) {
            if (StringUtils.equals(srcDatabaseName, databaseConfig.getFrom()) || StringUtils.equals("*", databaseConfig.getFrom())) {
                return databaseConfig;
            }
        }
        return null;
    }

    public void setMysqlMapping(MysqlMapping mysqlMapping) {
        this.mysqlMapping = mysqlMapping;
    }

    public static void main(String[] args44) {
        MysqlExecutor mysqlExecutor = new MysqlExecutor("localhost:3306", "root", "root");
        String sql = "INSERT INTO `test`.`test4` VALUES (?,?,?,?)";
        int id = 35685;
        for (int j = 0; j < 1000; j++) {
            List<Object[]> argslist = new ArrayList<Object[]>();
            for (int i = 0; i < 1000; i++) {
                Object[] args = new Object[] { Integer.valueOf(id), "name1", "name2", "desc" };
                argslist.add(args);
                id++;
            }
            mysqlExecutor.jdbcTemplate.batchUpdate(sql, argslist);
        }

    }

}
