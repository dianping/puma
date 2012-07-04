package com.dianping.puma.api;

public class Configuration {

	public Configuration server(String host, int port) {
		return this;
	}

	public Configuration table(String database, String... tablePatterns) {
		return this;
	}

	public Configuration ddl(boolean supported) {
	   return this;
   }

	public Configuration seq(int startingSequence) {
	   return this;
   }

	public Configuration transaction(boolean supported) {
	   return this;
   }

	public Configuration batch(int batchSize) {
	   return this;
   }
}
