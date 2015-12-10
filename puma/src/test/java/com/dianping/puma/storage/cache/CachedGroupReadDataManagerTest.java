package com.dianping.puma.storage.cache;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.storage.Sequence;
import com.dianping.puma.storage.data.GroupReadDataManager;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CachedGroupReadDataManagerTest {

    private CachedGroupReadDataManager target;

    private GroupReadDataManager groupReadDataManager;

    private CachedDataStorage.Reader cachedDataStorageReader;

    @Before
    public void setUp() throws Exception {
        groupReadDataManager = mock(GroupReadDataManager.class);
        CachedDataStorage cachedDataStorage = mock(CachedDataStorage.class);
        cachedDataStorageReader = mock(CachedDataStorage.Reader.class);
        doReturn(cachedDataStorageReader).when(cachedDataStorage).createReader();
        CachedDataStorageFactory factory = spy(new CachedDataStorageFactory());
        doReturn(cachedDataStorage).when(factory).initCachedDataStorage();
        target = new CachedGroupReadDataManager("test", factory, groupReadDataManager);
        target.start();
    }

    @Test
    public void test_open_memory() throws Exception {
        doReturn(true).when(cachedDataStorageReader).open(any(Sequence.class));
        target.open(new Sequence(20151111, 0, 0));
        Assert.assertEquals(target.getStorageMode(), "Memory");
        verify(cachedDataStorageReader, only()).open(any(Sequence.class));
    }

    @Test
    public void test_open_file() throws Exception {
        doReturn(false).when(cachedDataStorageReader).open(any(Sequence.class));
        target.open(new Sequence(20151111, 0, 0));
        Assert.assertEquals(target.getStorageMode(), "File");
        verify(cachedDataStorageReader, times(1)).open(any(Sequence.class));
        verify(groupReadDataManager, times(1)).open(any(Sequence.class));
    }

    @Test
    public void test_open_memory_switch_to_file() throws Exception {
        test_open_memory();

        ChangedEventWithSequence result = new ChangedEventWithSequence(new RowChangedEvent(1, 1, "", 1), new Sequence(1, 1, 1));
        doReturn(result).when(cachedDataStorageReader).next();
        doReturn(result.getChangedEvent()).when(groupReadDataManager).next();

        Assert.assertNotNull(target.next());
        verify(cachedDataStorageReader, times(1)).next();
        Assert.assertEquals(target.getStorageMode(), "Memory");

        doThrow(new IOException()).when(cachedDataStorageReader).next();
        Assert.assertNotNull(target.next());

        Assert.assertEquals(target.getStorageMode(), "File");
        verify(cachedDataStorageReader, times(2)).next();
        verify(groupReadDataManager, times(1)).next();
    }

    @Test
    public void test_open_file_switch_to_memory() throws Exception {
        test_open_file();

        ChangedEventWithSequence result = new ChangedEventWithSequence(new RowChangedEvent(1, 1, "", 1), new Sequence(1, 1, 1));
        doReturn(result).when(cachedDataStorageReader).next();
        doReturn(result.getChangedEvent()).when(groupReadDataManager).next();

        doReturn(true).when(cachedDataStorageReader).open(any(Sequence.class));

        Assert.assertNotNull(target.next());
        Assert.assertEquals(target.getStorageMode(), "Memory");
        verify(cachedDataStorageReader, times(0)).next();
        verify(groupReadDataManager, times(1)).next();

        Assert.assertNotNull(target.next());
        verify(cachedDataStorageReader, times(1)).next();
        verify(cachedDataStorageReader, times(2)).open(any(Sequence.class));
        Assert.assertEquals(target.getStorageMode(), "Memory");
        verify(groupReadDataManager, times(1)).next();
    }
}