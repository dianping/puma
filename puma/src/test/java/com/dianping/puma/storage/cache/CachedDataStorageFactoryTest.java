package com.dianping.puma.storage.cache;

import junit.framework.Assert;
import org.junit.Test;

import static org.mockito.Mockito.*;

/**
 * Dozer @ 2015-12
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class CachedDataStorageFactoryTest {
    @Test(expected = Exception.class)
    public void test_release_reader_exp() throws Exception {
        Assert.assertNotNull(CachedDataStorageFactory.getInstance().getReadCachedDataManager("test"));
        Assert.assertNotNull(CachedDataStorageFactory.getInstance().getReadCachedDataManager("test"));
        CachedDataStorageFactory.getInstance().releaseReadCachedDataManager("test");
        CachedDataStorageFactory.getInstance().releaseReadCachedDataManager("test");
        CachedDataStorageFactory.getInstance().releaseReadCachedDataManager("test");
    }

    @Test(expected = Exception.class)
    public void test_release_writer_exp() throws Exception {
        Assert.assertNotNull(CachedDataStorageFactory.getInstance().getWriteCachedDataManager("test"));
        CachedDataStorageFactory.getInstance().releaseWriteCachedDataManager("test");
        CachedDataStorageFactory.getInstance().releaseWriteCachedDataManager("test");
    }

    @Test(expected = Exception.class)
    public void test_can_not_alloc_multi_writer() throws Exception {
        CachedDataStorageFactory.getInstance().getWriteCachedDataManager("test");
        CachedDataStorageFactory.getInstance().getWriteCachedDataManager("test");
    }

    @Test
    public void test_start_stop() throws Exception {
        CachedDataStorageFactory factory = spy(new CachedDataStorageFactory());
        CachedDataStorage storage = mock(CachedDataStorage.class);
        doReturn(storage).when(factory).initCachedDataStorage();

        factory.getWriteCachedDataManager("test");
        verify(storage, times(0)).start();
        verify(storage, times(1)).stop();

        factory.getReadCachedDataManager("test");
        verify(storage, times(1)).start();
        verify(storage, times(1)).stop();

        factory.getReadCachedDataManager("test");
        verify(storage, times(2)).start();
        verify(storage, times(1)).stop();

        factory.releaseWriteCachedDataManager("test");
        verify(storage, times(2)).start();
        verify(storage, times(2)).stop();

        factory.getWriteCachedDataManager("test");
        verify(storage, times(3)).start();
        verify(storage, times(2)).stop();


        factory.releaseReadCachedDataManager("test");
        verify(storage, times(4)).start();
        verify(storage, times(2)).stop();

        factory.releaseReadCachedDataManager("test");
        verify(storage, times(4)).start();
        verify(storage, times(3)).stop();
    }
}