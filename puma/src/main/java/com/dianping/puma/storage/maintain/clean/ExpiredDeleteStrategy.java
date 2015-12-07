package com.dianping.puma.storage.maintain.clean;

import com.dianping.puma.storage.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.regex.Pattern;

@Service
public class ExpiredDeleteStrategy implements DeleteStrategy {

    private final int preservedDay = 3;

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{8}");

    @Override
    public boolean canClean(File directory) {
        if (!DATE_PATTERN.matcher(directory.getName()).matches()) {
            return false;
        }
        return DateUtils.expired(directory.getName(), DateUtils.getNowString(), preservedDay);
    }
}
