package com.dianping.puma.comparison.manager.run;

import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.comparison.TaskEntity;
import com.dianping.puma.comparison.TaskExecutor;
import com.dianping.puma.comparison.manager.utils.ThreadPool;
import org.springframework.stereotype.Service;

@Service
public class AsyncTaskRunner implements TaskRunner {

	@Override
	public TaskRunFuture run(CheckTaskEntity checkTask) {
		TaskEntity taskEntity = transform(checkTask);
		TaskExecutor.Builder builder = TaskExecutor.Builder.create(taskEntity);
		TaskExecutor taskExecutor = builder.build();

		TaskRunFuture taskRunFuture = new TaskRunFuture(taskExecutor);
		ThreadPool.submit(taskExecutor);
		return taskRunFuture;
	}

	protected TaskEntity transform(CheckTaskEntity checkTask) {
		TaskEntity taskEntity = new TaskEntity();
		taskEntity.setBeginTime(checkTask.getCurrTime());
		taskEntity.setBeginTime(checkTask.getNextTime());
		taskEntity.setComparison(checkTask.getComparison());
		taskEntity.setComparisonProp(checkTask.getComparisonProp());
		taskEntity.setMapper(checkTask.getMapper());
		taskEntity.setMapperProp(checkTask.getMapperProp());
		taskEntity.setSourceFetcher(checkTask.getSourceFetcher());
		taskEntity.setSourceFetcherProp(checkTask.getSourceFetcherProp());
		taskEntity.setSourceDsBuilder(checkTask.getSourceDsBuilder());
		taskEntity.setSourceDsBuilderProp(checkTask.getSourceDsBuilderProp());
		taskEntity.setTargetDsBuilder(checkTask.getTargetDsBuilder());
		taskEntity.setTargetDsBuilderProp(checkTask.getTargetDsBuilderProp());
		taskEntity.setTargetFetcher(checkTask.getTargetFetcher());
		taskEntity.setTargetFetcherProp(checkTask.getTargetFetcherProp());
		return taskEntity;
	}
}
