package com.dianping.puma.api.manager;

public enum Feedback {
	INITIAL        , // Sets before first connection.
	SUCCESS        , // Sets after reading events success.
	NET_ERROR      , // Sets after connection or reading events failure.
	HEARTBEAT_ERROR, // Sets after heartbeat lost.
	SERVER_ERROR   , // Sets after puma server error.
}
