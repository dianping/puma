package com.dianping.puma.storage.maintain.clean;

public interface DeleteStrategy {

    boolean canClean(String name);
}
