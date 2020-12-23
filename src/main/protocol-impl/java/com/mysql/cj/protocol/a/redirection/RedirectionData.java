package com.mysql.cj.protocol.a.redirection;

import java.util.Map;
import java.util.Objects;

/**
 * Contains information about redirection
 */
public class RedirectionData {

	private final String host;
	private final int port;
	private final String user;
	private final int ttl;
	private Map<String, String> properties;

	public RedirectionData(String host, int port, String user, int ttl, Map<String, String> properties) {
		this.host = host;
		this.port = port;
		this.user = user;
		this.ttl = ttl;
		this.properties = properties;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public int getTtl() {
		return ttl;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		RedirectionData that = (RedirectionData) o;
		return port == that.port && ttl == that.ttl && Objects.equals(host, that.host)
				&& Objects.equals(user, that.user) && Objects.equals(properties, that.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(host, port, user, ttl, properties);
	}
}
