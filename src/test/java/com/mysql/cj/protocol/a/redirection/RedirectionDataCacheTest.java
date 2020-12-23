package com.mysql.cj.protocol.a.redirection;


import com.mysql.cj.conf.HostInfo;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RedirectionDataCacheTest {

    /**
     *
     * @throws InterruptedException
     */
    @Test
    void testRedirectionDataCacheTTL() throws InterruptedException {
        RedirectionDataCache redirectionDataCache = RedirectionDataCache.getInstance();
        RedirectionData redirectionData = new RedirectionData("newHost", 1235, "user",4, new HashMap<>());
        HostInfo hostInfo = new HostInfo(null, "host", 1234, "user", "password");
        redirectionDataCache.put(hostInfo, redirectionData);
        RedirectionData cachedRedirectionData = redirectionDataCache.get(hostInfo);
        assertNotNull(redirectionData);
        assertEquals(redirectionData, cachedRedirectionData);
        TimeUnit.SECONDS.sleep(5);
        cachedRedirectionData = redirectionDataCache.get(hostInfo);
        assertNull(cachedRedirectionData);
    }
}