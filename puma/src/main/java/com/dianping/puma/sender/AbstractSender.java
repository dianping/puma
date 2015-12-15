package com.dianping.puma.sender;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.core.event.ChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSender implements Sender {
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractSender.class);

    private String name;

    private int maxTryTimes = 3;

    private boolean canMissEvent = false;

    private volatile boolean stopped = true;

    private final String MSG_SKIP = "[Miss]Send event failed for %d times. [servername=%s; current binlogfile=%s; current binlogpos=%d; next binlogpos=%d] ";

    private final String MSG_LOOP_FAILED = "[Can't Miss]Send event failed for %d times. [servername=%s; current binlogfile=%s; current binlogpos=%d; next binlogpos=%d] ";

    /**
     * @return the stop
     */
    public boolean isStop() {
        return stopped;
    }

    /**
     * @return the maxTryTimes
     */
    public int getMaxTryTimes() {
        return maxTryTimes;
    }

    /**
     * @param maxTryTimes the maxTryTimes to set
     */
    public void setMaxTryTimes(int maxTryTimes) {
        this.maxTryTimes = maxTryTimes;
    }

    /**
     * @return the canMissEvent
     */
    public boolean isCanMissEvent() {
        return canMissEvent;
    }

    /**
     * @param canMissEvent the canMissEvent to set
     */
    public void setCanMissEvent(boolean canMissEvent) {
        this.canMissEvent = canMissEvent;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dianping.puma.common.LifeCycle#start()
     */
    @Override
    public void start() {
        stopped = false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dianping.puma.common.LifeCycle#stop()
     */
    @Override
    public void stop() {
        stopped = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dianping.puma.sender.Sender#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void send(ChangedEvent event, PumaContext context) throws SenderException {
        long retryCount = 0;

        while (true) {
            if (isStop()) {
                break;
            }

            try {
                doSend(event, context);
                break;
            } catch (Exception e) {
                LOG.error("Send error!", e);

                if (retryCount++ > maxTryTimes) {
                    if (canMissEvent) {
                        LOG.error(String.format(MSG_SKIP, maxTryTimes, context.getPumaServerName(),
                                context.getBinlogFileName(), context.getBinlogStartPos(), context.getNextBinlogPos()));
                        return;
                    } else {
                        if (retryCount % 100 == 0) {
                            LOG.error(String.format(MSG_LOOP_FAILED, maxTryTimes, context.getPumaServerName(),
                                    context.getBinlogFileName(), context.getBinlogStartPos(), context.getNextBinlogPos()));
                        }
                    }
                }

                try {
                    Thread.sleep(((retryCount % 15) + 1) * 300);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    throw new SenderException("Interrupted", e1);
                }
            }
        }
    }

    protected abstract void doSend(ChangedEvent event, PumaContext context) throws SenderException;
}
