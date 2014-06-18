package com.dianping.puma.channel.page.acceptor;

import com.dianping.puma.channel.ChannelPage;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ChannelPage, Action> {
    private ChannelPage m_page;

    @FieldMeta("op")
    private Action      m_action;

    @FieldMeta("seq")
    private long        m_seq       = -1;

    @FieldMeta("ddl")
    private boolean     m_ddl;

    @FieldMeta("dml")
    private boolean     m_dml       = true;

    @FieldMeta("ts")
    private boolean     m_needsTransactionMeta; // needs transaction meta info?

    @FieldMeta("codec")
    private String      m_codecType = "json";

    @FieldMeta("name")
    private String      m_clientName;

    @FieldMeta("target")
    private String      m_target;
    @FieldMeta("serverId")
    private long        m_serverId  = -1L;
    @FieldMeta("binlog")
    private String      m_binlog    = null;
    @FieldMeta("binlogPos")
    private long        m_binlogPos = -1L;
    @FieldMeta("timestamp")
    private long        m_timestamp = -1L;

    @FieldMeta("dt")
    private String[]    m_databaseTables;

    public long getTimestamp() {
        return m_timestamp;
    }

    @Override
    public Action getAction() {
        return m_action;
    }

    public String getClientName() {
        return m_clientName;
    }

    public long getServerId() {
        return m_serverId;
    }

    public String getBinlog() {
        return m_binlog;
    }

    public long getBinlogPos() {
        return m_binlogPos;
    }

    public String getCodecType() {
        return m_codecType;
    }

    public String[] getDatabaseTables() {
        return m_databaseTables;
    }

    @Override
    public ChannelPage getPage() {
        return m_page;
    }

    public long getSeq() {
        return m_seq;
    }

    public String getTarget() {
        return m_target;
    }

    public boolean isDdl() {
        return m_ddl;
    }

    public boolean isDml() {
        return m_dml;
    }

    public boolean isNeedsTransactionMeta() {
        return m_needsTransactionMeta;
    }

    public void setAction(Action action) {
        m_action = action;
    }

    public void setClientName(String clientName) {
        m_clientName = clientName;
    }

    public void setCodecType(String codecType) {
        m_codecType = codecType;
    }

    public void setDatabaseTables(String[] databaseTables) {
        m_databaseTables = databaseTables;
    }

    public void setDdl(boolean ddl) {
        m_ddl = ddl;
    }

    public void setDml(boolean dml) {
        m_dml = dml;
    }

    public void setNeedsTransactionMeta(boolean needsTransactionMeta) {
        m_needsTransactionMeta = needsTransactionMeta;
    }

    @Override
    public void setPage(String page) {
        m_page = ChannelPage.getByName(page, ChannelPage.ACCEPTOR);
    }

    public void setSeq(long seq) {
        m_seq = seq;
    }

    public void setTarget(String target) {
        m_target = target;
    }

    @Override
    public void validate(ActionContext<?> ctx) {
        if (m_action == null) {
            m_action = Action.VIEW;
        }
    }
}
