package com.dianping.puma.checkserver.manager.run;

import com.dianping.puma.checkserver.MockTest;
import com.dianping.puma.biz.entity.CheckTaskEntity;
import com.dianping.puma.checkserver.model.SourceTargetPair;
import com.dianping.puma.checkserver.model.TaskResult;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AsyncTaskRunnerTest extends MockTest {

    @Spy
    AsyncTaskRunner taskRunner;

    @Mock
    Callable<TaskResult> taskExecutor;

    @Before
    public void before() {
    }

    @Test
    public void testRun() throws Exception {
        doReturn(taskExecutor).when(taskRunner).build(any(CheckTaskEntity.class));

        final TaskResult taskResult = new TaskResult();
        taskResult.setDifference(Lists.newArrayList(new SourceTargetPair(null, null)));
        when(taskExecutor.call()).thenAnswer(new Answer<TaskResult>() {
            @Override
            public TaskResult answer(InvocationOnMock invocationOnMock) throws Throwable {
                Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
                return taskResult;
            }
        });

        taskRunner.run(new CheckTaskEntity(), new TaskRunFutureListener() {
            @Override
            public void onSuccess(TaskResult result) {
                assertEquals(1, result.getDifference().size());
            }

            @Override
            public void onFailure(Throwable cause) {
                assertTrue(false);
            }
        });

        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
}