package com.dianping.puma.parser.mysql;

import com.dianping.puma.parser.mysql.packet.FieldPacket;

import java.util.ArrayList;
import java.util.List;

public class ResultSet {
	
	private List<FieldPacket> fieldDescriptors=new ArrayList<FieldPacket>();
	
	private List<String> filedValues= new ArrayList<String>();

	public List<FieldPacket> getFieldDescriptors() {
		return fieldDescriptors;
	}

	public void setFieldDescriptors(List<FieldPacket> fieldDescriptors) {
		this.fieldDescriptors = fieldDescriptors;
	}

	public List<String> getFiledValues() {
		return filedValues;
	}

	public void setFiledValues(List<String> filedValues) {
		this.filedValues = filedValues;
	}
	
	
}
