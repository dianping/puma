package com.dianping.puma.admin.web;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.puma.admin.service.DumpActionService;
import com.dianping.puma.admin.service.DumpActionStateService;
import com.dianping.puma.admin.service.MysqlConfigService;
import com.dianping.puma.admin.service.PumaSyncServerConfigService;
import com.dianping.puma.admin.service.SyncTaskActionService;
import com.dianping.puma.admin.service.SyncTaskActionStateService;
import com.dianping.puma.admin.util.GsonUtil;
import com.dianping.puma.core.sync.model.action.SyncTaskAction;
import com.dianping.puma.core.sync.model.action.SyncTaskActionState;
import com.dianping.puma.core.sync.model.config.MysqlConfig;
import com.dianping.puma.core.sync.model.config.PumaSyncServerConfig;
import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.dianping.puma.core.sync.model.mapping.DumpMapping;
import com.dianping.puma.core.sync.model.mapping.MysqlMapping;
import com.dianping.puma.core.sync.model.mapping.TableMapping;

/**
 * TODO <br>
 * (1) 以create为整个controller，所有中间状态存放在session<br>
 * (2) 编写SyncTask的service <br>
 * (3) 保存binlog信息<br>
 * (4) pumaSyncServer的host的选择 (5) 创建同步任务，启动任务
 * 
 * @author wukezhu
 */
@Controller
public class ModifyController {
    private static final Logger LOG = LoggerFactory.getLogger(ModifyController.class);
    @Autowired
    private MysqlConfigService mysqlConfigService;
    @Autowired
    private DumpActionService dumpActionService;
    @Autowired
    private DumpActionStateService dumpActionStateService;
    @Autowired
    private PumaSyncServerConfigService pumaSyncServerConfigService;
    @Autowired
    private SyncTaskActionService syncTaskActionService;
    @Autowired
    private SyncTaskActionStateService syncTaskActionStateService;

    private static final String errorMsg = "对不起，出了一点错误，请刷新页面试试。";
    private static final int PAGESIZE = 8;

    @RequestMapping(value = { "/modify" })
    public ModelAndView modify(HttpServletRequest request, HttpServletResponse response) {
        return modify0(request, response, 1);
    }

    @RequestMapping(value = { "/modify/{pageNum}" })
    public ModelAndView modify0(HttpServletRequest request, HttpServletResponse response, @PathVariable("pageNum") Integer pageNum) {
        Map<String, Object> map = new HashMap<String, Object>();
        //        System.out.println(syncConfigService.find());
        int offset = pageNum == null ? 0 : (pageNum - 1) * PAGESIZE;
        List<SyncTaskAction> syncTaskActions = syncTaskActionService.find(offset, PAGESIZE);
        map.put("syncTaskActions", syncTaskActions);
        map.put("modifyActive", "active");
        map.put("subPath", "view");
        map.put("path", "modify");
        return new ModelAndView("main/container", map);
    }

    /**
     * 修改SyncTaskActionState的页面
     */
    @RequestMapping(value = "/modify/action/{id}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public ModelAndView action(HttpSession session, @PathVariable("id") Long actionId) {
        Map<String, Object> map = new HashMap<String, Object>();
        SyncTaskAction action = this.syncTaskActionService.find(actionId);
        SyncTaskActionState state = this.syncTaskActionStateService.find(actionId);
        map.put("action", action);
        map.put("state", state);
        map.put("modifyActive", "active");
        map.put("path", "modify");
        map.put("subPath", "step1");
        return new ModelAndView("main/container", map);
    }

    @RequestMapping(value = "/modify/action/{id}/step1Save", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    public Object step1Save(HttpSession session, @PathVariable("id") Long actionId, String[] databaseFrom, String[] databaseTo,
                            String[] tableFrom, String[] tableTo, Integer count[]) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
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
            MysqlMapping mysqlMapping = new MysqlMapping();
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
                mysqlMapping.addDatabase(database);
            }
            //对比新的mapping和现有的mapping
            //查询出SyncTaskAction
            SyncTaskAction syncTaskAction = this.syncTaskActionService.find(actionId);
            MysqlMapping oldMysqlMapping = syncTaskAction.getMysqlMapping();
            MysqlMapping additionalMysqlMapping = this.syncTaskActionService.compare(oldMysqlMapping, mysqlMapping);

            //保存到session
            session.setAttribute("mysqlMapping", mysqlMapping);
            session.setAttribute("additionalMysqlMapping", additionalMysqlMapping);

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

    @RequestMapping(method = RequestMethod.GET, value = { "/modify/action/{id}/step2" })
    public ModelAndView step2(HttpSession session, @PathVariable("id") Long actionId) throws SQLException {
        Map<String, Object> map = new HashMap<String, Object>();
        //从session拿出srcMysql，destMysql查询mysql配置
        SyncTaskAction syncTaskAction = this.syncTaskActionService.find(actionId);
        String srcMysqlName = syncTaskAction.getSrcMysqlName();
        MysqlConfig srcMysqlConfig = this.mysqlConfigService.find(srcMysqlName);
        String destMysqlName = syncTaskAction.getDestMysqlName();
        MysqlConfig destMysqlConfig = this.mysqlConfigService.find(destMysqlName);
        //查询所有syncServer
        List<PumaSyncServerConfig> syncServerConfigs = pumaSyncServerConfigService.findAll();
        //从会话中取出保存的mysqlMapping，计算出dumpMapping
        MysqlMapping additionalMysqlMapping = (MysqlMapping) session.getAttribute("additionalMysqlMapping");
        DumpMapping dumpMapping = this.syncTaskActionService.convertMysqlMappingToDumpMapping(srcMysqlConfig.getHosts().get(0),
                additionalMysqlMapping);
        session.setAttribute("dumpMapping", dumpMapping);

        map.put("srcMysqlConfig", srcMysqlConfig);
        map.put("destMysqlConfig", destMysqlConfig);
        map.put("syncServerConfigs", syncServerConfigs);
        map.put("dumpMapping", dumpMapping);
        map.put("modifyActive", "active");
        map.put("path", "modify");
        map.put("subPath", "step2");
        return new ModelAndView("main/container", map);
    }

}
