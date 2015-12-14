package com.dianping.puma.storage.cleanup;

public interface DeleteStrategy {

    boolean canClean(String name);
}
