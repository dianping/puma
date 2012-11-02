package com.dianping.puma.core.sync;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DumpConfig {

    private Src src;
    private Dest dest;

    private Map<String, List<String>> databaseName2TableNames;

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

    public Map<String, List<String>> getDatabaseName2TableNames() {
        return databaseName2TableNames;
    }

    public void setDatabaseName2TableNames(Map<String, List<String>> databaseName2TableNames) {
        this.databaseName2TableNames = databaseName2TableNames;
    }

    @Override
    public String toString() {
        return "DumpConfig [src=" + src + ", dest=" + dest + ", databaseName2TableNames=" + databaseName2TableNames + "]";
    }

    public static class Src {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private List<String> options = Arrays.asList(new String[] { "--no-autocommit", " --disable-keys", "--extended-insert",
                "--quick", "--add-drop-database=false", "--add-drop-table=false", "--skip-add-locks",
                "--default-character-set=utf8", "--max_allowed_packet=16777216", " --net_buffer_length=16384", "-i",
                "--master-data=2", "--single-transaction" });

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

    }

    public static class Dest {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private String optionString;

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

        public String getOptionString() {
            return optionString;
        }

        public void setOptionString(String optionString) {
            this.optionString = optionString;
        }

    }

}
