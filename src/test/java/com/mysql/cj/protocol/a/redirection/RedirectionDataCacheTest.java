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

import com.mysql.cj.conf.HostInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RedirectionDataCache}
 */
class RedirectionDataCacheTest {

    private RedirectionDataCache redirectionDataCache;
    private static final int LENGTHY_TTL = 999999;
    private static final int ZERO_TTL = 0;

    @BeforeEach
    public void setUp() {
        redirectionDataCache = RedirectionDataCache.getInstance();
    }

    @AfterEach
    public void cleanUp() throws ReflectiveOperationException {
        resetRedirectionDataCache();
    }

    // TEST INSERTION / MAX SIZE
    @Test
    void testMaxSizeIsMinusOne() throws ReflectiveOperationException {
        fillRedirectDataCache(-1, 10);
        assertEquals(10, getSizeOfRedirectionDataCache());
    }

    @Test
    void testMaxSizeIsZero() throws ReflectiveOperationException {
        fillRedirectDataCache(0, 10);
        assertEquals(0, getSizeOfRedirectionDataCache());
    }

    @Test
    void testMaxSizeExceeded() throws ReflectiveOperationException {
        fillRedirectDataCache(10, 11);
        assertEquals(10, getSizeOfRedirectionDataCache());
    }

    @Test
    void testMaxSizeEqualToEntries() throws ReflectiveOperationException {
        fillRedirectDataCache(10, 10);
        assertEquals(10, getSizeOfRedirectionDataCache());
    }

    @Test
    void testMaxSizeIsInRange() throws ReflectiveOperationException {
        fillRedirectDataCache(10, 9);
        assertEquals(9, getSizeOfRedirectionDataCache());
    }

    // TEST TTL
    @Test
    void testTtlWhenRecordShouldBeValid() throws InterruptedException {
        RedirectionData redirectionData = prepareRedirectionData(1235, 2);
        HostInfo hostInfo = prepareHostInfo(1234);
        redirectionDataCache.put(hostInfo, redirectionData);
        RedirectionData cachedRedirectionData = redirectionDataCache.get(hostInfo);
        assertEquals(redirectionData, cachedRedirectionData);
        TimeUnit.SECONDS.sleep(1);
        cachedRedirectionData = redirectionDataCache.get(hostInfo);
        assertEquals(redirectionData, cachedRedirectionData);
    }

    @Test
    void testTtlWhenRecordShouldBeInvalidV2() throws InterruptedException {
        RedirectionData redirectionData = prepareRedirectionData(1235, 2);
        HostInfo hostInfo = prepareHostInfo(1234);
        redirectionDataCache.put(hostInfo, redirectionData);
        RedirectionData cachedRedirectionData = redirectionDataCache.get(hostInfo);
        assertEquals(redirectionData, cachedRedirectionData);
        TimeUnit.SECONDS.sleep(2);
        cachedRedirectionData = redirectionDataCache.get(hostInfo);
        assertNull(cachedRedirectionData);
    }

    @Test
    void testTtlWhenRecordShouldBeInvalid() throws InterruptedException {
        RedirectionData redirectionData = prepareRedirectionData(1235, 2);
        HostInfo hostInfo = prepareHostInfo(1234);
        redirectionDataCache.put(hostInfo, redirectionData);
        RedirectionData cachedRedirectionData = redirectionDataCache.get(hostInfo);
        assertNotNull(redirectionData);
        assertEquals(redirectionData, cachedRedirectionData);
        TimeUnit.SECONDS.sleep(3);
        cachedRedirectionData = redirectionDataCache.get(hostInfo);
        assertNull(cachedRedirectionData);
    }

    // TEST REDIRECTS WITH LENGTH_TTL
    @Test
    void testABCDCircularDPointsAWithValidTtl() {
        prepareRedirectCacheData(LENGTHY_TTL);
        HostInfo hostInfoD = prepareHostInfo(3337);
        RedirectionData redirectionData = prepareRedirectionData(3307, LENGTHY_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertNull(finalRedirectionData);
    }

    @Test
    void testABCDCircularDPointsBWithValidTtl() {
        prepareRedirectCacheData(LENGTHY_TTL);
        HostInfo hostInfoD = prepareHostInfo(3337);
        RedirectionData redirectionData = prepareRedirectionData(3317, LENGTHY_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertNull(finalRedirectionData);
    }

    @Test
    void testABCDCircularDPointsCWithValidTtl() {
        prepareRedirectCacheData(LENGTHY_TTL);
        HostInfo hostInfoD = prepareHostInfo(3337);
        RedirectionData redirectionData = prepareRedirectionData(3327, LENGTHY_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertNull(finalRedirectionData);
    }

    @Test
    void testABCDCircularDPointsDWithValidTtl() {
        prepareRedirectCacheData(LENGTHY_TTL);
        HostInfo hostInfoD = prepareHostInfo(3337);
        RedirectionData redirectionData = prepareRedirectionData(3337, LENGTHY_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertNull(finalRedirectionData);
    }

    // TESTING TO SEE HOW REDIRECT CACHE IS BEHAVING WITH LENGTHY_TTL
    @Test
    void testABCDIfCallFromDifferentServerToARedirectsToDValidTtl() {
        prepareRedirectCacheData(LENGTHY_TTL);
        HostInfo hostInfoD = prepareHostInfo(8000);
        RedirectionData redirectionData = prepareRedirectionData(3307, LENGTHY_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(prepareRedirectionData(3337, LENGTHY_TTL), finalRedirectionData);
    }

    @Test
    void testABCDIfCallFromDifferentServerToBRedirectsToDValidTtl() {
        prepareRedirectCacheData(LENGTHY_TTL);
        HostInfo hostInfoD = prepareHostInfo(8000);
        RedirectionData redirectionData = prepareRedirectionData(3317, LENGTHY_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(prepareRedirectionData(3337, LENGTHY_TTL), finalRedirectionData);
    }

    @Test
    void testABCDIfCallFromDifferentServerToCRedirectsToDValidTtl() {
        prepareRedirectCacheData(LENGTHY_TTL);
        HostInfo hostInfoD = prepareHostInfo(8000);
        RedirectionData redirectionData = prepareRedirectionData(3327, LENGTHY_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(prepareRedirectionData(3337, LENGTHY_TTL), finalRedirectionData);
    }

    @Test
    void testABCDIfCallFromDifferentServerToDRedirectsToDValidTtl() {
        prepareRedirectCacheData(LENGTHY_TTL);
        HostInfo hostInfoD = prepareHostInfo(8000);
        RedirectionData redirectionData = prepareRedirectionData(3337, LENGTHY_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(prepareRedirectionData(3337, LENGTHY_TTL), finalRedirectionData);
    }

    // TEST REDIRECTS WITH ZERO_TTL
    @Test
    void testABCDCircularDPointsAWithInvalidTtl() {
        prepareRedirectCacheData(ZERO_TTL);
        HostInfo hostInfoD = prepareHostInfo(3337);
        RedirectionData redirectionData = prepareRedirectionData(3307, ZERO_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(redirectionData, finalRedirectionData);
    }

    @Test
    void testABCDCircularDPointsBWithInvalidTtl() {
        prepareRedirectCacheData(ZERO_TTL);
        HostInfo hostInfoD = prepareHostInfo(3337);
        RedirectionData redirectionData = prepareRedirectionData(3317, ZERO_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(redirectionData, finalRedirectionData);
    }

    @Test
    void testABCDCircularDPointsCWithInvalidTtl() {
        prepareRedirectCacheData(ZERO_TTL);
        HostInfo hostInfoD = prepareHostInfo(3337);
        RedirectionData redirectionData = prepareRedirectionData(3327, ZERO_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(redirectionData, finalRedirectionData);
    }

    @Test
    void testABCDCircularDPointsDWithInvalidTtl() {
        prepareRedirectCacheData(ZERO_TTL);
        HostInfo hostInfoD = prepareHostInfo(3337);
        RedirectionData redirectionData = prepareRedirectionData(3337, ZERO_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertNull(finalRedirectionData);
    }

    // TESTING TO SEE HOW REDIRECT CACHE IS BEHAVING WITH ZERO_TTL
    @Test
    void testABCDIfCallFromDifferentServerToARedirectsToDInvalidTtl() {
        prepareRedirectCacheData(ZERO_TTL);
        HostInfo hostInfoD = prepareHostInfo(8000);
        RedirectionData redirectionData = prepareRedirectionData(3307, ZERO_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(redirectionData, finalRedirectionData);
    }

    @Test
    void testABCDIfCallFromDifferentServerToBRedirectsToDInvalidTtl() {
        prepareRedirectCacheData(ZERO_TTL);
        HostInfo hostInfoD = prepareHostInfo(8000);
        RedirectionData redirectionData = prepareRedirectionData(3317, ZERO_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(redirectionData, finalRedirectionData);
    }

    @Test
    void testABCDIfCallFromDifferentServerToCRedirectsToDInvalidTtl() {
        prepareRedirectCacheData(ZERO_TTL);
        HostInfo hostInfoD = prepareHostInfo(8000);
        RedirectionData redirectionData = prepareRedirectionData(3327, ZERO_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(redirectionData, finalRedirectionData);
    }

    @Test
    void testABCDIfCallFromDifferentServerToDRedirectsToDInvalidTtl() {
        prepareRedirectCacheData(ZERO_TTL);
        HostInfo hostInfoD = prepareHostInfo(8000);
        RedirectionData redirectionData = prepareRedirectionData(3337, ZERO_TTL);
        RedirectionData finalRedirectionData = redirectionDataCache.determineFinalRedirection(hostInfoD, redirectionData);
        assertEquals(redirectionData, finalRedirectionData);
    }

    private void prepareRedirectCacheData(int ttl) {
        insertPreparedDataToRedirectCache(3307, 3317, ttl);
        insertPreparedDataToRedirectCache(3317, 3327, ttl);
        insertPreparedDataToRedirectCache(3327, 3337, ttl);
    }

    private void insertPreparedDataToRedirectCache(int hostPort, int redirectPort, int redirectionTtl) {
        HostInfo hostInfoA = prepareHostInfo(hostPort);
        RedirectionData redirectionDataA = prepareRedirectionData(redirectPort, redirectionTtl);
        redirectionDataCache.put(hostInfoA, redirectionDataA);
    }

    private HostInfo prepareHostInfo(int port) {
        return new HostInfo(null, "host", port, "user", "password");
    }

    private RedirectionData prepareRedirectionData(int port, int ttl) {
        return new RedirectionData("host", port, "user", ttl, Collections.emptyMap());
    }

    private void fillRedirectDataCache(int sizeOfCache, int redirectEntries) throws ReflectiveOperationException {
        resetRedirectionDataCache();
        redirectionDataCache = RedirectionDataCache.getInstance(sizeOfCache);
        for (int i = 0; i < redirectEntries; i++) {
            insertPreparedDataToRedirectCache(i, i, 123456);
        }
    }

    private void resetRedirectionDataCache() throws ReflectiveOperationException {
        Field redirectDataCacheInstance = RedirectionDataCache.class.getDeclaredField("instance");
        redirectDataCacheInstance.setAccessible(true);
        redirectDataCacheInstance.set(null, null);
    }

    private int getSizeOfRedirectionDataCache() throws ReflectiveOperationException {
        Field redirectDataCacheInstance = RedirectionDataCache.class.getDeclaredField("redirectDataCache");
        redirectDataCacheInstance.setAccessible(true);
        return ((ConcurrentHashMap) redirectDataCacheInstance.get(redirectionDataCache)).size();
    }
}