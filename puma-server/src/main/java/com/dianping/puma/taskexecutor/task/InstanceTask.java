package com.dianping.puma.taskexecutor.task;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InstanceTask {

	private boolean main;

	private String taskName;

	private String instance;

	private List<DatabaseTask> databaseTasks = new ArrayList<DatabaseTask>();

	public InstanceTask() {}

	public InstanceTask(boolean main, String instance, DatabaseTask databaseTask) {
		this.main = main;
		this.instance = instance;
		this.databaseTasks.add(databaseTask);
		this.taskName = (main ? instance : instance + "-" + databaseTask.getDatabase());
	}

	public InstanceTask(boolean main, String instance, List<DatabaseTask> databaseTasks) {
		this.main = main;
		this.instance = instance;
		this.databaseTasks = databaseTasks;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("main", main)
				.append("taskName", taskName)
				.append("instance", instance)
				.append("databaseTasks", databaseTasks)
				.toString();
	}

	public int size() {
		return databaseTasks.size();
	}

	public boolean isMain() {
		return main;
	}

	public String getTaskName() {
		return taskName;
	}

	public String getInstance() {
		return instance;
	}

	public List<DatabaseTask> getDatabaseTasks() {
		return databaseTasks;
	}

	public void temp2Main() {
		main = true;
		taskName = instance;
	}

	public boolean contains(String database) {
		for (DatabaseTask databaseTask: databaseTasks) {
			if (databaseTask.getDatabase().equals(database)) {
				return true;
			}
		}
		return false;
	}

	public void merge(InstanceTask instanceTask) {
		if (instanceTask != null) {
			for (DatabaseTask databaseTask: instanceTask.getDatabaseTasks()) {
				create(databaseTask);
			}
		}
	}

	public void create(DatabaseTask task) {
		if (databaseTasks == null) {
			databaseTasks = new ArrayList<DatabaseTask>();
		}
		databaseTasks.add(task);
	}

	public void update(DatabaseTask task) {
		for (DatabaseTask databaseTask: databaseTasks) {
			if (databaseTask.equalName(task)) {
				databaseTask.setTables(task.getTables());
				databaseTask.setBeginTime(task.getBeginTime());
			}
		}
	}

	public void remove(String database) {
		Iterator<DatabaseTask> iterator = databaseTasks.iterator();
		while (iterator.hasNext()) {
			DatabaseTask databaseTask = iterator.next();
			if (databaseTask.getDatabase().equals(database)) {
				iterator.remove();
				break;
			}
		}
	}
}
