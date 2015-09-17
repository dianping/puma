package com.dianping.puma.checkserver;

import com.dianping.puma.checkserver.comparison.Comparison;
import com.dianping.puma.checkserver.comparison.FullComparison;
import com.dianping.puma.checkserver.datasource.GroupDataSourceBuilder;
import com.dianping.puma.checkserver.datasource.ShardDataSourceBuilder;
import com.dianping.puma.checkserver.fetcher.SingleLineTargetFetcher;
import com.dianping.puma.checkserver.fetcher.UpdateTimeAndIdSourceFetcher;
import com.dianping.puma.checkserver.mapper.DefaultRowMapper;
import com.dianping.puma.checkserver.mapper.RowMapper;
import com.google.common.collect.Sets;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;

/**
 * Dozer @ 2015-09
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class TaskExecutorBetaTest {
    private final Date startTime = new Date(1438387200000l); //2015-08-01
    private final Date endTime = new Date(1441065600000l); //2015-09-01

    @Test
    @Ignore
    public void testSame() throws Exception {
        GroupDataSourceBuilder sourceBuilder = new GroupDataSourceBuilder();
        sourceBuilder.setJdbcRef("unifiedorder0");

        ShardDataSourceBuilder targetBuilder = new ShardDataSourceBuilder();
        targetBuilder.setTableName("UOD_Order");
        targetBuilder.setDimensionIndex(3);
        targetBuilder.setRuleName("unifiedorder");

        UpdateTimeAndIdSourceFetcher sourceFetcher = new UpdateTimeAndIdSourceFetcher();
        sourceFetcher.setIdName("OrderID");
        sourceFetcher.setStartTime(startTime);
        sourceFetcher.setEndTime(endTime);
        sourceFetcher.init(sourceBuilder.build());
        sourceFetcher.setColumns("OrderID,UserID,MobileNO,CountryCode,DPID,BizType,CityID,SpugID,TotalAmount,TotalQuantity,Status,SubStatus,Platform,Source,Mode,ShopID,PartnerID,DeviceType,AddTime,UpdateTime,BuySuccessTime,TradeFinishedTime,ExpiredTime,HasPayment,HasRefund,HasCertification,HasConsumption,HasPromotion,DisplayMode,UserIP");
        sourceFetcher.setTableName("UOD_Order0");

        SingleLineTargetFetcher targetFetcher = new SingleLineTargetFetcher();
        targetFetcher.init(targetBuilder.build());
        targetFetcher.setColumns("OrderID,UserID,MobileNO,CountryCode,DPID,BizType,CityID,SpugID,TotalAmount,TotalQuantity,Status,SubStatus,Platform,Source,Mode,ShopID,PartnerID,DeviceType,AddTime,UpdateTime,BuySuccessTime,TradeFinishedTime,ExpiredTime,HasPayment,HasRefund,HasCertification,HasConsumption,HasPromotion,DisplayMode,UserIP");
        targetFetcher.setTableName("UOD_Order");

        Comparison comparison = new FullComparison();
        RowMapper mapper = new DefaultRowMapper().setMapKey(Sets.newHashSet("OrderID", "AddTime"));

        TaskExecutor target = TaskExecutor.Builder.create()
                .setSourceFetcher(sourceFetcher)
                .setTargetFetcher(targetFetcher)
                .setComparison(comparison)
                .setRowMapper(mapper)
                .build();

        target.call();
    }
}