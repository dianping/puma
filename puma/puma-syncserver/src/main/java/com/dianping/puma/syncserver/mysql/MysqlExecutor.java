package com.dianping.puma.syncserver.mysql;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.sync.model.mapping.ColumnMapping;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;
import com.dianping.puma.syncserver.util.SyncConfigPatternParser;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class MysqlExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(MysqlExecutor.class);

    private MysqlMapping mysqlMapping;

    public static final int INSERT = RowChangedEvent.INSERT;
    public static final int DELETE = RowChangedEvent.DELETE;
    public static final int UPDATE = RowChangedEvent.UPDATE;
    public static final int REPLACE_INTO = 3;//插入，如果已存在则更新
    private static final int UPDTAE_TO_NULL = 4;//将对应的列都设置为null
    public static final int SELECT = 5;//查询

    private ComboPooledDataSource dataSource;
    private Connection conn = null;

    public MysqlExecutor(String host, String username, String password) {
        try {
            dataSource = new ComboPooledDataSource();
            dataSource.setJdbcUrl("jdbc:mysql://" + host + "/");
            dataSource.setUser(username);
            dataSource.setPassword(password);
            dataSource.setDriverClass("com.mysql.jdbc.Driver");
            dataSource.setMinPoolSize(1);
            dataSource.setMaxPoolSize(1);
            dataSource.setInitialPoolSize(1);
            dataSource.setMaxIdleTime(300);
            dataSource.setIdleConnectionTestPeriod(60);
            dataSource.setAcquireRetryAttempts(3);
            dataSource.setAcquireRetryDelay(300);
            dataSource.setMaxStatements(0);
            dataSource.setMaxStatementsPerConnection(100);
            dataSource.setNumHelperThreads(6);
            dataSource.setMaxAdministrativeTaskTime(5);
            dataSource.setPreferredTestQuery("SELECT 1");
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 执行event(如果event是查询，则根据event中的主键的newValue，进行查询，返回ResultSet，其他update/insert/delete情况返回null)
     */
    public ResultSet execute(ChangedEvent event) throws SQLException {
        if (event instanceof DdlEvent) {
            String sql = ((DdlEvent) event).getSql();
            if (StringUtils.isNotBlank(sql)) {
                //ddl不做命名的替换！直接执行
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[Not Execute]execute ddl sql: " + sql);
                }
                //ddl不做执行
                //jdbcTemplate.update(sql);
            }
        } else if (event instanceof RowChangedEvent) {
            return _execute((RowChangedEvent) event);
        }
        return null;
    }

    public void commit() throws SQLException {
        if (conn != null) {
            conn.commit();
            try {
                conn.close();//释放连接到连接池
            } catch (Exception e) {
                //ignore
            }
            conn = null;
        }
    }

    public void rollback() throws SQLException {
        if (conn != null) {
            conn.rollback();
            try {
                conn.close();//释放连接到连接池
            } catch (Exception e) {
                //ignore
            }
            conn = null;
        }
    }

    private ResultSet _execute(RowChangedEvent rowChangedEvent) throws SQLException {
        ResultSet re = null;
        MysqlStatement mus = convertStatement(rowChangedEvent);
        if (LOG.isDebugEnabled()) {
            LOG.debug("execute dml sql statement: " + mus);
        }
        PreparedStatement ps = null;
        try {
            if (conn == null) {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
            }
            ps = conn.prepareStatement(mus.getSql());
            for (int i = 1; i <= mus.getArgs().length; i++) {
                ps.setObject(i, mus.getArgs()[i - 1]);
            }
            ps.execute();
            if (rowChangedEvent.getActionType() == SELECT) {
                re = ps.getResultSet();
            }
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return re;
    }

    /**
     * 将RowChangedEvent转化成MysqlStatement(Mysql操作对象)
     */
    private MysqlStatement convertStatement(RowChangedEvent rowChangedEvent) {
        MysqlStatement mus = new MysqlStatement();
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
            case SELECT:
                sql = SqlBuildUtil.buildSelectSql(event);
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
            case SELECT:
                for (Map.Entry<String, ColumnInfo> columnName2ColumnInfo : columnMap.entrySet()) {
                    if (columnName2ColumnInfo.getValue().isKey()) {//select 语句只使用关键字作where条件
                        args.add(columnName2ColumnInfo.getValue().getOldValue());
                    }
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

}
