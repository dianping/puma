package com.dianping.puma.checkserver;

import com.dianping.cat.Cat;
import com.dianping.puma.checkserver.comparison.Comparison;
import com.dianping.puma.checkserver.datasource.DataSourceBuilder;
import com.dianping.puma.checkserver.fetcher.SourceFetcher;
import com.dianping.puma.checkserver.fetcher.TargetFetcher;
import com.dianping.puma.checkserver.mapper.RowMapper;
import com.dianping.puma.checkserver.model.SourceTargetPair;
import com.dianping.puma.checkserver.model.TaskEntity;
import com.dianping.puma.checkserver.model.TaskResult;
import com.dianping.puma.core.util.GsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public final class TaskExecutor implements Callable<TaskResult> {

    private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);

    private final RowMapper rowMapper;

    private final SourceFetcher sourceFetcher;

    private final TargetFetcher targetFetcher;

    private final DataSource sourceDs;

    private final DataSource targetDs;

    private final DataSourceBuilder sourceBuilder;

    private final DataSourceBuilder targetBuilder;

    private final Comparison comparison;

    private static final int RETRY_TIMES = 3;

    private static final long RETRY_SLEEP_TIME = 10 * 1000;

    private TaskExecutor(DataSourceBuilder sourceBuilder, DataSourceBuilder targetBuilder, SourceFetcher sourceFetcher, TargetFetcher targetFetcher, RowMapper rowMapper, Comparison comparison) {
        this.sourceBuilder = sourceBuilder;
        this.targetBuilder = targetBuilder;
        this.sourceDs = sourceBuilder.build();
        this.targetDs = targetBuilder.build();
        this.sourceFetcher = sourceFetcher;
        this.targetFetcher = targetFetcher;
        this.rowMapper = rowMapper;
        this.comparison = comparison;

        this.sourceFetcher.init(sourceDs);
        this.targetFetcher.init(targetDs);
    }

    @Override
    public TaskResult call() throws Exception {
        try {
            List<SourceTargetPair> difference = new ArrayList<SourceTargetPair>();

            fullCompare(difference);

            int tryTimes = 0;
            while (tryTimes++ < RETRY_TIMES && difference.size() > 0) {
                Thread.sleep(RETRY_SLEEP_TIME);
                retry(difference);
            }

            return new TaskResult().setDifference(difference);
        } finally {
            sourceBuilder.destory(sourceDs);
            targetBuilder.destory(targetDs);
        }
    }

    protected void retry(List<SourceTargetPair> difference) {
        Iterator<SourceTargetPair> iterable = difference.iterator();

        while (iterable.hasNext()) {
            SourceTargetPair pair = iterable.next();

            Map<String, Object> mappedColumn = rowMapper.mapToSource(pair.getSource());
            Map<String, Object> sourceData = sourceFetcher.retry(mappedColumn);

            SourceTargetPair newPair;
            if (sourceData == null) {
                newPair = targetFetcher.fetch(pair.getSource(), rowMapper.mapToTarget(pair.getSource()));
            } else {
                newPair = targetFetcher.fetch(sourceData, rowMapper.mapToTarget(sourceData));
            }

            if (comparison.compare(newPair.getSource(), newPair.getTarget())) {
                iterable.remove();
            }
        }
    }

    protected void fullCompare(List<SourceTargetPair> difference) {
        List<Map<String, Object>> sourceData;
        do {
            sourceData = sourceFetcher.fetch();

            List<SourceTargetPair> pairs;
            if (targetFetcher.isBatch()) {
                List<Map<String, Object>> mappedColumn = rowMapper.mapToTarget(sourceData);
                pairs = targetFetcher.fetch(sourceData, mappedColumn);
            } else {
                pairs = new ArrayList<SourceTargetPair>();
                for (Map<String, Object> data : sourceData) {
                    pairs.add(targetFetcher.fetch(data, rowMapper.mapToTarget(data)));
                }
            }

            for (SourceTargetPair pair : pairs) {
                if (!comparison.compare(pair.getSource(), pair.getTarget())) {
                    difference.add(pair);
                    LOG.info("find difference:" + GsonUtil.toJson(pair));
                }
            }
        } while (sourceData != null && sourceData.size() > 0);
    }

    public static final class Builder {

        private RowMapper rowMapper;

        private SourceFetcher sourceFetcher;

        private TargetFetcher targetFetcher;

        private DataSourceBuilder sourceBuilder;

        private DataSourceBuilder targetBuilder;

        private Comparison comparison;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(TaskEntity task) {
            Builder builder = new Builder();
            builder.sourceBuilder = initSourceDataSourceBuilder(task);
            builder.targetBuilder = initTargetDataSourceBuilder(task);
            builder.sourceFetcher = initSourceFetcher(task);
            builder.sourceFetcher.setStartTime(task.getBeginTime());
            builder.sourceFetcher.setEndTime(task.getEndTime());
            builder.targetFetcher = initTargetFetcher(task);
            builder.rowMapper = initRowMapper(task);
            builder.comparison = initComparison(task);
            return builder;
        }

        public TaskExecutor build() {
            return new TaskExecutor(this.sourceBuilder, this.targetBuilder, this.sourceFetcher, this.targetFetcher, this.rowMapper, this.comparison);
        }

        protected static Comparison initComparison(TaskEntity task) {
            return (Comparison) fromClassNameAndJson(Comparison.class, task.getComparison(), task.getComparisonProp());
        }

        protected static TargetFetcher initTargetFetcher(TaskEntity task) {
            return (TargetFetcher) fromClassNameAndJson(TargetFetcher.class, task.getTargetFetcher(), task.getTargetFetcherProp());
        }

        protected static SourceFetcher initSourceFetcher(TaskEntity task) {
            return (SourceFetcher) fromClassNameAndJson(SourceFetcher.class, task.getSourceFetcher(), task.getSourceFetcherProp());
        }

        protected static RowMapper initRowMapper(TaskEntity task) {
            return (RowMapper) fromClassNameAndJson(RowMapper.class, task.getMapper(), task.getMapperProp());
        }

        protected static DataSourceBuilder initTargetDataSourceBuilder(TaskEntity task) {
            DataSourceBuilder builder = (DataSourceBuilder) fromClassNameAndJson(DataSourceBuilder.class, task.getTargetDsBuilder(), task.getTargetDsBuilderProp());
            return builder;
        }

        protected static DataSourceBuilder initSourceDataSourceBuilder(TaskEntity task) {
            DataSourceBuilder builder = (DataSourceBuilder) fromClassNameAndJson(DataSourceBuilder.class, task.getSourceDsBuilder(), task.getSourceDsBuilderProp());
            return builder;
        }

        protected static Object fromClassNameAndJson(Class baseInterface, String className, String json) {
            try {
                return GsonUtil.fromJson(json, Class.forName(baseInterface.getPackage().getName() + "." + className));
            } catch (ClassNotFoundException e) {
                Cat.logError(className, e);
                LOG.error(className, e);
                throw new IllegalArgumentException(className, e);
            }
        }

        public Builder setRowMapper(RowMapper rowMapper) {
            this.rowMapper = rowMapper;
            return this;
        }

        public Builder setSourceFetcher(SourceFetcher sourceFetcher) {
            this.sourceFetcher = sourceFetcher;
            return this;
        }

        public Builder setTargetFetcher(TargetFetcher targetFetcher) {
            this.targetFetcher = targetFetcher;
            return this;
        }

        public Builder setComparison(Comparison comparison) {
            this.comparison = comparison;
            return this;
        }

        public Builder setSourceBuilder(DataSourceBuilder sourceBuilder) {
            this.sourceBuilder = sourceBuilder;
            return this;
        }

        public Builder setTargetBuilder(DataSourceBuilder targetBuilder) {
            this.targetBuilder = targetBuilder;
            return this;
        }
    }
}
