package com.dianping.puma.alarm.core.monitor.render;

import com.dianping.puma.common.AbstractPumaLifeCycle;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public abstract class AbstractPumaAlarmRenderer extends AbstractPumaLifeCycle implements PumaAlarmRenderer {

    protected String titleTemplate;

    protected String contentTemplate;

    public void setTitleTemplate(String titleTemplate) {
        this.titleTemplate = titleTemplate;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }
}
