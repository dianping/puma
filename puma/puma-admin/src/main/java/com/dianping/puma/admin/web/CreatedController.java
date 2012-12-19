package com.dianping.puma.admin.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.puma.admin.service.SyncConfigService;
import com.dianping.puma.admin.service.SyncTaskService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.sync.SyncConfig;
import com.dianping.puma.core.sync.SyncTask;

/**
 * 查看已经创建的所有任务
 */
@Controller
@RequestMapping(method = RequestMethod.POST, produces = "application/json; charset=utf-8")
public class CreatedController {
    private static final Logger LOG = LoggerFactory.getLogger(CreatedController.class);
    @Autowired
    private SyncConfigService syncConfigService;
    @Autowired
    private SyncTaskService syncTaskService;

    private static final String errorMsg = "对不起，出了一点错误，请刷新页面试试。";
    private static final int PAGESIZE = 8;

    @RequestMapping(value = "/loadSyncConfigs", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadSyncConfigs(HttpSession session, HttpServletRequest request, Integer pageNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
            List<SyncConfig> syncConfigs = syncConfigService.findSyncConfigs(offset, PAGESIZE);
            Long totalSyncConfig = syncConfigService.countSyncConfigs();
            List<SyncTask> syncTasks = syncTaskService.findSyncTasksBySyncConfigId(getSyncConfigIds(syncConfigs));
//            
//            List<HashMap<String,Object>> resultMaps = new ArrayList<HashMap<String,Object>>();
//            for(SyncConfig syncConfig:syncConfigs){
//                for(SyncTask task:syncTasks){
//                    if(task.getSyncConfigId().equals(syncConfig.getId())){
//                        HashMap<String,Object> m = new HashMap<String, Object>();
//                        m.put("", syncConfig.getDest());
//                    }
//                }
//            }
            
            map.put("syncConfigs", syncConfigs);
            map.put("syncTasks", syncTasks);
            map.put("totalPage", totalSyncConfig / PAGESIZE + (totalSyncConfig % PAGESIZE == 0 ? 0 : 1));
            map.put("success", true);
        } catch (IllegalArgumentException e) {
            map.put("success", false);
            map.put("errorMsg", e.getMessage());
        } catch (Exception e) {
            map.put("success", false);
            map.put("errorMsg", errorMsg);
            LOG.error(e.getMessage(), e);
        }
        return GsonUtil.toJson(map);

    }

    private List<ObjectId> getSyncConfigIds(List<SyncConfig> syncConfigs) {
        List<ObjectId> list = new ArrayList<ObjectId>(syncConfigs.size());
        for (SyncConfig syncConfig : syncConfigs) {
            list.add(syncConfig.getId());
        }
        return list;
    }
}
