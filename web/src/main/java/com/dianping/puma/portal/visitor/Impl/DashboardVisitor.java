package com.dianping.puma.portal.visitor.Impl;

import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.portal.model.ChartColors;
import com.dianping.puma.portal.model.DashboardModel;
import com.dianping.puma.portal.model.PumaServerStatusDto;
import com.dianping.puma.portal.visitor.PumaStatusVisitor;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dozer @ 2015-11
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class DashboardVisitor implements PumaStatusVisitor {

    private TreeMap<Double, AtomicInteger> serverLoadAnalytics = new TreeMap<Double, AtomicInteger>();
    private TreeMap<Long, AtomicInteger> taskDelayAnalytics = new TreeMap<Long, AtomicInteger>();
    private TreeMap<Long, AtomicInteger> clientDelayAnalytics = new TreeMap<Long, AtomicInteger>();

    private TreeSet<PumaServerStatusDto> serverQpsRank = new TreeSet<PumaServerStatusDto>(new Comparator<PumaServerStatusDto>() {
        @Override
        public int compare(PumaServerStatusDto o1, PumaServerStatusDto o2) {
            int result = Ints.compare(o2.getStoreQps(), o1.getStoreQps());
            if (result != 0) {
                return result;
            }
            return Objects.equal(o1, o2) ? 0 : 1;
        }
    });

    private TreeSet<PumaServerStatusDto.Server> taskQpsRank = new TreeSet<PumaServerStatusDto.Server>(new Comparator<PumaServerStatusDto.Server>() {
        @Override
        public int compare(PumaServerStatusDto.Server o1, PumaServerStatusDto.Server o2) {
            int result = Ints.compare(o2.getStoreQps(), o1.getStoreQps());
            if (result != 0) {
                return result;
            }
            return Objects.equal(o1, o2) ? 0 : 1;
        }
    });

    private TreeSet<PumaServerStatusDto.Client> clientQpsRank = new TreeSet<PumaServerStatusDto.Client>(new Comparator<PumaServerStatusDto.Client>() {
        @Override
        public int compare(PumaServerStatusDto.Client o1, PumaServerStatusDto.Client o2) {
            int result = Ints.compare(o2.getFetchQps(), o1.getFetchQps());
            if (result != 0) {
                return result;
            }
            return Objects.equal(o1, o2) ? 0 : 1;
        }
    });


    private TreeSet<PumaServerStatusDto> serverLoadRank = new TreeSet<PumaServerStatusDto>(new Comparator<PumaServerStatusDto>() {
        @Override
        public int compare(PumaServerStatusDto o1, PumaServerStatusDto o2) {
            int result = Double.compare(o2.getLoad(), o1.getLoad());
            if (result != 0) {
                return result;
            }
            return Objects.equal(o1, o2) ? 0 : 1;
        }
    });

    private static final Comparator<BinlogInfo> binlogInfoComparator = new Comparator<BinlogInfo>() {
        @Override
        public int compare(BinlogInfo o1, BinlogInfo o2) {
            long left = o1 == null ? 0 : o1.getTimestamp();
            long right = o2 == null ? 0 : o2.getTimestamp();

            if (left == right) {
                return 0;
            }
            return left > right ? 1 : -1;
        }
    };

    TreeSet<PumaServerStatusDto.Server> taskDelayRank = new TreeSet<PumaServerStatusDto.Server>(new Comparator<PumaServerStatusDto.Server>() {
        @Override
        public int compare(PumaServerStatusDto.Server o1, PumaServerStatusDto.Server o2) {
            int result = binlogInfoComparator.compare(o1.getBinlogInfo(), o2.getBinlogInfo());
            if (result == 0) {
                return Objects.equal(o1, o2) ? 0 : 1;
            }
            return result;
        }
    });
    TreeSet<PumaServerStatusDto.Client> clientDelayRank = new TreeSet<PumaServerStatusDto.Client>(new Comparator<PumaServerStatusDto.Client>() {
        @Override
        public int compare(PumaServerStatusDto.Client o1, PumaServerStatusDto.Client o2) {
            int result = binlogInfoComparator.compare(o1.getAckBinlogInfo(), o2.getAckBinlogInfo());
            if (result == 0) {
                return Objects.equal(o1, o2) ? 0 : 1;
            }
            return result;
        }
    });

    public DashboardVisitor() {
        serverLoadAnalytics.put(0.5, new AtomicInteger(0));
        serverLoadAnalytics.put(1., new AtomicInteger(0));
        serverLoadAnalytics.put(2., new AtomicInteger(0));
        serverLoadAnalytics.put(5., new AtomicInteger(0));
        serverLoadAnalytics.put(Double.MAX_VALUE, new AtomicInteger(0));

        taskDelayAnalytics.put(5L, new AtomicInteger(0));
        taskDelayAnalytics.put(10L, new AtomicInteger(0));
        taskDelayAnalytics.put(60L, new AtomicInteger(0));
        taskDelayAnalytics.put(10 * 60L, new AtomicInteger(0));
        taskDelayAnalytics.put(Long.MAX_VALUE, new AtomicInteger(0));

        clientDelayAnalytics.put(5L, new AtomicInteger(0));
        clientDelayAnalytics.put(10L, new AtomicInteger(0));
        clientDelayAnalytics.put(60L, new AtomicInteger(0));
        clientDelayAnalytics.put(10 * 60L, new AtomicInteger(0));
        clientDelayAnalytics.put(Long.MAX_VALUE, new AtomicInteger(0));
    }


    @Override
    public void visit(Collection<PumaServerStatusDto> root) {
        for (PumaServerStatusDto item : root) {
            visit(item);
        }
    }

    @Override
    public void visit(PumaServerStatusDto item) {
        serverLoadAnalytics.ceilingEntry(item.getLoad()).getValue().incrementAndGet();
        serverLoadRank.add(item);
        serverQpsRank.add(item);

        for (PumaServerStatusDto.Server server : item.getServers().values()) {
            server.setServer(item.getName());
            visit(item, server);
        }
        for (PumaServerStatusDto.Client client : item.getClients().values()) {
            visit(item, client);
        }
    }

    @Override
    public void visit(PumaServerStatusDto item, PumaServerStatusDto.Client client) {
        client.setAckDelay(getDelay(item.getGenerateTime(), client.getAckBinlogInfo()));

        clientDelayAnalytics.ceilingEntry(client.getAckDelay())
                .getValue().incrementAndGet();
        clientDelayRank.add(client);
        clientQpsRank.add(client);
    }

    @Override
    public void visit(PumaServerStatusDto item, PumaServerStatusDto.Server task) {
        task.setDelay(getDelay(item.getGenerateTime(), task.getBinlogInfo()));
        taskDelayAnalytics.ceilingEntry(task.getDelay())
                .getValue().incrementAndGet();
        taskDelayRank.add(task);
        taskQpsRank.add(task);
    }

    protected long getDelay(long time, BinlogInfo binlogInfo) {
        return time / 1000 - (binlogInfo == null ? 0 : binlogInfo.getTimestamp());
    }

    public DashboardModel getDashboardModel() {
        DashboardModel model = new DashboardModel();

        model.setServers(FluentIterable.from(serverLoadRank).limit(8).transform(new Function<PumaServerStatusDto, DashboardModel.Server>() {
            @Override
            public DashboardModel.Server apply(PumaServerStatusDto input) {
                return new DashboardModel.Server().setName(input.getName()).setLoad(input.getLoad());
            }
        }).toList());
        model.setServerLabel(Lists.newArrayList("[0,0.5]", "(0.5,1]", "(1,2]", "(2,5]", "(5,+∞)"));
        model.setServerColor(Lists.newArrayList(ChartColors.Green, ChartColors.Blue, ChartColors.LightGrey, ChartColors.Yellow, ChartColors.Red));
        model.setServerValue(FluentIterable.from(serverLoadAnalytics.values()).transform(new Function<AtomicInteger, Integer>() {
            @Override
            public Integer apply(AtomicInteger input) {
                return input.get();
            }
        }).toList());

        model.setTasks(FluentIterable.from(taskDelayRank).limit(8).transform(new Function<PumaServerStatusDto.Server, DashboardModel.Task>() {
            @Override
            public DashboardModel.Task apply(PumaServerStatusDto.Server input) {
                return new DashboardModel.Task().setName(String.format("%s-%s", input.getServer(), input.getName())).setDelay(input.getDelay());
            }
        }).toList());
        model.setTaskLabel(Lists.newArrayList("[0秒,5秒]", "(5秒,10秒]", "(10秒,1分钟]", "(1分钟,10分钟]", "(10分钟,+∞)"));
        model.setTaskColor(Lists.newArrayList(ChartColors.Green, ChartColors.Blue, ChartColors.LightGrey, ChartColors.Yellow, ChartColors.Red));
        model.setTaskValue(FluentIterable.from(taskDelayAnalytics.values()).transform(new Function<AtomicInteger, Integer>() {
            @Override
            public Integer apply(AtomicInteger input) {
                return input.get();
            }
        }).toList());

        model.setClients(FluentIterable.from(clientDelayRank).limit(8).transform(new Function<PumaServerStatusDto.Client, DashboardModel.Client>() {
            @Override
            public DashboardModel.Client apply(PumaServerStatusDto.Client input) {
                return new DashboardModel.Client().setName(input.getName()).setDelay(input.getAckDelay());
            }
        }).toList());
        model.setClientLabel(Lists.newArrayList("[0秒,5秒]", "(5秒,10秒]", "(10秒,1分钟]", "(1分钟,10分钟]", "(10分钟,+∞)"));
        model.setClientColor(Lists.newArrayList(ChartColors.Green, ChartColors.Blue, ChartColors.LightGrey, ChartColors.Yellow, ChartColors.Red));
        model.setClientValue(FluentIterable.from(clientDelayAnalytics.values()).transform(new Function<AtomicInteger, Integer>() {
            @Override
            public Integer apply(AtomicInteger input) {
                return input.get();
            }
        }).toList());

        model.setServerQps(FluentIterable.from(serverQpsRank).limit(8).transform(new Function<PumaServerStatusDto, DashboardModel.Server>() {
            @Override
            public DashboardModel.Server apply(PumaServerStatusDto input) {
                return new DashboardModel.Server().setName(input.getName()).setQps(input.getStoreQps());
            }
        }).toList());
        model.setTaskQps(FluentIterable.from(taskQpsRank).limit(8).transform(new Function<PumaServerStatusDto.Server, DashboardModel.Task>() {
            @Override
            public DashboardModel.Task apply(PumaServerStatusDto.Server input) {
                return new DashboardModel.Task().setName(input.getName()).setQps(input.getStoreQps());
            }
        }).toList());
        model.setClientQps(FluentIterable.from(clientQpsRank).limit(8).transform(new Function<PumaServerStatusDto.Client, DashboardModel.Client>() {
            @Override
            public DashboardModel.Client apply(PumaServerStatusDto.Client input) {
                return new DashboardModel.Client().setName(input.getName()).setQps(input.getFetchQps());
            }
        }).toList());

        return model;
    }
}
