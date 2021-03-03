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

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mysql.cj.conf.HostInfo;

/**
 * Cache of Redirection Data retrieved from connected host's OK packet
 */
public class RedirectionDataCache {

	private static RedirectionDataCache instance = null;
	private final long maxSize;
	private static final int INFINITY_SIZE = -1;
	private final ConcurrentMap<String, RedirectionDataCacheItem> redirectDataCache;

	RedirectionDataCache(long maxSize) {
		this.maxSize = maxSize;
		this.redirectDataCache = new ConcurrentHashMap<>();
	}

	/**
	 * Returned instance of RedirectionCache
	 *
	 * @return RedirectionCache instance
	 */
	public static synchronized RedirectionDataCache getInstance() {
		return getInstance(INFINITY_SIZE);
	}

	/**
	 * Returned instance of RedirectionCache with maximum size
	 *
	 * @param maxSize maximum size of cache
	 * @return RedirectionCache instance
	 */
	public static synchronized RedirectionDataCache getInstance(long maxSize) {
		if (Objects.isNull(instance)) {
			instance = new RedirectionDataCache(maxSize);
		}
		return instance;
	}

	/**
	 * Checks if redirection should be continued.
	 *
	 * @param currentHost     currentHost HostInfo instance
	 * @param redirectionData redirectionData RedirectionData instance
	 * @return Returns null if redirection should be stopped, that's when currentHost is pointing to redirectionData, or redirection data is already present in cache, else it
	 * returns passed redirectionData.
	 */
	public synchronized RedirectionData shouldContinueRedirect(HostInfo currentHost, RedirectionData redirectionData) {
		// cache is pointing to current location, return null and do not perform redirection
		if (compareRedirectDataCacheEntries(currentHost, redirectionData)) {
			return null;
		}
		RedirectionData cachedRedirect = get(redirectionData);
		// if redirectionData is present in cache, do not perform redirect
		return Objects.isNull(cachedRedirect) ? redirectionData : null;
	}

	/**
	 * Determine final redirection based on RedirectDataCache, as well as Host to which we are currently connected to.
	 *
	 * @param currentHost     Host to which we are currently connected to.
	 * @param redirectionData Redirection data that was received from OkPacket from current server
	 * @return Returns null if redirectionData is pointing to current host, or return correct value from redirect's chain
	 */
	public synchronized RedirectionData determineRedirectionUponCache(HostInfo currentHost, RedirectionData redirectionData) {
		RedirectionData cachedRedirect = redirectionData;
		RedirectionData temp;
		// determine if there is path in cache for given redirect information
		while (Objects.nonNull(cachedRedirect)) {
			// cache is pointing to current location, return null and do not perform redirection
			if (compareRedirectDataCacheEntries(currentHost, cachedRedirect)) {
				return null;
			}
			temp = get(cachedRedirect);
			// current cachedRedirect is not pointing to any location stored in cache, redirect to that location
			if (Objects.isNull(temp)) {
				return cachedRedirect;
			} else {
				cachedRedirect = temp;
			}
		}
		return null;
	}

	/**
	 * Put redirection data into cache
	 *
	 * @param hostInfo        information obout original host
	 * @param redirectionData redirection data
	 */
	public void put(HostInfo hostInfo, RedirectionData redirectionData) {
		if (this.maxSize == INFINITY_SIZE || this.redirectDataCache.size() < this.maxSize) {
			redirectDataCache.put(generateKey(hostInfo),
					new RedirectionDataCacheItem(redirectionData, Instant.now().getEpochSecond()));
		}
	}

	/**
	 * Returns redirection data for given host information or null if not exits in
	 * cache or expire
	 *
	 * @param hostInfo information about host
	 * @return Redirection data or null
	 */
	public RedirectionData get(HostInfo hostInfo) {
		return this.get(generateKey(hostInfo));
	}

	/**
	 * Returns redirection data for another redirection data (information about
	 * host) or null if not exits in cache or expire
	 *
	 * @param redirectionData redirection data (information about host)
	 * @return Redirection data
	 */
	public RedirectionData get(RedirectionData redirectionData) {
		return this.get(generateKey(redirectionData));
	}

	/**
	 * Compares HostInfo instance with RedirectionData instance
	 *
	 * @param hostInfo        HostInfo instance to compare
	 * @param redirectionData RedirectionData instance to compare
	 * @return True if hostInfo has the same user, host and port as redirectionData
	 */
	private boolean compareRedirectDataCacheEntries(HostInfo hostInfo, RedirectionData redirectionData) {
		return hostInfo.getUser().equals(redirectionData.getUser()) &&
				hostInfo.getHost().equals(redirectionData.getHost()) &&
				hostInfo.getPort() == redirectionData.getPort();
	}

	/**
	 * Generate cache key from information about host
	 *
	 * @param hostInfo information about host
	 * @return Cache key
	 */
	private String generateKey(HostInfo hostInfo) {
		return hostInfo.getUser() + "@" + hostInfo.getHost() + ":" + hostInfo.getPort();
	}

	/**
	 * Generate cache key from redirection data
	 *
	 * @param redirectionData redirection data
	 * @return Cache key
	 */
	private String generateKey(RedirectionData redirectionData) {
		return redirectionData.getUser() + "@" + redirectionData.getHost() + ":" + redirectionData.getPort();
	}

	private RedirectionData get(String key) {
		RedirectionDataCacheItem redirectionDataCacheItem = redirectDataCache.get(key);
		if (Objects.isNull(redirectionDataCacheItem)) {
			return null;
		}
		if (validateRedirectCacheEntry(redirectionDataCacheItem)) {
			this.invalidate(key);
			return null;
		}
		return redirectionDataCacheItem.getRedirectionData();
	}

	/**
	 * Checks if entry in redirectDataCache should be invalidated
	 *
	 * @param entry Entry from redirectDataCache
	 * @return true if it should be removed from collection, false if not
	 */
	private boolean validateRedirectCacheEntry(RedirectionDataCacheItem entry) {
		long entryValidTime = entry.getStorageTime() + entry.getRedirectionData().getTtl();
		return Instant.now().getEpochSecond() >= entryValidTime;
	}

	private void invalidate(String key) {
		redirectDataCache.remove(key);
	}

	/**
	 * Used to store redirection and TTL
	 */
	class RedirectionDataCacheItem {
		private final RedirectionData redirectionData;
		private final long storageTime;

		public RedirectionDataCacheItem(RedirectionData redirectionData, long storageTime) {
			this.redirectionData = redirectionData;
			this.storageTime = storageTime;
		}

		public RedirectionData getRedirectionData() {
			return redirectionData;
		}

		public long getStorageTime() {
			return storageTime;
		}
	}
}
