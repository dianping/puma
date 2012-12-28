package com.dianping.puma.admin.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXParseException;

import com.dianping.puma.admin.bo.SyncXml;
import com.dianping.puma.admin.service.SyncConfigService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.admin.util.MongoUtils;
import com.dianping.puma.admin.util.SyncXmlParser;
import com.dianping.puma.core.sync.SyncConfig;

/**
 * TODO <br>
 * (1) 以create为整个controller，所有中间状态存放在session<br>
 * (2) 编写SyncTask的service <br>
 * (3) 保存binlog信息<br>
 * (4) pumaSyncServer的host的选择
 * (5) 创建同步任务，启动任务
 * 
 * @author wukezhu
 */
@Controller
@RequestMapping(method = RequestMethod.POST, produces = "application/json; charset=utf-8")
public class ModifyController {
    private static final Logger LOG = LoggerFactory.getLogger(ModifyController.class);
    @Autowired
    private SyncConfigService syncConfigService;

    private static final String errorMsg = "对不起，出了一点错误，请刷新页面试试。";
    private static final int PAGESIZE = 8;

//    @RequestMapping(value = "/loadSyncConfigs", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    @ResponseBody
//    public Object loadSyncConfigs(HttpSession session, HttpServletRequest request, Integer pageNum) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        try {
//            int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
//            List<SyncConfig> syncConfigs = syncConfigService.findSyncConfigs(offset, PAGESIZE);
//            Long totalSyncConfig = syncConfigService.countSyncConfigs();
//            map.put("syncConfigs", syncConfigs);
//            map.put("totalPage", totalSyncConfig / PAGESIZE + (totalSyncConfig % PAGESIZE == 0 ? 0 : 1));
//            map.put("success", true);
//        } catch (IllegalArgumentException e) {
//            map.put("success", false);
//            map.put("errorMsg", e.getMessage());
//        } catch (Exception e) {
//            map.put("success", false);
//            map.put("errorMsg", errorMsg);
//            LOG.error(e.getMessage(), e);
//        }
//        return GsonUtil.toJson(map);
//
//    }

    @RequestMapping(value = "/loadSyncXml", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object loadSyncXml(HttpSession session, HttpServletRequest request, String mergeId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String[] mergeIdSplits = StringUtils.split(mergeId, '_');
            int inc = Integer.parseInt(mergeIdSplits[0]);
            int machine = Integer.parseInt(mergeIdSplits[1]);
            int time = Integer.parseInt(mergeIdSplits[2]);
            ObjectId objectId = new ObjectId(time, machine, inc);
            //mergeId解析成ObjectId
            SyncXml syncXml = syncConfigService.findSyncXml(objectId);

            map.put("syncXml", syncXml);
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

    @RequestMapping(value = "/modifySyncXml", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object modifySyncXml(HttpSession session, HttpServletRequest request, String syncXmlString, String mergeId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //获取ObjectId
            ObjectId objectId = MongoUtils.mergeId2ObjectId(mergeId);
            //解析xml，得到SyncConfig
            SyncConfig syncConfig = SyncXmlParser.parse(syncXmlString);
            syncConfig.setId(objectId);
            LOG.info("receive sync: " + syncConfig);
            //保存修改
            syncConfigService.modifySyncConfig(syncConfig, syncXmlString);

            map.put("success", true);
        } catch (SAXParseException e) {
            map.put("success", false);
            map.put("errorMsg", "xml解析出错：" + e.getMessage());
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

}
