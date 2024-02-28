package com.uefa.platform.service.b2bpush.core.domain.feed.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;

public class HashUtil {

    private static final Logger LOGGER = LogManager.getLogger(HashUtil.class);

    private HashUtil() {
    }

    public static String getMD5(String data) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.update(data.getBytes());
            byte[] digest = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(Integer.toHexString(b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            LOGGER.error("Exception while hashing string: {}, error {}", data, e);
        }
        return null;

    }
}
