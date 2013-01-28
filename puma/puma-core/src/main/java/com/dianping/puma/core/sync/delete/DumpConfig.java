package com.dianping.puma.core.sync.delete;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;

import com.dianping.puma.core.sync.model.mapping.DatabaseMapping;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * dump规则：<br>
 * (1)如果目标没有数据库，则自动为目标数据库自动创建table，但database不自动创建。<br>
 * (2)允许db改名，table改名，不会自动修改字段名。 (3)要求table的结构(字段个数，对应字段的类型)一致。
 */
@Entity
public class DumpConfig {

    @Id
    private ObjectId id;
    private DumpSrc src;
    private DumpDest dest;

    private List<DatabaseMapping> databaseMappings;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public DumpSrc getSrc() {
        return src;
    }

    public void setSrc(DumpSrc src) {
        this.src = src;
    }

    public DumpDest getDest() {
        return dest;
    }

    public void setDest(DumpDest dest) {
        this.dest = dest;
    }

    public List<DatabaseMapping> getDatabaseMappings() {
        return databaseMappings;
    }

    public void setDatabaseMappings(List<DatabaseMapping> databaseMappings) {
        this.databaseMappings = databaseMappings;
    }

    @Override
    public String toString() {
        return "DumpConfig [id=" + id + ", src=" + src + ", dest=" + dest + ", databaseMappings=" + databaseMappings + "]";
    }

    public static class DumpSrc {
        private String host;
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
            return "Src [host=" + host + ", username=" + username + ", password=" + password + ", options=" + options + "]";
        }

    }

    public static class DumpDest {
        private String host;
        private String username;
        private String password;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
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
            return "Dest [host=" + host + ", username=" + username + ", password=" + password + "]";
        }

    }

    //    /**
    //     * Dump的映射关系。
    //     */
    //    public static class DumpRelation {
    //        private String srcDatabaseName;
    //        private String destDatabaseName;
    //        private List<String> srcTableNames;
    //        private List<String> destTableNames;
    //
    //        public String getSrcDatabaseName() {
    //            return srcDatabaseName;
    //        }
    //
    //        public void setSrcDatabaseName(String srcDatabaseName) {
    //            this.srcDatabaseName = srcDatabaseName;
    //        }
    //
    //        public String getDestDatabaseName() {
    //            return destDatabaseName;
    //        }
    //
    //        public void setDestDatabaseName(String destDatabaseName) {
    //            this.destDatabaseName = destDatabaseName;
    //        }
    //
    //        public List<String> getSrcTableNames() {
    //            return srcTableNames;
    //        }
    //
    //        public void setSrcTableNames(List<String> srcTableNames) {
    //            this.srcTableNames = srcTableNames;
    //        }
    //
    //        public List<String> getDestTableNames() {
    //            return destTableNames;
    //        }
    //
    //        public void setDestTableNames(List<String> destTableNames) {
    //            this.destTableNames = destTableNames;
    //        }
    //
    //        @Override
    //        public String toString() {
    //            return "DumpRelation [srcDatabaseName=" + srcDatabaseName + ", destDatabaseName=" + destDatabaseName
    //                    + ", srcTableNames=" + srcTableNames + ", destTableNames=" + destTableNames + "]";
    //        }
    //
    //    }
}
