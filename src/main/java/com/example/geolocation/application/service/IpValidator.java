package com.example.geolocation.application.service;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IpValidator {
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.){3}(25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)$");
    private static final Pattern PRIVATE_IPV4_PATTERN = Pattern.compile(
            "^(10\\.|172\\.(1[6-9]|2\\d|3[01])\\.|192\\.168\\.|127\\.|0\\.|169\\.254\\."
                    + "|100\\.(6[4-9]|[7-9]\\d|1[01]\\d|12[0-7])\\.)");
    private static final Pattern LOCALHOST_IPV6_PATTERN = Pattern.compile("^(::1|0:0:0:0:0:0:0:1)$");
    private static final Pattern PRIVATE_IPV6_PATTERN =
            Pattern.compile("^(fe80:|fc00:|fd00:)", Pattern.CASE_INSENSITIVE);

    public boolean isValid(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String trimmed = ip.trim();
        return isValidIpv4(trimmed) || isValidIpv6(trimmed);
    }

    public boolean isValidIpv4(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        return IPV4_PATTERN.matcher(ip.trim()).matches();
    }

    public boolean isValidIpv6(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String trimmed = ip.trim();

        if (isValidIpv4(trimmed)) {
            return false;
        }

        try {
            InetAddress addr = InetAddress.getByName(trimmed);
            return addr instanceof Inet6Address;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public boolean isPrivateOrReserved(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String trimmed = ip.trim();

        if (isValidIpv4(trimmed)) {
            return PRIVATE_IPV4_PATTERN.matcher(trimmed).find();
        }

        if (isValidIpv6(trimmed)) {
            return LOCALHOST_IPV6_PATTERN.matcher(trimmed).matches()
                    || PRIVATE_IPV6_PATTERN.matcher(trimmed).find();
        }

        return false;
    }

    public boolean isLocalhost(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        String trimmed = ip.trim();
        return trimmed.startsWith("127.") || LOCALHOST_IPV6_PATTERN.matcher(trimmed).matches();
    }

    public boolean isPublic(String ip) {
        return isValid(ip) && !isPrivateOrReserved(ip);
    }

    public String normalize(String ip) {
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("IP cannot be null or blank");
        }
        String trimmed = ip.trim();
        if (isValidIpv6(trimmed)) {
            return trimmed.toLowerCase();
        }
        return trimmed;
    }
}

