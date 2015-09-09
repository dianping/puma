package com.dianping.puma.comparison;

import com.dianping.puma.comparison.fetcher.SourceFetcher;
import com.dianping.puma.comparison.fetcher.TargetFetcher;
import com.dianping.puma.comparison.mapper.RowMapper;

import javax.sql.DataSource;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskExecutor implements Runnable {

    private final TaskEntity task;

    private final DataSource sourceDataSource;

    private final DataSource targetDataSource;

    private final RowMapper rowMapper;

    private final SourceFetcher sourceFetcher;

    private final TargetFetcher targetFetcher;

    public TaskExecutor(TaskEntity task) {
        this.task = task;
        this.sourceDataSource = initSourceDataSource(task);
        this.targetDataSource = initTargetDataSource(task);
        this.sourceFetcher = initSourceFetcher(task);
        this.targetFetcher = initTargetFetcher(task);
        this.rowMapper = initRowMapper(task);
    }

    protected TargetFetcher initTargetFetcher(TaskEntity task) {
        return null;
    }

    protected SourceFetcher initSourceFetcher(TaskEntity task) {
        return null;
    }

    protected RowMapper initRowMapper(TaskEntity task) {
        return null;
    }

    protected DataSource initTargetDataSource(TaskEntity task) {
        return null;
    }

    protected DataSource initSourceDataSource(TaskEntity task) {
        return null;
    }

    @Override
    public void run() {
    }
}
