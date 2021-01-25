/*
 * Copyright (c) 2021, Oracle and/or its affiliates. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License, version 2.0, as published by the
 * Free Software Foundation.
 *
 * This program is also distributed with certain software (including but not
 * limited to OpenSSL) that is licensed under separate terms, as designated in a
 * particular file or component or in included license documentation. The
 * authors of MySQL hereby grant you an additional permission to link the
 * program and your derivative works with the separately licensed software that
 * they have included with MySQL.
 *
 * Without limiting anything contained in the foregoing, this file, which is
 * part of MySQL Connector/J, is also subject to the Universal FOSS Exception,
 * version 1.0, a copy of which can be found at
 * http://oss.oracle.com/licenses/universal-foss-exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License, version 2.0,
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301  USA
 */

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
