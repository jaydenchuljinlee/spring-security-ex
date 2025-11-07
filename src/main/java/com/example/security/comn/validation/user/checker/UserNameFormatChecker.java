package com.example.security.comn.validation.user.checker;

import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserNameFormatChecker {
    private static final int USER_NAME_MAX_LENGTH = 20;

    public static boolean check(String mayUserName) {
        if (StringUtils.isBlank(mayUserName)) {
            return false;
        }

        return StringUtils.isNotBlank(mayUserName) &&
                mayUserName.length() <= USER_NAME_MAX_LENGTH &&
                containsOnlySupportedCharacter(mayUserName);
    }

    private static boolean containsOnlySupportedCharacter(String mayUserName) {
        for (char c : mayUserName.toCharArray()) {
            if (!isSupportedCharacter(c)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSupportedCharacter(char c) {
        return Character.isLowerCase(c) ||
                Character.isUpperCase(c);
    }
}
