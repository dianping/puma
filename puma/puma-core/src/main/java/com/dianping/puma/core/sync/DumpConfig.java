package com.dianping.puma.core.sync;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * dump规则：<br>
 * (1)会为目标数据库自动创建table，但database不自动创建。<br>
 * (2)允许db改名，table改名。但要求table的结构(字段个数，对应字段的类型)一致，字段名可以不一致。
 */
@Entity
public class DumpConfig {

    @Id
    private ObjectId id;
    private Src src;
    private Dest dest;

    private List<DumpRelation> dumpRelations;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Src getSrc() {
        return src;
    }

    public void setSrc(Src src) {
        this.src = src;
    }

    public Dest getDest() {
        return dest;
    }

    public void setDest(Dest dest) {
        this.dest = dest;
    }

    public List<DumpRelation> getDumpRelations() {
        return dumpRelations;
    }

    public void setDumpRelations(List<DumpRelation> dumpRelations) {
        this.dumpRelations = dumpRelations;
    }

    @Override
    public String toString() {
        return "DumpConfig [id=" + id + ", src=" + src + ", dest=" + dest + ", dumpRelations=" + dumpRelations + "]";
    }

    public static class Src {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private List<String> options = Arrays.asList(new String[] { "--no-autocommit", " --disable-keys", "--quick",
                "--add-drop-database=false", "--add-drop-table=false", "--skip-add-locks", "--default-character-set=utf8",
                "--max_allowed_packet=16777216", " --net_buffer_length=16384", "-i", "--master-data=2", "--single-transaction" });

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        @Override
        public String toString() {
            return "Src [host=" + host + ", port=" + port + ", username=" + username + ", password=" + password + ", options="
                    + options + "]";
        }

    }

    public static class Dest {
        private String host;
        private Integer port;
        private String username;
        private String password;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "Dest [host=" + host + ", port=" + port + ", username=" + username + ", password=" + password + "]";
        }

    }

    /**
     * Dump的映射关系。
     */
    public static class DumpRelation {
        private String srcDatabaseName;
        private String destDatabaseName;
        private List<String> srcTableNames;
        private List<String> destTableNames;

        public String getSrcDatabaseName() {
            return srcDatabaseName;
        }

        public void setSrcDatabaseName(String srcDatabaseName) {
            this.srcDatabaseName = srcDatabaseName;
        }

        public String getDestDatabaseName() {
            return destDatabaseName;
        }

        public void setDestDatabaseName(String destDatabaseName) {
            this.destDatabaseName = destDatabaseName;
        }

        public List<String> getSrcTableNames() {
            return srcTableNames;
        }

        public void setSrcTableNames(List<String> srcTableNames) {
            this.srcTableNames = srcTableNames;
        }

        public List<String> getDestTableNames() {
            return destTableNames;
        }

        public void setDestTableNames(List<String> destTableNames) {
            this.destTableNames = destTableNames;
        }

        @Override
        public String toString() {
            return "DumpRelation [srcDatabaseName=" + srcDatabaseName + ", destDatabaseName=" + destDatabaseName
                    + ", srcTableNames=" + srcTableNames + ", destTableNames=" + destTableNames + "]";
        }

    }
}
