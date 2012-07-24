package com.dianping.puma.storage;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;


public class LocalBucketIndexTest {
	
	protected File work=null;
	
	@Before
	public void before()
	{
		work = new File(System.getProperty("java.io.tmpdir", "."), "Puma/20120710/bucket-0");
		work.getParentFile().mkdirs();
		try {
			if (work.createNewFile())
				System.out.println("create success!");
			
			work= new File(System.getProperty("java.io.tmpdir","."), "Puma/20120710/bucket-1");
			
			if(work.createNewFile())
			{
				System.out.println("create success");
				
			}
		} catch (IOException e1) {
			System.out.println("failed to create file");
		}
		
	}
	@Test
	public void testInit()
	{
		LocalFileBucketIndex localBucketIndex=new LocalFileBucketIndex();
		localBucketIndex.setBaseDir(System.getProperty("java.io.tmpdir", ".")+ "Puma");
		localBucketIndex.setBucketFilePrefix("bucket-");
		localBucketIndex.setMaxBucketLengthMB(500);
		
		
		
		
	}
}
