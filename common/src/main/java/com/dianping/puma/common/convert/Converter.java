package com.dianping.puma.common.convert;

import java.lang.reflect.Type;

/**
 * Created by xiaotian.li on 16/1/6.
 * Email: lixiaotian07@gmail.com
 */
public interface Converter {

    <S, D> D convert(S source, Class<D> clazz);

    <S, D> D convert(S source, Type type);
}
