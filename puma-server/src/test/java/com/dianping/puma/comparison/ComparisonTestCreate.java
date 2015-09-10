package com.dianping.puma.comparison;

import com.dianping.puma.comparison.comparison.Comparison;
import com.dianping.puma.comparison.comparison.FullComparison;
import com.dianping.puma.comparison.datasource.GroupDataSourceBuilder;
import com.dianping.puma.comparison.datasource.ShardDataSourceBuilder;
import com.dianping.puma.comparison.fetcher.SingleLineTargetFetcher;
import com.dianping.puma.comparison.fetcher.UpdateTimeAndIdSourceFetcher;
import com.dianping.puma.comparison.mapper.DefaultRowMapper;
import com.dianping.puma.comparison.mapper.RowMapper;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class ComparisonTestCreate {

	public static void main(String args[]) {

		String json = null;

		GroupDataSourceBuilder sourceBuilder = new GroupDataSourceBuilder();
		sourceBuilder.setJdbcRef("unifiedorder0");

		json = new Gson().toJson(sourceBuilder);

		System.out.println(json);

		ShardDataSourceBuilder targetBuilder = new ShardDataSourceBuilder();
		targetBuilder.setTableName("UOD_Order");
		targetBuilder.setDimensionIndex(3);
		targetBuilder.setRuleName("unifiedorder");

		json = new Gson().toJson(targetBuilder);

		System.out.println(json);

		UpdateTimeAndIdSourceFetcher sourceFetcher = new UpdateTimeAndIdSourceFetcher();
		sourceFetcher.setIdName("OrderID");
		sourceFetcher.setColumns("OrderID,UserID,MobileNO,CountryCode,DPID,BizType,CityID,SpugID,TotalAmount,TotalQuantity,Status,SubStatus,Platform,Source,Mode,ShopID,PartnerID,DeviceType,AddTime,UpdateTime,BuySuccessTime,TradeFinishedTime,ExpiredTime,HasPayment,HasRefund,HasCertification,HasConsumption,HasPromotion,DisplayMode,UserIP");
		sourceFetcher.setTableName("UOD_Order0");

		json = new Gson().toJson(sourceFetcher);

		System.out.println(json);

		SingleLineTargetFetcher targetFetcher = new SingleLineTargetFetcher();
		targetFetcher.setColumns("OrderID,UserID,MobileNO,CountryCode,DPID,BizType,CityID,SpugID,TotalAmount,TotalQuantity,Status,SubStatus,Platform,Source,Mode,ShopID,PartnerID,DeviceType,AddTime,UpdateTime,BuySuccessTime,TradeFinishedTime,ExpiredTime,HasPayment,HasRefund,HasCertification,HasConsumption,HasPromotion,DisplayMode,UserIP");
		targetFetcher.setTableName("UOD_Order");

		json = new Gson().toJson(targetFetcher);

		System.out.println(json);

		Comparison comparison = new FullComparison();

		json = new Gson().toJson(comparison);

		System.out.println(json);

		RowMapper mapper = new DefaultRowMapper().setMapKey(Sets.newHashSet("OrderID", "AddTime"));

		json = new Gson().toJson(mapper);

		System.out.println(json);
	}
}
