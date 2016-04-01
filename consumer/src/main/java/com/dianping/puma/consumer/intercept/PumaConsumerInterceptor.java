package com.dianping.puma.consumer.intercept;

import com.dianping.puma.common.intercept.PumaInterceptor;
import com.dianping.puma.consumer.ha.PumaClientCleanable;

/**
 * Created by xiaotian.li on 16/4/1.
 * Email: lixiaotian07@gmail.com
 */
public interface PumaConsumerInterceptor<T> extends PumaInterceptor<T>, PumaClientCleanable {
}
