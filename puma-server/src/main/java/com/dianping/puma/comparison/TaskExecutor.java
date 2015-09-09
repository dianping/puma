package com.dianping.puma.comparison;

import com.dianping.cat.Cat;
import com.dianping.puma.comparison.comparison.Comparison;
import com.dianping.puma.comparison.datasource.DataSourceBuilder;
import com.dianping.puma.comparison.fetcher.SourceFetcher;
import com.dianping.puma.comparison.fetcher.TargetFetcher;
import com.dianping.puma.comparison.mapper.RowMapper;
import com.dianping.puma.comparison.reporter.Reporter;
import com.dianping.puma.core.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskExecutor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);

    private final TaskEntity task;

    private final DataSource sourceDataSource;

    private final DataSource targetDataSource;

    private final RowMapper rowMapper;

    private final SourceFetcher sourceFetcher;

    private final TargetFetcher targetFetcher;

    private final Reporter reporter;

    private final Comparison comparison;

    public TaskExecutor(TaskEntity task) {
        this.task = task;
        this.sourceDataSource = initSourceDataSource(task);
        this.targetDataSource = initTargetDataSource(task);
        this.sourceFetcher = initSourceFetcher(task);
        this.sourceFetcher.init(this.sourceDataSource);
        this.targetFetcher = initTargetFetcher(task);
        this.targetFetcher.init(this.targetDataSource);
        this.rowMapper = initRowMapper(task);
        this.comparison = initComparison(task);
        this.reporter = initReporter(task);
    }

    protected Reporter initReporter(TaskEntity task) {
        return null;
    }

    protected Comparison initComparison(TaskEntity task) {
        return null;
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
        DataSourceBuilder builder = (DataSourceBuilder) fromClassNameAndJson(task.getTargetDataSourceBuilder(), task.getTargetDataSourceBuilderProp());
        return builder.build();
    }

    protected DataSource initSourceDataSource(TaskEntity task) {
        DataSourceBuilder builder = (DataSourceBuilder) fromClassNameAndJson(task.getSourceDataSourceBuilder(), task.getSourceDataSourceBuilderProp());
        return builder.build();
    }

    protected Object fromClassNameAndJson(String className, String json) {
        try {
            return GsonUtil.fromJson(json, Class.forName(className));
        } catch (ClassNotFoundException e) {
            Cat.logError(className, e);
            LOG.error(className, e);
            throw new RuntimeException(className, e);
        }
    }

    @Override
    public void run() {
    }
}
