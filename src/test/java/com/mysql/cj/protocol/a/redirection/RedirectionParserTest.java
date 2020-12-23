package com.mysql.cj.protocol.a.redirection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedirectionParserTest {

    /**
     * Test if RedirectionParser parse correct message
     */
    @Test
    void correctRedirectMessageTest() {
        String redirectMessage = "Location: mysql://[redirectHostName]:1600/?user=redirectUserName&ttl=10\n";
        RedirectionData redirectionData = RedirectionParser.parseOkInfo(redirectMessage);
        assertNotNull(redirectionData);
    }

    /**
     * Test if RedirectionParser parse incorrect message
     */
    @Test
    void incorrectRedirectMessageTest() {
        String redirectMessage = "Location: mysql://[redirectHostName]:1600/?user=redirectUserName&ttl=10";
        RedirectionData redirectionData = RedirectionParser.parseOkInfo(redirectMessage);
        assertNull(redirectionData);
    }

}