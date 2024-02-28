package com.uefa.platform.service.b2bpush;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class TestUtils {

    public static String loadResource(String resourceName) throws IOException {
        return StreamUtils.copyToString(new ClassPathResource(resourceName).getInputStream(), Charset.defaultCharset());
    }

}
