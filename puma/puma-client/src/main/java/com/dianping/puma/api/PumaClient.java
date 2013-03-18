package com.dianping.puma.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.dianping.puma.core.codec.EventCodec;
import com.dianping.puma.core.codec.EventCodecFactory;
import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.util.ByteArrayUtils;
import com.dianping.puma.core.util.PumaThreadUtils;
import com.dianping.puma.core.util.StreamUtils;

public class PumaClient {
    private static final Logger log = Logger.getLogger(PumaClient.class);
    private Configuration config;
    private EventListener eventListener;
    private volatile boolean active = false;
    private SeqFileHolder seqFileHolder;
    private EventCodec codec;
    private Thread subscribeThread;

    //    private volatile boolean done = false;
    //    private HttpURLConnection connection;

    public SeqFileHolder getSeqFileHolder() {
        return seqFileHolder;
    }

    public PumaClient(Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("Config can't be null!");
        }

        this.config = config;
        this.seqFileHolder = new MMapBasedSeqFileHolder(config);
        codec = EventCodecFactory.createCodec(config.getCodecType());
    }

    public void register(EventListener listener) {
        this.eventListener = listener;
    }

    public void stop() {
        active = false;
        if (subscribeThread != null) {
            subscribeThread.interrupt();
        }
    }

    public void start() {
        config.validate();

        subscribeThread = PumaThreadUtils.createThread(new PumaClientTask(), "PumaClientSub", false);

        active = true;
        subscribeThread.start();
    }

    private boolean checkStop() {
        if (!active) {
            log.info("Puma client[" + config.getName() + "] stopped.");
            return true;
        }
        if (Thread.currentThread().isInterrupted()) {
            log.info("Puma client[" + config.getName() + "] stopped since interrupted.");
            return true;
        }

        return false;
    }

    private InputStream connect() {
        try {
            final URL url = new URL(config.buildUrl());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(3000);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Cache-Control", "no-cache");

            PrintWriter out = new PrintWriter(connection.getOutputStream());

            String requestParams = config.buildRequestParamString(seqFileHolder.getSeq());

            out.print(requestParams);

            out.close();

            eventListener.onConnected();

            return connection.getInputStream();

        } catch (Exception ex) {
            log.error("Puma client[" + config.getName() + "] Connect to puma server failed. " + config, ex);
            eventListener.onConnectException(ex);
        }

        return null;

    }

    private ChangedEvent readEvent(InputStream is) throws IOException {
        byte[] lengthArray = new byte[4];
        StreamUtils.readFully(is, lengthArray, 0, 4);
        int length = ByteArrayUtils.byteArrayToInt(lengthArray, 0, 4);
        byte[] data = new byte[length];
        StreamUtils.readFully(is, data, 0, length);
        return codec.decode(data);
    }

    private class PumaClientTask implements Runnable {

        @Override
        public void run() {

            // reconnect while there is some connection problem
            while (true) {
                InputStream is = null;

                if (checkStop()) {
                    break;
                }

                try {

                    is = connect();

                    // reconnect case
                    if (is == null) {
                        Thread.sleep(100);
                        log.info("Puma client[" + config.getName() + "] reconnecting...");
                        continue;
                    }

                    // loop read event from the input stream
                    while (true) {
                        if (checkStop()) {
                            break;
                        }

                        ChangedEvent event = readEvent(is);

                        boolean listenerCallSuccess = true;

                        // call event listener until success
                        while (true) {
                            if (checkStop()) {
                                break;
                            }

                            try {
                                eventListener.onEvent(event);
                                listenerCallSuccess = true;
                                break;
                            } catch (Exception e) {
                                log.warn("Puma client[" + config.getName() + "] Exception occurs in eventListerner. Event: " + event, e);

                                if (eventListener.onException(event, e)) {
                                    log.warn("Puma client[" + config.getName() + "] Event(" + event + ") skipped. ");
                                    eventListener.onSkipEvent(event);
                                    listenerCallSuccess = true;
                                    break;
                                } else {
                                    listenerCallSuccess = false;
                                }
                            }
                        }

                        // Maybe not because of the success of last event
                        // listener's run, but the stop command that system
                        // received. We shouldn't save in this case.
                        if (listenerCallSuccess) {
                            seqFileHolder.saveSeq(event.getSeq());
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.warn("Puma client[" + config.getName() + "] Connection problem occurs." + e);
                    log.warn("Puma client[" + config.getName() + "] reconnecting...");
                    eventListener.onConnectException(e);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }
}
