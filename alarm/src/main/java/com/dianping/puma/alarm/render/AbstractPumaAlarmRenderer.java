package com.dianping.puma.alarm.render;

import com.dianping.puma.alarm.exception.PumaAlarmRenderException;
import com.dianping.puma.alarm.model.AlarmTemplate;
import com.dianping.puma.common.AbstractPumaLifeCycle;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by xiaotian.li on 16/3/25.
 * Email: lixiaotian07@gmail.com
 */
public abstract class AbstractPumaAlarmRenderer extends AbstractPumaLifeCycle implements PumaAlarmRenderer {

    protected AlarmTemplate template;

    protected AlarmTemplate generateAlarmTemplate(String propertiesFilePath) {
        try {
            Properties properties = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream(propertiesFilePath);
            properties.load(is);

            String titleTemplate = properties.getProperty("title");
            if (titleTemplate == null) {
                throw new NullPointerException("title template");
            } else {
                template.setTitleTemplate(titleTemplate);
            }

            String contentTemplate = properties.getProperty("content");
            if (contentTemplate == null) {
                throw new NullPointerException("content template");
            } else {
                template.setContentTemplate(contentTemplate);
            }

            return template;

        } catch (Throwable t) {
            throw new PumaAlarmRenderException("Failed to generate alarm template.", t);
        }
    }
}
