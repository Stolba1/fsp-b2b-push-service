package com.uefa.platform.service.b2bpush;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Contains spring profiles for {@link org.springframework.context.annotation.Profile}
     */
    public static class Profiles {

        public static final String DEFAULT = "default";
        public static final String TEST = "test";
        public static final String CI = "ci";
        private static final String PROFILE_PROPERTY_PATH = "spring.profiles.active";
        private static final String PROFILE_ENV_PATH = "SPRING_PROFILES_ACTIVE";

        private Profiles() {
            // prevent instantiation of this class
        }

        public static boolean isDefaultProfile() {
            String profileName = getActiveProfileName();
            return Objects.equals(null, profileName);
        }


        public static boolean isCiProfile() {
            return isProfile(Profiles.CI);
        }

        /**
         * Checks if the specified profileName is active
         *
         * @param profileName can not be null
         * @return true if current active profile matches specified profileName
         */
        public static boolean isProfile(String profileName) {
            String activeProfiles = getActiveProfileName();
            if (ObjectUtils.isEmpty(activeProfiles)) {
                return DEFAULT.equalsIgnoreCase(profileName);
            } else {
                String[] profileArr = activeProfiles.split(",");
                for (String profile : profileArr) {
                    if (profile.equalsIgnoreCase(profileName)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static String getActiveProfileName() {
            return System.getProperty(PROFILE_PROPERTY_PATH) != null ? System.getProperty(PROFILE_PROPERTY_PATH) : System.getenv(PROFILE_ENV_PATH);
        }

        public static boolean isLocalProfile() {
            return isProfile(DEFAULT) || isProfile(TEST) || isProfile(CI);
        }
    }
}
