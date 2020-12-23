package com.mysql.cj.protocol.a.redirection;

import com.mysql.cj.conf.ConnectionUrl;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.util.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extract and parse redirection data from server response OK packet
 */
public class RedirectionParser {
	private static final String REDIRECT_DATA_MESSAGE_BEGINNING = "Location: ";
	private static final String REDIRECT_DATA_MESSAGE_END = "\n";

	/**
	 * Returns redirection data extracted and parsed from server response OK packet
	 * 
	 * @param okInfo
	 *            server response OK packet
	 * @return Redirection data
	 */
	public static RedirectionData parseOkInfo(String okInfo) {
		RedirectionData redirectionData = null;
		String redirectInfoUrl = extractPartOfMessage(okInfo, REDIRECT_DATA_MESSAGE_BEGINNING,
				REDIRECT_DATA_MESSAGE_END);
		if (!StringUtils.isNullOrEmpty(redirectInfoUrl)) {
			redirectionData = parseRedirectUrl(redirectInfoUrl);
		}
		return redirectionData;
	}

	private static RedirectionData parseRedirectUrl(String redirect) {
		HostInfo hostInfo = ConnectionUrl.getConnectionUrlInstance("jdbc:" + redirect, null).getMainHost();
		String host = hostInfo.getHost();
		String port = String.valueOf(hostInfo.getPort());
		String user = hostInfo.getUser();
		Map<String, String> hostProperties = hostInfo.getHostProperties();
		String ttl = hostProperties.get("ttl");
		hostProperties = prepareRedirectionProperties(hostProperties);
		RedirectionData redirectionData = null;
		if (!StringUtils.isNullOrEmpty(host) && !StringUtils.isNullOrEmpty(port) && !StringUtils.isNullOrEmpty(user)
				&& !StringUtils.isNullOrEmpty(ttl) && isNumeric(ttl)) {
			redirectionData = new RedirectionData(host, Integer.parseInt(port), user, Integer.parseInt(ttl),
					hostProperties);
		}
		return redirectionData;
	}

	private static Map<String, String> prepareRedirectionProperties(Map<String, String> hostProperties) {
		return hostProperties.entrySet().stream().filter(kv -> !StringUtils.isNullOrEmpty(kv.getValue()))
				.filter(kv -> "ttl".equals(kv.getKey()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private static boolean isNumeric(String value) {
		return value.chars().allMatch(Character::isDigit);
	}

	/**
	 * From given String extracts substring located between @begin and @end
	 * 
	 * @param message
	 *            given String
	 * @param begin
	 *            string before looking substring
	 * @param end
	 *            string after looking substring
	 * @return Substring from given String located between @begin and @end
	 */
	private static String extractPartOfMessage(String message, String begin, String end) {
		int beginningIndex = message.indexOf(begin);
		if (beginningIndex > -1) {
			beginningIndex += begin.length();
			int endIndex = message.indexOf(end, beginningIndex);
			if (endIndex > -1) {
				return message.substring(beginningIndex, endIndex);
			}
		}
		return "";
	}
}
