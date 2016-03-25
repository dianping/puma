package com.dianping.puma.portal.db;

import com.dianping.lion.client.ConfigCache;
import com.google.common.collect.Lists;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class CommandTableService implements TableService {

    private final Logger logger = LoggerFactory.getLogger(CommandTableService.class);

    private final long timeout = 60 * 1000; // 60s.

    ConfigCache configManager = ConfigCache.getInstance();

    @Autowired
    DatabaseService databaseService;

    private ConcurrentMap<String, List<String>> tableCache = new ConcurrentHashMap<String, List<String>>();

    private ConcurrentMap<String, Date> tableCacheTimes = new ConcurrentHashMap<String, Date>();

    @Override
    public List<String> getTables(String database) {
        List<String> tables = tableCache.get(database);
        if (tables != null && !isTimeout(database)) {
            logger.info("Get cached table infos: {}.", tables);
            return tables;
        } else {
            try {
                tables = getTablesWithoutCache(database);
                logger.info("Get real time table infos: {}.", tables);
                tableCache.put(database, tables);
                tableCacheTimes.put(database, new Date());
                return tables;
            } catch (SQLException e) {
                return Lists.newArrayList();
            }
        }
    }

    protected List<String> getTablesWithoutCache(String database) throws SQLException {
        return newQueryRunner(database).query("show tables", new ResultSetHandler<List<String>>() {
            @Override
            public List<String> handle(ResultSet rs) throws SQLException {
                List<String> result = new ArrayList<String>();
                while (rs.next()) {
                    String table = rs.getString(1);
                    result.add(table);
                }
                return result;
            }
        });
    }

    protected QueryRunner newQueryRunner(String database) {
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setJdbcUrl(databaseService.findWriteJdbcUrl(database));
        ds.setUser(configManager.getProperty("puma.server.binlog.username"));
        ds.setPassword(configManager.getProperty("puma.server.binlog.password"));
        return new QueryRunner(ds);
    }

    protected boolean isTimeout(String jdbcUrl) {
        Date date = tableCacheTimes.get(jdbcUrl);
        return date == null || new Date().getTime() - date.getTime() > timeout;
    }
}
