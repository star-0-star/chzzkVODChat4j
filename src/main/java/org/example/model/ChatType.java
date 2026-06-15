package org.example.model;

/**
 * 채팅 메시지의 종류입니다. 채팅 메시지의 {@code messageTypeCode}(라이브의 {@code msgTypeCode}) 필드에 대응합니다.
 */
public enum ChatType {

    TEXT(1),
    IMAGE(2),
    STICKER(3),
    VIDEO(4),
    RICH(5),
    DONATION(10),
    SUBSCRIPTION(11),
    SUBSCRIPTION_GIFT(12),
    SYSTEM_MESSAGE(30),
    OPEN(121),
    /** 매핑되지 않은 코드. */
    UNKNOWN(-1);

    private final int code;

    ChatType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    /** {@code messageTypeCode}에 해당하는 타입을 반환합니다. 매칭되는 값이 없으면 {@link #UNKNOWN}. */
    public static ChatType fromCode(int code) {
        for (ChatType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
