package com.dianping.puma.channel.page.acceptor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.puma.ComponentContainer;
import com.dianping.puma.common.SystemStatusContainer;
import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.filter.EventFilterChain;
import com.dianping.puma.filter.EventFilterChainFactory;
import com.dianping.puma.storage.EventChannel;
import com.dianping.puma.storage.EventStorage;
import com.dianping.puma.storage.exception.StorageException;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
    private static final Logger log = Logger.getLogger(Handler.class);

    protected void endCatTransaction() {
        try {
            // complete current transaction immediately (for abnormal case only)
            MessageManager manager = Cat.getManager();
            Transaction t1 = manager.getPeekTransaction();

            t1.setStatus(Message.SUCCESS);
            t1.complete();

            Transaction t2 = manager.getPeekTransaction();
            t2.setStatus(Message.SUCCESS);
            t2.complete();
        } catch (Exception e) {
            log.error("Cat failed.");
        }
    }

    @Override
    @PayloadMeta(Payload.class)
    @InboundActionMeta(name = "acceptor")
    public void handleInbound(Context ctx) throws ServletException, IOException {
        // display only, no action here
    }

    @Override
    @OutboundActionMeta(name = "acceptor")
    public void handleOutbound(Context ctx) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        Payload payload = ctx.getPayload();
        HttpServletResponse res = ctx.getHttpServletResponse();

        // status report
        SystemStatusContainer.instance.addClientStatus(payload.getClientName(), payload.getSeq(), payload.getTarget(),
                payload.isDml(), payload.isDdl(), payload.isNeedsTransactionMeta(), payload.getDatabaseTables(),
                payload.getCodecType());

        log.info("Client(" + payload.getClientName() + ") connected.");

        EventCodec codec = EventCodecFactory.createCodec(payload.getCodecType());
        EventFilterChain filterChain = EventFilterChainFactory.createEventFilterChain(payload.isDdl(), payload.isDml(),
                payload.isNeedsTransactionMeta(), payload.getDatabaseTables());

        res.setContentType("application/octet-stream");
        res.addHeader("Connection", "Keep-Alive");

        long seq = payload.getSeq();
        long serverId = payload.getServerId();
        String binlogFile = payload.getBinlog();
        long binlogPos = payload.getBinlogPos();
        long timeStamp = payload.getTimestamp();
        EventStorage storage = ComponentContainer.SPRING.lookup("storage-" + payload.getTarget(), EventStorage.class);
        EventChannel channel;
        try {
            channel = storage.getChannel(seq, serverId, binlogFile, binlogPos, timeStamp);
        } catch (StorageException e1) {
            log.error(e1.getMessage(), e1);
            throw new IOException(e1);
        }

        endCatTransaction();

        while (true) {
            try {
                filterChain.reset();
                ChangedEvent event = channel.next();
                if (filterChain.doNext(event)) {
                    byte[] data = codec.encode(event);
                    res.getOutputStream().write(ByteArrayUtils.intToByteArray(data.length));
                    res.getOutputStream().write(data);
                    res.getOutputStream().flush();
                    // status report
                    SystemStatusContainer.instance.updateClientSeq(payload.getClientName(), event.getSeq());
                }
            } catch (Exception e) {
                try {
                    Cat.getProducer().logError(e);
                } catch (Exception ex) {
                    log.error("Cat failed.");
                }
                SystemStatusContainer.instance.removeClient(payload.getClientName());
                log.info("Client(" + payload.getClientName() + ") failed. ", e);
                break;
            }
        }

        long end = System.currentTimeMillis();
        String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
        Cat.getProducer().logEvent("ChannelClosed", ipAddress, Message.SUCCESS, "duration=" + (end - start));
    }
}
