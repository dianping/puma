package com.dianping.puma.syncserver.mysql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.sync.ColumnConfig;
import com.dianping.puma.core.sync.DatabaseConfig;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.core.sync.TableConfig;
import com.dianping.puma.syncserver.util.SyncConfigPatternParser;

public class MysqlExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(MysqlExecutor.class);

    private SyncConfig syncConfig;

    public static final int INSERT = 0;
    public static final int DELETE = 1;
    public static final int UPDATE = 2;
    public static final int REPLACE_INTO = 3;//插入，如果已存在则更新
    private static final int UPDTAE_TO_NULL = 4;//将对应的列都设置为null

    private final JdbcTemplate jdbcTemplate;

    public MysqlExecutor(String url, String username, String password) {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource(url, username, password, true);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void execute(ChangedEvent event) {
        LOG.info("********************Received " + event);
        if (event instanceof DdlEvent) {
            String sql = ((DdlEvent) event).getSql();
            if (StringUtils.isNotBlank(sql)) {
                //ddl不做命名的替换！直接执行
                //TODO ddl 命名替换
                LOG.info("execute ddl sql: " + sql);
                jdbcTemplate.update(sql);
            }
        } else if (event instanceof RowChangedEvent) {
            _execute(syncConfig, (RowChangedEvent) event);
        }
    }

    private void _execute(SyncConfig syncConfig2, RowChangedEvent rowChangedEvent) {
        MysqlUpdateStatement mus = convert(syncConfig2, rowChangedEvent);
        LOG.info("execute dml sql statement: " + mus);
        jdbcTemplate.update(mus.getSql(), mus.getArgs());
    }

    /**
     * 将RowChangedEvent转化成MysqlUpdateStatement(Mysql操作对象)
     */
    private MysqlUpdateStatement convert(SyncConfig sync, RowChangedEvent rowChangedEvent) {
        MysqlUpdateStatement mus = new MysqlUpdateStatement();
        RowChangedEvent event = rowChangedEvent.clone();

        //获取来源的database,column
        String databaseName = rowChangedEvent.getDatabase();
        String tableName = rowChangedEvent.getTable();
        Map<String, ColumnInfo> columnMap = rowChangedEvent.getColumns();
        int srcActionType = rowChangedEvent.getActionType();//actionType
        //2.根据"来源actionType,database,table,column"和Sync，得到dest的actionType,database,table,column
        List<DatabaseConfig> databases = sync.getInstance().getDatabases();
        DatabaseConfig database = findDatabaseConfig(databases, databaseName);
        if (database.getTo().equals("*")) {//如果是database匹配*
            //event就是rowChangedEvent;
        } else {//如果是database不匹配*
            event.setDatabase(database.getTo());
            List<TableConfig> tables = database.getTables();
            TableConfig table = findTableConfig(tables, tableName);
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
                List<ColumnConfig> columnConfigs = table.getColumns();
                for (Map.Entry<String, ColumnInfo> columnEntry : columnMap.entrySet()) {
                    String srcColumnName = columnEntry.getKey();
                    ColumnInfo srcColumn = columnEntry.getValue();
                    ColumnConfig columnConfig = findColumnConfig(columnConfigs, srcColumnName);
                    String destColumnName = columnConfig.getTo();
                    //替换event的column
                    event.getColumns().remove(srcColumnName);
                    event.getColumns().put(destColumnName, srcColumn);
                }
                //如果partOf为true，则修改actionType
                if (table.getPartOf() != null && table.getPartOf().booleanValue()) {
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
     * 从columnConfigs中找出from为srcColumnName的ColumnConfig <br>
     * (如果找不到，而有from=*，则返回to(to也等于*)，否则返回null)
     */
    private ColumnConfig findColumnConfig(List<ColumnConfig> columnConfigs, String srcColumnName) {
        for (ColumnConfig columnConfig : columnConfigs) {
            if (StringUtils.equals(srcColumnName, columnConfig.getFrom()) || StringUtils.equals("*", columnConfig.getFrom())) {
                return columnConfig;
            }
        }
        return null;
    }

    /**
     * 从tableConfigs中找出from为srcTableName的TableConfig <br>
     * (如果找不到，而有from=*，则返回to(to也等于*)，否则返回null)
     */
    private TableConfig findTableConfig(List<TableConfig> tables, String srcTableName) {
        for (TableConfig tableConfig : tables) {
            if (StringUtils.equals(srcTableName, tableConfig.getFrom()) || StringUtils.equals("*", tableConfig.getFrom())) {
                return tableConfig;
            }
        }
        return null;
    }

    /**
     * 从databaseConfigs中找出from为srcDatabaseName的DatabaseConfig<br>
     * (如果找不到，而有from=*，则返回to(to也等于*)，否则返回null)
     */
    private DatabaseConfig findDatabaseConfig(List<DatabaseConfig> databases, String srcDatabaseName) {
        for (DatabaseConfig databaseConfig : databases) {
            if (StringUtils.equals(srcDatabaseName, databaseConfig.getFrom()) || StringUtils.equals("*", databaseConfig.getFrom())) {
                return databaseConfig;
            }
        }
        return null;
    }

    public void setSync(SyncConfig sync) {
        this.syncConfig = sync;
    }

    public static void main(String[] args44) {
        MysqlExecutor mysqlExecutor = new MysqlExecutor(
                "jdbc:mysql://localhost:3306/test?useUnicode=true&amp;characterEncoding=utf8&amp;zeroDateTimeBehavior=convertToNull&amp;noAccessToProcedureBodies=true",
                "root", "root");
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
