package com.dianping.puma.syncserver.mysql;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.dianping.puma.core.sync.ColumnConfig;
import com.dianping.puma.core.sync.DatabaseConfig;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.core.sync.TableConfig;

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
        System.out.println("********************Received " + event);
        if (event instanceof DdlEvent) {
            //获取event的database和table

            //从Sync找出“database和table”
            //所有ddl的sql有哪些？

            //获取ddl的sql
            String sql = ((DdlEvent) event).getSql();
            System.out.println(sql);
            //处理database和table的名称
            //            sql.replaceAll("pumatest", "test");
            //执行sql
            //            jdbcTemplate.execute(sql);
        } else if (event instanceof RowChangedEvent) {
            _execute2(syncConfig, (RowChangedEvent) event);
        }
    }

    /**
     * 根据Sync和RowChangeEvent构造出sql和args
     * 
     * @param sync
     * @param rowChangedEvent
     * @return
     */
    private void _execute(SyncConfig sync, RowChangedEvent rowChangedEvent) {
        //1.获取来源
        //来源的database,column
        String databaseName = rowChangedEvent.getDatabase();
        //来源的table
        String tableName = rowChangedEvent.getTable();
        //来源的column
        Map<String, ColumnInfo> columnMap = rowChangedEvent.getColumns();
        //来源的actionType
        int srcActionType = rowChangedEvent.getActionType();//actionType
        //2.根据"来源actionType,database,table,column"和Sync，得到dest的actionType,database,table,column
        //dest的databse
        String destDatabaseName = null;
        //dest的table
        String destTableName = null;
        //dest的column
        List<String> destColumnNames = new ArrayList<String>();//insert,update涉及的所有列名
        List<Object> destColumnValues = new ArrayList<Object>();//insert,update涉及的所有列值
        List<String> destWhereColumnNames = new ArrayList<String>();//delete,update涉及的所有列名
        List<Object> destWhereColumnValues = new ArrayList<Object>();//delete,update涉及的Where所有列值(where的?将重复出现2次，见sql模板)
        //dest的actionType
        int destActionType = -1;
        //计算过程
        List<DatabaseConfig> databases = sync.getInstance().getDatabases();
        DatabaseConfig database = findDatabaseConfig(databases, databaseName);
        if (database.getTo().equals("*")) {//如果是database匹配*
            destDatabaseName = databaseName;
            destTableName = tableName;
            //column也和src一模一样
            for (Map.Entry<String, ColumnInfo> columnEntry : columnMap.entrySet()) {
                String srcColumnName = columnEntry.getKey();
                ColumnInfo srcColumn = columnEntry.getValue();
                //update,insert的列
                destColumnNames.add(srcColumnName);
                destColumnValues.add(srcColumn.getNewValue());
                //delete,update的where列
                destWhereColumnNames.add(srcColumnName);
                destWhereColumnValues.add(srcColumn.getOldValue());
                destWhereColumnValues.add(srcColumn.getOldValue());
            }
            destActionType = srcActionType;
        } else {//如果是database不匹配*
            destDatabaseName = database.getTo();
            List<TableConfig> tables = database.getTables();
            TableConfig table = findTableConfig(tables, tableName);
            if (table.getTo().equals("*")) {//如果是table匹配*
                destTableName = tableName;//如果是*，则和原来的tableName一致
                //column也和src一模一样
                for (Map.Entry<String, ColumnInfo> columnEntry : columnMap.entrySet()) {
                    String srcColumnName = columnEntry.getKey();
                    ColumnInfo srcColumn = columnEntry.getValue();
                    //update,insert的列
                    destColumnNames.add(srcColumnName);
                    destColumnValues.add(srcColumn.getNewValue());
                    //delete,update的where列
                    destWhereColumnNames.add(srcColumnName);
                    destWhereColumnValues.add(srcColumn.getOldValue());
                    destWhereColumnValues.add(srcColumn.getOldValue());
                }
                destActionType = srcActionType;
            } else {
                destTableName = table.getTo();//如果不是*，则destTableName为to的值
                //处理column
                List<ColumnConfig> columnConfigs = table.getColumns();
                for (Map.Entry<String, ColumnInfo> columnEntry : columnMap.entrySet()) {
                    String srcColumnName = columnEntry.getKey();
                    ColumnInfo srcColumn = columnEntry.getValue();
                    ColumnConfig columnConfig = findColumnConfig(columnConfigs, srcColumnName);
                    String destColumnName = columnConfig.getTo();
                    //update,insert的列
                    destColumnNames.add(destColumnName);
                    destColumnValues.add(srcColumn.getNewValue());
                    //delete,update的where列
                    destWhereColumnNames.add(destColumnName);
                    destWhereColumnValues.add(srcColumn.getOldValue());
                    destWhereColumnValues.add(srcColumn.getOldValue());
                }
                //处理destActionType
                if (table.getPartOf() != null && table.getPartOf().booleanValue()) {
                    switch (srcActionType) {
                        case INSERT:
                            destActionType = REPLACE_INTO;
                            break;
                        case UPDATE:
                            destActionType = REPLACE_INTO;
                            break;
                        case DELETE:
                            destActionType = UPDTAE_TO_NULL;
                            //TODO primery key 不能update to null
                            break;
                    }
                } else {
                    destActionType = srcActionType;
                }
            }
        }

        //确定了dest的actionType，database, table, column
        //将actionType，database, table, column拼装成MysqlUpdateStatement(含sql语句和args)
        //insert:获取所有column即可
        //update:set是column变化的，若有key，则where是id，无则所有column作为where
        //delete：若有key，则where是id，无则所有column作为where
        //sql(借助velocity，由sql模板和变量，生成sql)
        //        String insertSql = "INSERT INTO `<database>`.`<table>` (`column1`) VALUES ( ?,?)";
        //        String updateSql = "UPDATE `<database>`.`<table>` SET `<column>`=?,`<column>`=? WHERE `<column>`=? AND `<column>`=?";
        //        String deleteSql = "DELETE FROM `<database>`.`<table>` WHERE `<column>`=? AND `<column>`=?";

        MysqlUpdateStatement mus = new MysqlUpdateStatement();
        if (destActionType == INSERT) {
            String sql = SqlBuildUtil.buildInsertSql(destDatabaseName, destTableName, destColumnNames);
            mus.setSql(sql);
            mus.setArgs(destColumnValues.toArray());
        } else if (destActionType == UPDATE) {
            String sql = SqlBuildUtil.buildUpdateSql(destDatabaseName, destTableName, destColumnNames, destWhereColumnNames);
            mus.setSql(sql);
            List<Object> args = new ArrayList<Object>();
            args.addAll(destColumnValues);
            args.addAll(destWhereColumnValues);
            mus.setArgs(args.toArray());
        } else if (destActionType == DELETE) {
            String sql = SqlBuildUtil.buildDeleteSql(destDatabaseName, destTableName, destWhereColumnNames);
            mus.setSql(sql);
            mus.setArgs(destWhereColumnValues.toArray());
        } else if (destActionType == REPLACE_INTO) {
            String sql = SqlBuildUtil.buildReplaceSql(destDatabaseName, destTableName, destWhereColumnNames);
            mus.setSql(sql);
            mus.setArgs(destWhereColumnValues.toArray());
        } else if (destActionType == UPDTAE_TO_NULL) {
            String sql = SqlBuildUtil.buildUpdateToNullSql(destDatabaseName, destTableName, destColumnNames, destWhereColumnNames);
            mus.setSql(sql);
            List<Object> args = new ArrayList<Object>();
            args.addAll(destWhereColumnValues);
            mus.setArgs(args.toArray());
        }
        LOG.info(mus.toString());
        //TODO 修改sync验证生成sql的过程是否正确？

        //执行sql
        //使用jdbc直接执行MysqlUpdateStatement
        jdbcTemplate.update(mus.getSql(), mus.getArgs());

    }

    private void _execute2(SyncConfig syncConfig2, RowChangedEvent rowChangedEvent) {
        MysqlUpdateStatement mus = convert(syncConfig2, rowChangedEvent);

        //        jdbcTemplate.update(sql);
        jdbcTemplate.update(mus.getSql(), mus.getArgs());
    }

    private MysqlUpdateStatement convert(SyncConfig sync, RowChangedEvent rowChangedEvent) {
        MysqlUpdateStatement mus = new MysqlUpdateStatement();
        RowChangedEvent event = rowChangedEvent.clone();

        //来源的database,column
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
                event.setTable(table.getTo());//如果不是*，则destTableName为to的值
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
        System.out.println("----------------- after convert -------------");
        System.out.println(event);
        String sql = parseSql(event);
        System.out.println("----------------- after parse sql -----------");
        System.out.println(sql);
        //构造args
        Object[] args = parseArgs(event);
        System.out.println("----------------- after parse args -----------");
        System.out.println(Arrays.toString(args));
        mus.setSql(sql);
        mus.setArgs(args);
        return mus;

    }

    private String parseSql(RowChangedEvent event) {
        String sql = null;
        int actionType = event.getActionType();
        switch (actionType) {
            case INSERT:
                sql = SqlBuildUtil.buildInsertSql2(event);
                break;
            case UPDATE:
                sql = SqlBuildUtil.buildUpdateSql2(event);
                break;
            case DELETE:
                sql = SqlBuildUtil.buildDeleteSql2(event);
                break;
            case UPDTAE_TO_NULL:
                sql = SqlBuildUtil.buildUpdateToNullSql2(event);
                break;
            case REPLACE_INTO:
                sql = SqlBuildUtil.buildReplaceSql2(event);
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

    public void test_execute() {

    }

}
