package com.dianping.puma.syncserver.mysql;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;

public class MysqlExecutor {
    private final JdbcTemplate jdbcTemplate;

    public MysqlExecutor(String url, String username, String password) {
        SingleConnectionDataSource dataSource = new SingleConnectionDataSource(url, username, password, true);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void execute(ChangedEvent event) {
        // TODO Auto-generated method stub
        System.out.println("********************Received " + event);
        if (event instanceof DdlEvent) {

        } else if (event instanceof RowChangedEvent) {

        }
    }

}
