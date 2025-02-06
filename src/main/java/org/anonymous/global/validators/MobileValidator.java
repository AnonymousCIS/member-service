package org.anonymous.global.validators;

public interface MobileValidator {

    default boolean checkMobile(String num) {

        /**
         * 01010001000
         * 010-1000-1000
         * 010.1000.1000
         * 010 1000 1000
         *
         * 1) 숫자로만 통일성 있게 변환
         * 2) 패턴
         */

        // 정규 표현식 : 숫자(\\D)가 아닌 문자는 모두 제거
        num = num.replaceAll("\\D", "");

        // 정규 표현식 : 자리수
        String pattern = "^010\\d{4}\\d{4}$";

        return num.matches(pattern);
    }
}
