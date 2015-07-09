package com.dianping.puma.biz.service;


import com.dianping.puma.biz.entity.TaskState;

import java.util.List;

/**
 * Dozer @ 7/8/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public interface TaskStateService {

    List<TaskState> find(String name);

    TaskState find(String name, String serverName);

}