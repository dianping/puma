package com.dianping.puma.portal.dto;

/**
 * Created by xiaotian.li on 16/2/22.
 * Email: lixiaotian07@gmail.com
 */
public class PumaClientDto {

    /** Name. */
    private String clientName;

    /** Additional information. */
    private String groupName;

    /** Configuration. */
    private String databaseName;
    private String tableRegex;
    private boolean dml;
    private boolean ddl;

    /** Connect information. */
    private String clientAddress;
    private String serverAddress;

    /** Acknowledge position. */
    private long serverId;
    private String filename;
    private long position;
    private long timestamp;

    /** Alarm benchmark */
    private boolean pullTimeDelayAlarm;
    private long minPullTimeDelayInSecond;
    private long maxPullTimeDelayInSecond;
    private boolean pushTimeDelayAlarm;
    private long minPushTimeDelayInSecond;
    private long maxPushTimeDelayInSecond;

    /** Alarm strategy. */
    private boolean alarmByLog;
    private boolean alarmByEmail;
    private boolean alarmBySms;
    private boolean alarmByWeChat;
    private String recipients;

    /** Alarm meta. */
    private boolean noAlarm;
    private boolean linearAlarm;
    private long linearAlarmIntervalInSecond;
    private boolean exponentialAlarm;
    private long minExponentialAlarmIntervalInSecond;
    private long maxExponentialAlarmIntervalInSecond;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableRegex() {
        return tableRegex;
    }

    public void setTableRegex(String tableRegex) {
        this.tableRegex = tableRegex;
    }

    public Boolean getDml() {
        return dml;
    }

    public void setDml(Boolean dml) {
        this.dml = dml;
    }

    public Boolean getDdl() {
        return ddl;
    }

    public void setDdl(Boolean ddl) {
        this.ddl = ddl;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDml() {
        return dml;
    }

    public void setDml(boolean dml) {
        this.dml = dml;
    }

    public boolean isDdl() {
        return ddl;
    }

    public void setDdl(boolean ddl) {
        this.ddl = ddl;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isPullTimeDelayAlarm() {
        return pullTimeDelayAlarm;
    }

    public void setPullTimeDelayAlarm(boolean pullTimeDelayAlarm) {
        this.pullTimeDelayAlarm = pullTimeDelayAlarm;
    }

    public long getMinPullTimeDelayInSecond() {
        return minPullTimeDelayInSecond;
    }

    public void setMinPullTimeDelayInSecond(long minPullTimeDelayInSecond) {
        this.minPullTimeDelayInSecond = minPullTimeDelayInSecond;
    }

    public long getMaxPullTimeDelayInSecond() {
        return maxPullTimeDelayInSecond;
    }

    public void setMaxPullTimeDelayInSecond(long maxPullTimeDelayInSecond) {
        this.maxPullTimeDelayInSecond = maxPullTimeDelayInSecond;
    }

    public boolean isPushTimeDelayAlarm() {
        return pushTimeDelayAlarm;
    }

    public void setPushTimeDelayAlarm(boolean pushTimeDelayAlarm) {
        this.pushTimeDelayAlarm = pushTimeDelayAlarm;
    }

    public long getMinPushTimeDelayInSecond() {
        return minPushTimeDelayInSecond;
    }

    public void setMinPushTimeDelayInSecond(long minPushTimeDelayInSecond) {
        this.minPushTimeDelayInSecond = minPushTimeDelayInSecond;
    }

    public long getMaxPushTimeDelayInSecond() {
        return maxPushTimeDelayInSecond;
    }

    public void setMaxPushTimeDelayInSecond(long maxPushTimeDelayInSecond) {
        this.maxPushTimeDelayInSecond = maxPushTimeDelayInSecond;
    }

    public boolean isAlarmByLog() {
        return alarmByLog;
    }

    public void setAlarmByLog(boolean alarmByLog) {
        this.alarmByLog = alarmByLog;
    }

    public boolean isAlarmByEmail() {
        return alarmByEmail;
    }

    public void setAlarmByEmail(boolean alarmByEmail) {
        this.alarmByEmail = alarmByEmail;
    }

    public boolean isAlarmBySms() {
        return alarmBySms;
    }

    public void setAlarmBySms(boolean alarmBySms) {
        this.alarmBySms = alarmBySms;
    }

    public boolean isAlarmByWeChat() {
        return alarmByWeChat;
    }

    public void setAlarmByWeChat(boolean alarmByWeChat) {
        this.alarmByWeChat = alarmByWeChat;
    }

    public String getRecipients() {
        return recipients;
    }

    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    public boolean isNoAlarm() {
        return noAlarm;
    }

    public void setNoAlarm(boolean noAlarm) {
        this.noAlarm = noAlarm;
    }

    public boolean isLinearAlarm() {
        return linearAlarm;
    }

    public void setLinearAlarm(boolean linearAlarm) {
        this.linearAlarm = linearAlarm;
    }

    public long getLinearAlarmIntervalInSecond() {
        return linearAlarmIntervalInSecond;
    }

    public void setLinearAlarmIntervalInSecond(long linearAlarmIntervalInSecond) {
        this.linearAlarmIntervalInSecond = linearAlarmIntervalInSecond;
    }

    public boolean isExponentialAlarm() {
        return exponentialAlarm;
    }

    public void setExponentialAlarm(boolean exponentialAlarm) {
        this.exponentialAlarm = exponentialAlarm;
    }

    public long getMinExponentialAlarmIntervalInSecond() {
        return minExponentialAlarmIntervalInSecond;
    }

    public void setMinExponentialAlarmIntervalInSecond(long minExponentialAlarmIntervalInSecond) {
        this.minExponentialAlarmIntervalInSecond = minExponentialAlarmIntervalInSecond;
    }

    public long getMaxExponentialAlarmIntervalInSecond() {
        return maxExponentialAlarmIntervalInSecond;
    }

    public void setMaxExponentialAlarmIntervalInSecond(long maxExponentialAlarmIntervalInSecond) {
        this.maxExponentialAlarmIntervalInSecond = maxExponentialAlarmIntervalInSecond;
    }
}
