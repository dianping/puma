package com.dianping.puma.admin.ds.extension;

import com.dianping.puma.admin.ds.Cluster;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DBATest {

	@Test
	public void test() {
		List<Cluster> clusters = DBA.query();
		System.out.println(clusters);
	}

}