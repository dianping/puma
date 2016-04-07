package com.dianping.puma.portal.model;

import java.util.List;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DashboardModel {

    private List<String> serverLabel;
    private List<String> serverColor;
    private List<Integer> serverValue;

    private List<String> taskLabel;
    private List<String> taskColor;
    private List<Integer> taskValue;

    private List<String> clientLabel;
    private List<String> clientColor;
    private List<Integer> clientValue;

    private List<Server> servers;
    private List<Task> tasks;
    private List<Client> clients;

    private List<Server> serverQps;
    private List<Task> taskQps;
    private List<Client> clientQps;

    public List<Server> getServers() {
        return servers;
    }

    public DashboardModel setServers(List<Server> servers) {
        this.servers = servers;
        return this;
    }

    public List<String> getServerLabel() {
        return serverLabel;
    }

    public DashboardModel setServerLabel(List<String> serverLabel) {
        this.serverLabel = serverLabel;
        return this;
    }

    public List<Integer> getServerValue() {
        return serverValue;
    }

    public DashboardModel setServerValue(List<Integer> serverValue) {
        this.serverValue = serverValue;
        return this;
    }

    public List<String> getTaskLabel() {
        return taskLabel;
    }

    public DashboardModel setTaskLabel(List<String> taskLabel) {
        this.taskLabel = taskLabel;
        return this;
    }

    public List<Integer> getTaskValue() {
        return taskValue;
    }

    public DashboardModel setTaskValue(List<Integer> taskValue) {
        this.taskValue = taskValue;
        return this;
    }

    public List<String> getClientLabel() {
        return clientLabel;
    }

    public DashboardModel setClientLabel(List<String> clientLabel) {
        this.clientLabel = clientLabel;
        return this;
    }

    public List<Integer> getClientValue() {
        return clientValue;
    }

    public DashboardModel setClientValue(List<Integer> clientValue) {
        this.clientValue = clientValue;
        return this;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public DashboardModel setTasks(List<Task> tasks) {
        this.tasks = tasks;
        return this;
    }

    public List<Client> getClients() {
        return clients;
    }

    public DashboardModel setClients(List<Client> clients) {
        this.clients = clients;
        return this;
    }

    public List<String> getServerColor() {
        return serverColor;
    }

    public DashboardModel setServerColor(List<String> serverColor) {
        this.serverColor = serverColor;
        return this;
    }

    public List<String> getTaskColor() {
        return taskColor;
    }

    public DashboardModel setTaskColor(List<String> taskColor) {
        this.taskColor = taskColor;
        return this;
    }

    public List<String> getClientColor() {
        return clientColor;
    }

    public DashboardModel setClientColor(List<String> clientColor) {
        this.clientColor = clientColor;
        return this;
    }

    public List<Server> getServerQps() {
        return serverQps;
    }

    public DashboardModel setServerQps(List<Server> serverQps) {
        this.serverQps = serverQps;
        return this;
    }

    public List<Task> getTaskQps() {
        return taskQps;
    }

    public DashboardModel setTaskQps(List<Task> taskQps) {
        this.taskQps = taskQps;
        return this;
    }

    public List<Client> getClientQps() {
        return clientQps;
    }

    public DashboardModel setClientQps(List<Client> clientQps) {
        this.clientQps = clientQps;
        return this;
    }

    public static class Server {
        private String name;
        private double load;
        private int qps;

        public String getName() {
            return name;
        }

        public Server setName(String name) {
            this.name = name;
            return this;
        }

        public double getLoad() {
            return load;
        }

        public Server setLoad(double load) {
            this.load = load;
            return this;
        }

        public int getQps() {
            return qps;
        }

        public Server setQps(int qps) {
            this.qps = qps;
            return this;
        }
    }

    public static class Task {
        private String name;
        private long delay;
        private int qps;

        public String getName() {
            return name;
        }

        public Task setName(String name) {
            this.name = name;
            return this;
        }

        public long getDelay() {
            return delay;
        }

        public Task setDelay(long delay) {
            this.delay = delay;
            return this;
        }

        public int getQps() {
            return qps;
        }

        public Task setQps(int qps) {
            this.qps = qps;
            return this;
        }
    }

    public static class Client {
        private String name;
        private long delay;
        private int qps;

        public String getName() {
            return name;
        }

        public Client setName(String name) {
            this.name = name;
            return this;
        }

        public long getDelay() {
            return delay;
        }

        public Client setDelay(long delay) {
            this.delay = delay;
            return this;
        }

        public int getQps() {
            return qps;
        }

        public Client setQps(int qps) {
            this.qps = qps;
            return this;
        }
    }
}
