package com.dianping.puma.server.container;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.dianping.puma.server.container.DatabaseTaskContainer.*;

public interface InstanceTaskContainer {

	public InstanceTask get(String instanceName);

	public void create(InstanceTask instanceTask);

	public void update(InstanceTask instanceTask);

	public void remove(String instanceName);

	public class InstanceTask {

		private String instance;

		private List<DatabaseTask> databaseTasks = new ArrayList<DatabaseTask>();

		public InstanceTask() {}

		public InstanceTask(String instance, List<DatabaseTask> databaseTasks) {
			this.instance = instance;
			this.databaseTasks = databaseTasks;
		}

		public int size() {
			return databaseTasks.size();
		}

		public String getInstance() {
			return instance;
		}

		public List<DatabaseTask> getDatabaseTasks() {
			return databaseTasks;
		}

		public void create(DatabaseTask task) {
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
}
