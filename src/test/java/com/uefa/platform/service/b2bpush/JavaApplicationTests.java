package com.uefa.platform.service.b2bpush;

import com.uefa.platform.test.ActiveProfileOverrideResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(profiles = Application.Profiles.TEST, resolver = ActiveProfileOverrideResolver.class)
class JavaApplicationTests {

    @Test
    void contextLoads() {
    }

}
