package com.dianping.puma.admin.web;

import com.dianping.puma.admin.monitor.ReplicationTaskStatusContainer;
import com.dianping.puma.admin.service.DBInstanceConfigService;
import com.dianping.puma.admin.service.ReplicationTaskService;
import com.dianping.puma.admin.service.ServerConfigService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.model.replication.ReplicationTaskStatus;
import com.dianping.puma.core.replicate.model.config.DBInstanceConfig;
import com.dianping.puma.core.replicate.model.config.FileSenderConfig;
import com.dianping.puma.core.replicate.model.config.ServerConfig;
import com.dianping.puma.core.replicate.model.task.ReplicationTask;
import com.dianping.puma.core.model.BinlogInfo;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.*;

@Controller
public class ReplicationTaskController {

	private static final Logger LOG = LoggerFactory.getLogger(DBInstanceConfigController.class);

	@Autowired
	ReplicationTaskService replicationTaskService;

	@Autowired
	DBInstanceConfigService dbInstanceConfigService;

	@Autowired
	ServerConfigService serverConfigService;

	@Autowired
	ReplicationTaskStatusContainer replicationTaskStatusContainer;

	@RequestMapping(value = { "/replicationTask" })
	public ModelAndView view(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<ReplicationTask> replicationTasks = replicationTaskService.findAll();

		map.put("replicationTasks", replicationTasks);
		map.put("path", "replicationTask");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/replicationTask/create" }, method = RequestMethod.GET)
	public ModelAndView create(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<DBInstanceConfig> dbInstanceConfigs = dbInstanceConfigService.findAll();
		List<ServerConfig> serverConfigs = serverConfigService.findAll();

		map.put("dbInstanceConfigs", dbInstanceConfigs);
		map.put("serverConfigs", serverConfigs);
		map.put("path", "replicationTask");
		map.put("subPath", "create");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = { "/replicationTask/create" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String createPost(String dbInstanceName, String replicationServerName, String binlogFile,
			String binlogPosition, String taskId, String masterStorageBaseDir, String masterBucketFilePrefix,
			int maxMasterBucketLengthMB, int maxMasterFileCount, String slaveStorageBaseDir,
			String slaveBucketFilePrefix, int maxSlaveBucketLengthMB, int maxSlaveFileCount,
			int preservedDay, String binlogIndexBaseDir) {

		Map<String, Object> map = new HashMap<String, Object>();

		BinlogInfo binlogInfo = new BinlogInfo();
		binlogInfo.setBinlogFile(binlogFile);
		binlogInfo.setBinlogPosition(Long.parseLong(binlogPosition));

		List<FileSenderConfig> fileSenderConfigs = new ArrayList<FileSenderConfig>();
		FileSenderConfig fileSenderConfig = new FileSenderConfig();
		fileSenderConfig.setMasterBucketFilePrefix(masterBucketFilePrefix);
		fileSenderConfig.setMaxMasterBucketLengthMB(maxMasterBucketLengthMB);
		fileSenderConfig.setStorageMasterBaseDir(masterStorageBaseDir);
		fileSenderConfig.setSlaveBucketFilePrefix(slaveBucketFilePrefix);
		fileSenderConfig.setMaxSlaveBucketLengthMB(maxSlaveBucketLengthMB);
		fileSenderConfig.setStorageSlaveBaseDir(slaveStorageBaseDir);
		fileSenderConfig.setFileSenderName("fileSender-" + taskId);
		fileSenderConfig.setStorageName("storage-" + taskId);
		fileSenderConfig.setPreservedDay(preservedDay);
		fileSenderConfig.setMaxMasterFileCount(maxMasterFileCount);
		fileSenderConfig.setBinlogIndexBaseDir(binlogIndexBaseDir);
		fileSenderConfigs.add(fileSenderConfig);

		ReplicationTask replicationTask = new ReplicationTask();

		// @TODO
		replicationTask.setTaskId(taskId);
		replicationTask.setTaskName(taskId);

		replicationTask.setDispatchName("dispatch-" + taskId);
		replicationTask.setFileSenderConfigs(fileSenderConfigs);

		replicationTask.setDbInstanceName(dbInstanceName);
		DBInstanceConfig dbInstanceConfig = dbInstanceConfigService.find(dbInstanceName);
		replicationTask.setDbInstanceHost(dbInstanceConfig.getDbInstanceHost());
		replicationTask.setDbInstanceMetaHost(dbInstanceConfig.getDbInstanceMetaHost());

		replicationTask.setReplicationServerName(replicationServerName);
		replicationTask.setBinlogInfo(binlogInfo);

		try {
			this.replicationTaskService.save(replicationTask);
		}
		catch(Exception e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
		}

		map.put("success", true);

		return GsonUtil.toJson(map);
	}

	@RequestMapping(value = { "/replicationTask/refresh" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object refresh(String taskId) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			ReplicationTaskStatus taskStatus = replicationTaskStatusContainer.get(taskId);

			map.put("data", taskStatus);
			map.put("success", true);
		} catch(Exception e) {
			map.put("err", e.getMessage());
			map.put("success", false);
		}

		return GsonUtil.toJson(map);
	}


	@RequestMapping(value = { "/replicationTask/delete" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String delete(String id) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			if (id == null) {
				throw new IllegalArgumentException("id不能为空");
			}
			this.replicationTaskService.remove(new ObjectId(id));

			map.put("success", true);
		} catch (IllegalArgumentException e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
		} catch (Exception e) {
			map.put("success", false);
			map.put("errorMsg", e.getMessage());
			LOG.error(e.getMessage(), e);
		}
		return GsonUtil.toJson(map);
	}
}
