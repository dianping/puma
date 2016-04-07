package com.dianping.puma.common.convert;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;

/**
 * Created by xiaotian.li on 16/1/6.
 * Email: lixiaotian07@gmail.com
 */
@Service
public class ModelMapperConverter implements Converter {

    private ModelMapper modelMapper;

    private void init() {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Override
    public <S, D> D convert(S source, Class<D> clazz) {
        if (modelMapper == null) {
            init();
        }

        if (source == null) {
            return null;
        }

        return modelMapper.map(source, clazz);
    }

    @Override
    public <S, D> D convert(S source, Type type) {
        if (modelMapper == null) {
            init();
        }

        if (source == null) {
            return null;
        }

        return modelMapper.map(source, type);
    }
}
