package com.dianping.puma.storage.cleanup;

import com.dianping.puma.storage.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ExpiredDeleteStrategy implements DeleteStrategy {

    private final int preservedDay = 3;

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{8}");

    @Override
    public boolean canClean(String name) {
        if (!DATE_PATTERN.matcher(name).matches()) {
            return false;
        }
        return DateUtils.expired(name, DateUtils.getNowString(), preservedDay);
    }
}
