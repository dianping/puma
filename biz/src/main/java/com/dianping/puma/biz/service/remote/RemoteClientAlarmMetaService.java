package com.dianping.puma.biz.service.remote;

import com.dianping.puma.alarm.core.model.meta.EmailAlarmMeta;
import com.dianping.puma.alarm.core.model.meta.LogAlarmMeta;
import com.dianping.puma.alarm.core.model.meta.SmsAlarmMeta;
import com.dianping.puma.alarm.core.model.meta.WeChatAlarmMeta;
import com.dianping.puma.alarm.core.service.PumaClientAlarmMetaService;
import com.dianping.puma.common.convert.Converter;
import com.dianping.puma.biz.dao.ClientAlarmMetaDao;
import com.dianping.puma.biz.entity.ClientAlarmMetaEntity;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by xiaotian.li on 16/3/22.
 * Email: lixiaotian07@gmail.com
 */
public class RemoteClientAlarmMetaService implements PumaClientAlarmMetaService {

    private Converter converter;

    private ClientAlarmMetaDao clientAlarmMetaDao;

    @Override
    public EmailAlarmMeta findEmail(String clientName) {
        ClientAlarmMetaEntity entity = clientAlarmMetaDao.find(clientName);
        if (entity == null || !entity.isAlarmByEmail()) {
            return null;
        } else {
            List<String> emailRecipients = Lists.newArrayList(StringUtils.split(entity.getEmailRecipients(), ","));
            EmailAlarmMeta meta = converter.convert(entity, EmailAlarmMeta.class);
            meta.setEmailRecipients(emailRecipients);
            return meta;
        }
    }

    @Override
    public WeChatAlarmMeta findWeChat(String clientName) {
        ClientAlarmMetaEntity entity = clientAlarmMetaDao.find(clientName);
        if (entity == null || !entity.isAlarmByWeChat()) {
            return null;
        } else {
            List<String> weChatRecipients = Lists.newArrayList(StringUtils.split(entity.getWeChatRecipients(), ","));
            WeChatAlarmMeta meta = converter.convert(entity, WeChatAlarmMeta.class);
            meta.setWeChatRecipients(weChatRecipients);
            return meta;
        }
    }

    @Override
    public SmsAlarmMeta findSms(String clientName) {
        ClientAlarmMetaEntity entity = clientAlarmMetaDao.find(clientName);
        if (entity == null || !entity.isAlarmBySms()) {
            return null;
        } else {
            List<String> smsRecipients = Lists.newArrayList(StringUtils.split(entity.getSmsRecipients(), ","));
            SmsAlarmMeta meta = converter.convert(entity, SmsAlarmMeta.class);
            meta.setSmsRecipients(smsRecipients);
            return meta;
        }
    }

    @Override
    public LogAlarmMeta findLog(String clientName) {
        ClientAlarmMetaEntity entity = clientAlarmMetaDao.find(clientName);
        if (entity == null || !entity.isAlarmByLog()) {
            return null;
        } else {
            return converter.convert(entity, LogAlarmMeta.class);
        }
    }

    @Override
    public Map<String, EmailAlarmMeta> findEmailAll() {
        return null;
    }

    @Override
    public Map<String, WeChatAlarmMeta> findWeChatAll() {
        return null;
    }

    @Override
    public Map<String, SmsAlarmMeta> findSmsAll() {
        return null;
    }

    @Override
    public Map<String, LogAlarmMeta> findLogAll() {
        return null;
    }

    @Override
    public void replaceEmail(String clientName, EmailAlarmMeta meta) {

    }

    @Override
    public void replaceWeChat(String clientName, WeChatAlarmMeta meta) {

    }

    @Override
    public void replaceSms(String clientName, SmsAlarmMeta meta) {

    }

    @Override
    public void replaceLog(String clientName, LogAlarmMeta meta) {

    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public void setClientAlarmMetaDao(ClientAlarmMetaDao clientAlarmMetaDao) {
        this.clientAlarmMetaDao = clientAlarmMetaDao;
    }
}
