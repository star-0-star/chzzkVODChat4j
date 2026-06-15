package org.example.model;

/**
 * 수신한 치지직 채팅 한 건을 나타냅니다.
 *
 * @param user       채팅을 보낸 유저.
 * @param msg        채팅 본문. 채팅 메시지의 {@code msg} 필드.
 * @param chatType   메시지 종류. 채팅 메시지의 {@code messageTypeCode}를 매핑한 값. 알 수 없으면 {@code null}.
 * @param msgTime    메시지 시각 (Unix epoch milliseconds, UTC). 채팅 메시지의 {@code msgTime} 필드.
 *                   초 단위로 다루려면 1000으로 나눠 변환하면 됩니다. 알 수 없으면 {@code null}.
 * @param elapsedSec 방송 시작 후 경과 시간(초). 채팅의 {@code playerMessageTime}(ms)을 1000으로
 *                   나눈 값입니다. 알 수 없으면 {@code null}. 분 단위가 필요하면
 *                   {@code elapsedSec / 60}을 쓰세요.
 * @param clip       클립 구간 포함 여부. 클립에 속하면 {@code true}. 기본값 {@code false}.
 */
public record ChzzkChat(
        ChzzkUser user, String msg, ChatType chatType, Long msgTime, Double elapsedSec,
        boolean clip) {

    /** {@code chatType}, {@code msgTime}, {@code elapsedSec} 없이 생성합니다. (Python의 기본값 None 대응) */
    public ChzzkChat(ChzzkUser user, String msg) {
        this(user, msg, null, null, null, false);
    }

    /** {@code elapsedSec} 없이 생성합니다. (Python의 기본값 None 대응) */
    public ChzzkChat(ChzzkUser user, String msg, ChatType chatType, Long msgTime) {
        this(user, msg, chatType, msgTime, null, false);
    }

    /** {@code clip} 없이 생성합니다. (기본값 {@code false}) */
    public ChzzkChat(
            ChzzkUser user, String msg, ChatType chatType, Long msgTime, Double elapsedSec) {
        this(user, msg, chatType, msgTime, elapsedSec, false);
    }

    /** {@code clip} 값만 바꾼 새 인스턴스를 반환합니다. */
    public ChzzkChat withClip(boolean clip) {
        return new ChzzkChat(user, msg, chatType, msgTime, elapsedSec, clip);
    }
}
