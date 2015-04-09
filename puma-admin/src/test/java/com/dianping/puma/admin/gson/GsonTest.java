package com.dianping.puma.admin.gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.puma.core.model.AcceptedTables;
import com.google.gson.Gson;

public class GsonTest {

	public static final Gson gson = new Gson();

	public static void main(String[] args){
		Map<String,AcceptedTables> acceptedDataInfos=new HashMap<String,AcceptedTables>();
		AcceptedTables entity = new AcceptedTables();
		List<String> tables=new ArrayList<String>();
		tables.add("table1");
		tables.add("table2");
		entity.setTables(tables);
		acceptedDataInfos.put("database1", entity);
		AcceptedTables entity1 = new AcceptedTables();
		
		List<String> tables1=new ArrayList<String>();
		tables1.add("table1");
		tables1.add("table2");
		entity1.setTables(tables1);
		acceptedDataInfos.put("database2", entity1);
		String strGson = gson.toJson(acceptedDataInfos);
		System.out.println(strGson);
		acceptedDataInfos = gson.fromJson(strGson, Map.class);
		
		strGson = gson.toJson(acceptedDataInfos);
		System.out.println(strGson);
		//{"database1":{"tables":["table1","table2"]},"database2":{"tables":["table1","table2"]}}
		//{"test":{"tables":["tb_user"]}}
	}
}
