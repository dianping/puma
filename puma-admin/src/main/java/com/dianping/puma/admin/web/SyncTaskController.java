package com.dianping.puma.admin.web;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dianping.puma.admin.remote.reporter.SyncTaskControllerReporter;
import com.dianping.puma.admin.remote.reporter.SyncTaskOperationReporter;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.constant.ActionController;
import com.dianping.puma.core.constant.ActionOperation;
import com.dianping.puma.core.model.state.SyncTaskState;
import com.dianping.puma.core.model.state.TaskStateContainer;
import com.dianping.puma.core.service.SyncTaskStateService;
import com.dianping.swallow.common.producer.exceptions.SendFailedException;
import com.mongodb.MongoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.puma.core.service.SyncTaskService;
import com.dianping.puma.core.entity.SyncTask;

@Controller
public class SyncTaskController {

	private static final Logger LOG = LoggerFactory.getLogger(SyncTaskController.class);

	@Autowired
	private SyncTaskService syncTaskService;

	@Autowired
	SyncTaskStateService syncTaskStateService;

	@Autowired
	private TaskStateContainer syncTaskStateContainer;

	@Autowired
	private SyncTaskOperationReporter syncTaskOperationReporter;
	
	@Autowired
	private SyncTaskControllerReporter syncTaskControllerReporter;

	private static final int PAGESIZE = 30;

	@RequestMapping(value = { "/sync-task" })
	public ModelAndView created(HttpServletRequest request, HttpServletResponse response) {
		return created0(request, response, 1);
	}

	@RequestMapping(value = { "/sync-task/{pageNum}" })
	public ModelAndView created0(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("pageNum") Integer pageNum) {
		Map<String, Object> map = new HashMap<String, Object>();
		//        System.out.println(syncConfigService.find());
		int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
		List<SyncTask> syncTasks = syncTaskService.find(offset, PAGESIZE);
		map.put("syncTasks", syncTasks);
		map.put("createdActive", "active");
		map.put("subPath", "main");
		map.put("path", "sync-task");
		return new ModelAndView("main/container", map);
	}

	@RequestMapping(value = {
			"/sync-task/remove" }, method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public String removePost(String name) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			// Actually name here.

			SyncTask syncTask = syncTaskService.find(name);

			syncTaskService.remove(name);
			// Publish puma task operation event to puma server.
			syncTaskOperationReporter.report(syncTask.getSyncServerName(), syncTask.getName(), ActionOperation.REMOVE);

			map.put("success", true);
		} catch (MongoException e) {
			map.put("error", "storage");
			map.put("success", false);
		} catch (SendFailedException e) {
			map.put("error", "notify");
			map.put("success", false);
		} catch (Exception e) {
			map.put("error", e.getMessage());
			map.put("success", false);
		}

		map.put("success", true);

		return GsonUtil.toJson(map);
	}

	/**
	 * 查询SyncTask状态
	 */
	@RequestMapping(value = "/sync-task/status", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object status(HttpSession session, String taskName) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			SyncTaskState syncTaskState = syncTaskStateService.find(taskName);
			//binlog信息，从数据库查询binlog位置即可，不需要从SyncServer实时发过来的status中的binlog获取
			//binlogInfoOfIOThread则是从status中获取
			SyncTask syncTask = this.syncTaskService.find(taskName);
			syncTaskState.setBinlogInfo(syncTask.getBinlogInfo());

			map.put("status", syncTaskState);
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

	/**
	 * 查询SyncTask
	 */
	@RequestMapping(value = "/sync-task/detail/{taskName}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public ModelAndView task(HttpSession session, @PathVariable String taskName) {
		Map<String, Object> map = new HashMap<String, Object>();
		SyncTask syncTask = this.syncTaskService.find(taskName);
		SyncTaskState syncTaskState = syncTaskStateService.find(taskName);
		map.put("syncTask", syncTask);
		map.put("syncTaskState", syncTaskState);
		map.put("createdActive", "active");
		map.put("subPath", "detail");
		map.put("path", "sync-task");
		return new ModelAndView("main/container", map);
	}

	/**
	 * 暂停SyncTask状态
	 */
	@RequestMapping(value = "/sync-task/pause", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object pause(HttpSession session, String taskName) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			SyncTask syncTask = syncTaskService.find(taskName);
			this.syncTaskService.updateStatusAction(taskName, ActionController.PAUSE);
			syncTaskControllerReporter.report(syncTask.getSyncServerName(), syncTask.getName(), ActionController.PAUSE);
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

	/**
	 * 恢复SyncTask的运行
	 */
	@RequestMapping(value = "/sync-task/rerun", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object rerun(HttpSession session, String taskName) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			SyncTask syncTask = syncTaskService.find(taskName);
			this.syncTaskService.updateStatusAction(taskName, ActionController.RESUME);
			syncTaskControllerReporter.report(syncTask.getSyncServerName(), syncTask.getName(), ActionController.RESUME);
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

	/**
	 * 修复SyncTask
	 */
	@RequestMapping(value = "/sync-task/resolved", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
	@ResponseBody
	public Object resolved(HttpSession session, String taskName) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			SyncTask syncTask = syncTaskService.find(taskName);
			this.syncTaskService.updateStatusAction(taskName, ActionController.RESUME);
			syncTaskControllerReporter.report(syncTask.getSyncServerName(), syncTask.getName(), ActionController.RESUME);
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
	

    /**
     * 显示待修改SyncTask的页面
     */
    @RequestMapping(value = "sync-task/modify/{taskName}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ModelAndView modify(HttpSession session, @PathVariable String taskName) {
        Map<String, Object> map = new HashMap<String, Object>();
        SyncTask syncTask = this.syncTaskService.find(taskName);
        session.setAttribute("syncTask", syncTask);
        SyncTaskState syncTaskState = syncTaskStateService.find(taskName);
        map.put("status", syncTaskState);

        map.put("createdActive", "active");
        map.put("path", "sync-task/modify");
        map.put("subPath", "step1");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = "sync-task/modify/step1Save", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object step1Save(HttpSession session, String[] databaseFrom, String[] databaseTo, String[] tableFrom, String[] tableTo,
                            Integer count[]) {
        Map<String, Object> map = new HashMap<String, Object>();
      /*  try {
            //验证参数
            //databaseFrom和databaseTo
            if (!(databaseFrom != null && databaseTo != null && databaseFrom.length == databaseTo.length)) {
                throw new IllegalArgumentException("源数据库个数和目标数据库的个数不一致。(databaseFrom个数=" + databaseFrom.length + ", databaseTo个数="
                        + databaseTo.length + ")");
            }
            //count
            if (count == null || count.length != databaseFrom.length) {
                throw new IllegalArgumentException("count参数个数和源数据库个数不一致。(database个数=" + databaseFrom.length + ", count个数="
                        + count.length + ")");
            }
            int totalTable = 0;
            for (int c : count) {
                totalTable += c;
            }
            //tableFrom和tableTo,totalTable
            if (tableFrom == null && tableTo == null) {
                tableFrom = new String[] { "*" };
                tableTo = new String[] { "*" };
            } else if (!(tableFrom != null && tableTo != null && tableFrom.length == tableTo.length)) {
                throw new IllegalArgumentException("源表个数和目标表的个数不一致。(tableFrom个数=" + tableFrom.length + ", tableTo个数="
                        + tableTo.length + ")");
            } else if (tableFrom.length != totalTable) {
                throw new IllegalArgumentException("count参数总和与表的个数不一致。(table个数=" + tableFrom.length + ", count总和=" + totalTable
                        + ")");
            }
            //解析mapping
            MysqlMapping newMysqlMapping = new MysqlMapping();
            int j = 0, countAmount = 0;
            for (int i = 0; i < databaseFrom.length; i++) {
                String dbFrom = databaseFrom[i];
                String dbTo = databaseTo[i];
                DatabaseMapping database = new DatabaseMapping();
                database.setFrom(dbFrom);
                database.setTo(dbTo);
                countAmount += count[i];
                for (; j < countAmount; j++) {
                    String tableFrom0 = tableFrom[j];
                    String tableTo0 = tableTo[j];
                    TableMapping table = new TableMapping();
                    table.setFrom(tableFrom0);
                    table.setTo(tableTo0);
                    database.addTable(table);
                }
                newMysqlMapping.addDatabase(database);
            }
            //对比新的mapping和现有的mapping
            SyncTask syncTask = (SyncTask) session.getAttribute("syncTask");
            MysqlMapping oldMysqlMapping = syncTask.getMysqlMapping();
            MysqlMapping additionalMysqlMapping = this.syncTaskService.compare(oldMysqlMapping, newMysqlMapping);

            //保存到session
            session.setAttribute("newMysqlMapping", newMysqlMapping);
            session.setAttribute("additionalMysqlMapping", additionalMysqlMapping);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }*/
        return GsonUtil.toJson(map);

    }

    @RequestMapping(method = RequestMethod.GET, value = { "sync-task/modify/step2" })
    public ModelAndView step2(HttpSession session) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
     /*   SyncTask syncTask = (SyncTask) session.getAttribute("syncTask");
        //查询所有syncServer
        List<PumaSyncServerConfig> syncServerConfigs = pumaSyncServerConfigService.findAll();
        //从会话中取出保存的mysqlMapping，计算出dumpMapping
        MysqlMapping additionalMysqlMapping = (MysqlMapping) session.getAttribute("additionalMysqlMapping");
        DumpMapping dumpMapping = this.syncTaskService.convertMysqlMappingToDumpMapping(syncTask.getSrcMysqlHost(),
                additionalMysqlMapping);
        session.setAttribute("dumpMapping", dumpMapping);

        map.put("syncServerConfigs", syncServerConfigs);
        map.put("createdActive", "active");
        map.put("path", "modify");
        map.put("subPath", "step2");*/
        return new ModelAndView("main/container", map);
    }

    /**
     * 创建DumpTask
     */
    @RequestMapping(value = "sync-task/modify/createDumpTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createDumpTask(HttpSession session, String srcMysqlHost, String syncServerName) {
        Map<String, Object> map = new HashMap<String, Object>();
        /*try {
            if (StringUtils.isBlank(syncServerName)) {
                throw new IllegalArgumentException("syncServerHost不能为空");
            }
            DumpMapping dumpMapping = (DumpMapping) session.getAttribute("dumpMapping");
            SyncTask syncTask = (SyncTask) session.getAttribute("syncTask");

            //查询所有syncServer
            DumpTask dumpTask = new DumpTask();
            dumpTask.setSrcMysqlName(syncTask.getSrcMysqlName());
            dumpTask.setSrcMysqlHost(syncTask.getSrcMysqlHost());
            dumpTask.setDestMysqlName(syncTask.getDestMysqlName());
            dumpTask.setDestMysqlHost(syncTask.getDestMysqlHost());
            dumpTask.setDumpMapping(dumpMapping);
            dumpTask.setSyncServerName(syncServerName);
            //保存dumpTask到数据库
            dumpTaskService.create(dumpTask);
            //保存dumpTask到session
            session.setAttribute("dumpTask", dumpTask);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }*/
        return GsonUtil.toJson(map);

    }

    /**
     * 刷新DumpTask的状态
     */
    @RequestMapping(value = "sync-task/modify/refreshDumpStatus", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object refreshDumpStatus(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        /*try {
            DumpTask dumpTask = (DumpTask) session.getAttribute("dumpTask");
            //检查参数
            if (dumpTask == null) {
                throw new IllegalArgumentException("dumpTask为空，可能是会话已经过期！");
            }
            //查询dumpTaskId对应的状态
            dumpTask = this.dumpTaskService.find(dumpTask.getId());
            session.setAttribute("dumpTask", dumpTask);
            TaskExecutorStatus status = systemStatusContainer.getStatus(Type.DUMP, dumpTask.getId());
            if (status != null) {
                map.put("status", status);
                if (status.getBinlogInfo() != null) {
                    session.setAttribute("binlogInfo", status.getBinlogInfo());
                }
            }

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }*/
        return GsonUtil.toJson(map);

    }

    /**
     * 创建CatchupTask的页面
     */
    @RequestMapping(method = RequestMethod.GET, value = { "sync-task/modify/step3" })
    public ModelAndView step3(HttpSession session) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        /*//查询所有syncServer
        List<PumaSyncServerConfig> syncServerConfigs = pumaSyncServerConfigService.findAll();

        map.put("syncServerConfigs", syncServerConfigs);
        map.put("pumaClientName", "Catchup-" + UUID.randomUUID());
        map.put("createdActive", "active");
        map.put("path", "modify");
        map.put("subPath", "step3");*/
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = "sync-task/modify/createCatchupTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object createCatchupTask(HttpSession session, String binlogFile, String binlogPosition, String pumaClientName) {
        Map<String, Object> map = new HashMap<String, Object>();
       /* try {
            //检查参数
            if (StringUtils.isBlank(binlogFile)) {
                throw new IllegalArgumentException("binlogFile不能为空");
            }
            if (StringUtils.isBlank(binlogPosition)) {
                throw new IllegalArgumentException("binlogPosition不能为空");
            }
            if (StringUtils.isBlank(pumaClientName)) {
                throw new IllegalArgumentException("pumaClientName不能为空");
            }
            SyncTask syncTask = (SyncTask) session.getAttribute("syncTask");
            MysqlMapping additionalMysqlMapping = (MysqlMapping) session.getAttribute("additionalMysqlMapping");
            //创建
            CatchupTask catchupTask = new CatchupTask();
            catchupTask.setSrcMysqlName(syncTask.getSrcMysqlName());
            catchupTask.setDestMysqlName(syncTask.getDestMysqlName());
            catchupTask.setDestMysqlHost(syncTask.getDestMysqlHost());
            catchupTask.setMysqlMapping(additionalMysqlMapping);
            catchupTask.setSyncServerName(syncTask.getSyncServerName());
            BinlogInfo binlogInfo = new BinlogInfo();
            if (StringUtils.isNotBlank(binlogFile) && StringUtils.isNotBlank(binlogPosition)) {
                binlogInfo.setBinlogFile(binlogFile);
                binlogInfo.setBinlogPosition(Long.parseLong(binlogPosition));
            }
            catchupTask.setDdl(syncTask.isDdl());
            catchupTask.setDml(syncTask.isDml());
            catchupTask.setPumaClientName(pumaClientName);
            catchupTask.setServerId(syncTask.getServerId());
            catchupTask.setTransaction(syncTask.isTransaction());
            catchupTask.setSyncTaskId(syncTask.getId());
            //保存到数据库
            catchupTaskService.create(catchupTask, binlogInfo);
            //保存catchupTask到session
            session.setAttribute("catchupTask", catchupTask);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }*/
        return GsonUtil.toJson(map);
    }

    /**
     * 查看CatchupTask的状态
     */
    @RequestMapping(value = "sync-task/modify/refreshCatchupStatus", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object refreshCatchupStatus(HttpSession session) {
        Map<String, Object> map = new HashMap<String, Object>();
        /*try {
            CatchupTask catchupTask = (CatchupTask) session.getAttribute("catchupTask");
            //检查参数
            if (catchupTask == null) {
                throw new IllegalArgumentException("CatchupTask为空，可能是会话已经过期！");
            }
            //            重新查询
            //            catchupTask = this.catchupTaskService.find(catchupTask.getId());
            //            map.put("catchupTask", catchupTask);

            TaskExecutorStatus status = systemStatusContainer.getStatus(Type.CATCHUP, catchupTask.getId());
            if (status != null) {
                map.put("status", status);
                if (status.getBinlogInfo() != null) {
                    session.setAttribute("binlogInfo", status.getBinlogInfo());
                }
            }

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }*/
        return GsonUtil.toJson(map);

    }

    @RequestMapping(value = "sync-task/modify/updateSyncTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object updateSyncTask(HttpSession session, String binlogFile, String binlogPos) {
        Map<String, Object> map = new HashMap<String, Object>();
       /* try {
            //检查参数
            if (StringUtils.isBlank(binlogFile)) {
                throw new IllegalArgumentException("binlogFile不能为空");
            }
            if (StringUtils.isBlank(binlogPos)) {
                throw new IllegalArgumentException("binlogPosition不能为空");
            }
            SyncTask syncTask = (SyncTask) session.getAttribute("syncTask");
            MysqlMapping newMysqlMapping = (MysqlMapping) session.getAttribute("newMysqlMapping");
            //修改
            BinlogInfo binlogInfo = new BinlogInfo();
            binlogInfo.setBinlogFile(binlogFile);
            binlogInfo.setBinlogPosition(Long.parseLong(binlogPos));
            binlogInfo.setSkipToNextPos(true);
            //保存到数据库
            syncTaskService.modify(syncTask.getId(), binlogInfo, newMysqlMapping);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }*/
        return GsonUtil.toJson(map);
    }

    /**
     * 查看CatchupTask的状态
     */
    @RequestMapping(value = "sync-task/modify/delTask", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object delete(HttpSession session, Long id) {
        Map<String, Object> map = new HashMap<String, Object>();
       /* try {
            //检查参数
            if (id == null) {
                throw new IllegalArgumentException("id不能为空！");
            }
            //删除
            this.syncTaskService.delete(id);

            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }*/
        return GsonUtil.toJson(map);

    }

}
