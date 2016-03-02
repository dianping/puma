package com.dianping.puma.biz.convert;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

/**
 * Created by xiaotian.li on 16/1/6.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class ConverterImpl implements Converter {

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public <S, D> D convert(S source, Class<D> clazz) {
        if (source == null) {
            return null;
        }

        return modelMapper.map(source, clazz);
    }

    @Override
    public <S, D> D convert(S source, Type type) {
        if (source == null) {
            return null;
        }

        return modelMapper.map(source, type);
    }
}
