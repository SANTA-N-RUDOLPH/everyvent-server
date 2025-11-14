package kr.santanrudolph.everyvent.global.util;

import kr.santanrudolph.everyvent.global.exception.ErrorCode;
import kr.santanrudolph.everyvent.global.exception.EveryventException;

public class EnumUtil {

    /**
     * 문자열을 Enum으로 안전하게 변환
     * @param enumClass 변환할 Enum 클래스
     * @param value 변환할 문자열
     * @param <E> Enum 타입
     * @return 변환된 Enum 값
     * @throws EveryventException 잘못된 값이면 발생
     */
    public static <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
        if (value == null || value.isBlank()) {
            throw new EveryventException(ErrorCode.INVALID_INPUT_VALUE);
        }

        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new EveryventException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
