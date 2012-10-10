package com.dianping.puma.syncserver.mysql;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.sync.Database;
import com.dianping.puma.core.sync.Sync;
import com.dianping.puma.core.sync.Table;

public class MysqlExecutor {

    private Sync sync;

    private final JdbcTemplate jdbcTemplate;

    public MysqlExecutor(String url, String username, String password) {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource(url, username, password, true);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void execute(ChangedEvent event) {
        // TODO Auto-generated method stub
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
            //获取event的[database,table,column]

            //从Sync找出[database,table,column]的处理方式
        }
    }

    /**
     * 根据Sync和RowChangeEvent构造出sql和args
     * 
     * @param sync
     * @param rowChangedEvent
     * @return
     */
    public MysqlUpdateStatement parse(Sync sync, RowChangedEvent rowChangedEvent) {
        //database,table,column
        String databaseName = rowChangedEvent.getDatabase();
        String tableName = rowChangedEvent.getTable();
        Map<String, ColumnInfo> columnMap = rowChangedEvent.getColumns();
        int actionType = rowChangedEvent.getActionType();//actionType
        //Sync
        List<Database> databases = sync.getInstance().getDatabases();
        Database database = findDatabase(databases, databaseName);
        List<Table> tables = database.getTables();
        Table table = findTable(tables, tableName);
        //        table.get

        //确定action，database, table, column
        //insert:获取所有column即可
        //update:set是column变化的，若有key，则where是id，无则所有column作为where
        //delete：若有key，则where是id，无则所有column作为where

        //sql(借助velocity，由sql模板和变量，生成sql)
        String action = null;
        String insertSql = "INSERT INTO `<database>`.`<table>` (`column1`) VALUES ( ?,?)";
        String updateSql = "UPDATE `<database>`.`<table>` SET `<column>`=?,`<column>`=? WHERE `<column>`=? AND `<column>`=?";
        String deleteSql = "DELETE FROM `<database>`.`<table>` WHERE `<column>`=? AND `<column>`=?";

        return null;
    }

    private Table findTable(List<Table> tables, String tableName) {
        // TODO Auto-generated method stub
        return null;
    }

    private Database findDatabase(List<Database> databases, String databaseName) {
        // TODO Auto-generated method stub
        return null;
    }

}
