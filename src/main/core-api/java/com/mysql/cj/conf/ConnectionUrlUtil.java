package com.mysql.cj.conf;

import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mysql.cj.util.StringUtils.isNullOrEmpty;
import static com.mysql.cj.util.StringUtils.safeTrim;

public class ConnectionUrlUtil {

	private static final String DUMMY_SCHEMA = "cj://";
	private static final Pattern KEY_VALUE_HOST_PTRN = Pattern
			.compile("[,\\s]*(?<key>[\\w\\.\\-\\s%]*)(?:=(?<value>[^,]*))?");
	private static final Pattern ADDRESS_EQUALS_HOST_PTRN = Pattern
			.compile("\\s*\\(\\s*(?<key>[\\w\\.\\-%]+)?\\s*(?:=(?<value>[^)]*))?\\)\\s*");
	private static final Pattern PROPERTIES_PTRN = Pattern
			.compile("[&\\s]*(?<key>[\\w\\.\\-\\s%]*)(?:=(?<value>[^&]*))?");

	/**
	 * Takes hostInfo and returns map of key and value if it form is key/value
	 * 
	 * @param hostInfo
	 *            information about host from connection utl
	 * @return key/value map containing information about host
	 */
	static Map<String, String> processHostKeyValue(String hostInfo) {
		return processKeyValuePattern(KEY_VALUE_HOST_PTRN, hostInfo);
	}

	/**
	 * Takes hostInfo and produces key/value map if it form is "address-equals"
	 * 
	 * @param hostInfo
	 *            information about host from connection utl
	 * @return key/value map containing information about host
	 */
	static Map<String, String> processAddressHost(String hostInfo) {
		return processKeyValuePattern(ADDRESS_EQUALS_HOST_PTRN, hostInfo);
	}

	/**
	 * Takes query part of connection url and produces key/value map of connection
	 * properties
	 * 
	 * @param query
	 *            query part of connection url
	 * @return key/value map of connection properties
	 */
	static Map<String, String> processUrlProperties(String query) {
		return processKeyValuePattern(PROPERTIES_PTRN, query);
	}

	/**
	 * Verify if host has Url form
	 * 
	 * @param hostInfo
	 *            host part of url
	 * @return True if host has Url form, False if not
	 */
	static boolean isHostUrlForm(String hostInfo) {
		try {
			URI uri = URI.create(DUMMY_SCHEMA + hostInfo);
			if (Objects.isNull(uri.getHost()) || uri.getPort() == -1) {
				return false;
			}
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	/**
	 * Takes a two-matching-groups (respectively named "key" and "value") pattern
	 * which is successively tested against the given string and produces a
	 * key/value map with the matched values. The given pattern must ensure that
	 * there are no leftovers between successive tests, i.e., the end of the
	 * previous match must coincide with the beginning of the next.
	 *
	 * @param pattern
	 *            the regular expression pattern to match against to
	 * @param input
	 *            the input string
	 * @return a key/value map containing the matched values
	 */
	private static Map<String, String> processKeyValuePattern(Pattern pattern, String input) {
		Matcher matcher = pattern.matcher(input);
		int p = 0;
		Map<String, String> kvMap = new HashMap<>();
		while (matcher.find()) {
			if (matcher.start() != p) {
				throw ExceptionFactory.createException(WrongArgumentException.class,
						Messages.getString("ConnectionString.4", new Object[]{input.substring(p)}));
			}
			String key = decode(safeTrim(matcher.group("key")));
			String value = decode(safeTrim(matcher.group("value")));
			if (!isNullOrEmpty(key)) {
				kvMap.put(key, value);
			} else if (!isNullOrEmpty(value)) {
				throw ExceptionFactory.createException(WrongArgumentException.class,
						Messages.getString("ConnectionString.4", new Object[]{input.substring(p)}));
			}
			p = matcher.end();
		}
		if (p != input.length()) {
			throw ExceptionFactory.createException(WrongArgumentException.class,
					Messages.getString("ConnectionString.4", new Object[]{input.substring(p)}));
		}
		return kvMap;
	}

	/**
	 * URL-decode the given string.
	 *
	 * @param text
	 *            the string to decode
	 * @return the decoded string
	 */
	static String decode(String text) {
		if (isNullOrEmpty(text)) {
			return text;
		}
		try {
			return URLDecoder.decode(text, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			// Won't happen.
			System.out.println(e.getMessage());
		}
		return "";
	}
}
