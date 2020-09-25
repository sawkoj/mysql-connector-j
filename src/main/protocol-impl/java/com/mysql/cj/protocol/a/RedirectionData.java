package com.mysql.cj.protocol.a;

import com.mysql.cj.util.StringUtils;

public class RedirectionData {

    public static final String REDIRECT_DATA_MESSAGE_BEGINNING = "Location: mysql://[";
    private String host;
    private int port;
    private String user;
    private int ttl;

    private RedirectionData(String host, int port, String user, int ttl) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.ttl = ttl;
    }

    public static RedirectionData parseOkInfo(String okInfo) {
        String host;
        String port;
        String user;
        String ttl;

        int currentIndex = okInfo.indexOf(REDIRECT_DATA_MESSAGE_BEGINNING);
        currentIndex += REDIRECT_DATA_MESSAGE_BEGINNING.length();
        host = extractPartOfMessage(okInfo, 0, REDIRECT_DATA_MESSAGE_BEGINNING, "]");
        if (!StringUtils.isNullOrEmpty(host)) {
            currentIndex += host.length();
            port = extractPartOfMessage(okInfo, currentIndex, ":", "?");
            if (!StringUtils.isNullOrEmpty(port)) {
                currentIndex += port.length();
                user = extractPartOfMessage(okInfo, currentIndex, "user=", "&");
                if (!StringUtils.isNullOrEmpty(user)) {
                    currentIndex += user.length();
                    ttl = extractPartOfMessage(okInfo, currentIndex, "ttl=", "\n");
                    if (!StringUtils.isNullOrEmpty(ttl)) {
                        return new RedirectionData(host, Integer.parseInt(port), user, Integer.parseInt(ttl));
                    }
                }
            }
        }
        return null;
    }

    private static String extractPartOfMessage(String message, int startsFrom, String begin, String end) {

        int beginningIndex = message.indexOf(REDIRECT_DATA_MESSAGE_BEGINNING, startsFrom);
        if (beginningIndex > -1) {
            beginningIndex += beginningIndex;
            int endIndex = message.indexOf(end, beginningIndex);
            if (endIndex > -1) {
                return message.substring(beginningIndex, endIndex - 1);
            }
        }
        return null;
    }
}
