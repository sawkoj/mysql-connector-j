package com.mysql.cj.protocol.a.redirection;

import com.mysql.cj.util.StringUtils;

public class RedirectionParser {
    public static final String REDIRECT_DATA_MESSAGE_BEGINNING = "Location: ";
    public static final String REDIRECT_DATA_MESSAGE_END = "\n";

    public static RedirectionData parseOkInfo(String okInfo) {
        RedirectionData redirectionData = null;
        String redirectInfoUrl = extractPartOfMessage(okInfo, REDIRECT_DATA_MESSAGE_BEGINNING, REDIRECT_DATA_MESSAGE_END);
        if (!StringUtils.isNullOrEmpty(redirectInfoUrl)) {
            redirectionData = parseRedirectUrl(redirectInfoUrl + "\n");
        }
        return redirectionData;
    }

    private static RedirectionData parseRedirectUrl(String redirect) {
        String host = extractPartOfMessage(redirect, "mysql://",":");
        String port = extractPartOfMessage(redirect, host + ":","/?");
        String user = extractPartOfMessage(redirect, "user=","&");
        String ttl = extractPartOfMessage(redirect, "ttl=","\n");
        RedirectionData redirectionData = null;
        if (!StringUtils.isNullOrEmpty(host)
                && !StringUtils.isNullOrEmpty(port) && isNumeric(port)
                && !StringUtils.isNullOrEmpty(user)
                && !StringUtils.isNullOrEmpty(ttl) && isNumeric(ttl)) {
            redirectionData = new RedirectionData(handleCommunityProtocol(host), Integer.parseInt(port), user, Integer.parseInt(ttl));
        }
        return redirectionData;
    }

    private static String handleCommunityProtocol(String host) {
        if (host.charAt(0) == '[' && host.charAt(host.length() - 1) == ']') {
            host = host.substring(1, host.length() - 2);
        }
        return host;
    }

    private static boolean isNumeric(String port) {
        return port.chars().allMatch(Character::isDigit);
    }

    private static String extractPartOfMessage(String message, String begin, String end) {

        int beginningIndex = message.indexOf(begin);
        if (beginningIndex > -1) {
            beginningIndex += begin.length();
            int endIndex = message.indexOf(end, beginningIndex);
            if (endIndex > -1) {
                return message.substring(beginningIndex, endIndex);
            }
        }
        return null;
    }
}
