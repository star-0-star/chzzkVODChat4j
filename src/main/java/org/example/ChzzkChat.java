package org.example;

/**
 * 수신한 치지직 채팅 한 건을 나타냅니다.
 *
 * @param user       채팅을 보낸 유저.
 * @param msg        채팅 본문. 채팅 메시지의 {@code msg} 필드.
 * @param msgTime    메시지 시각 (Unix epoch milliseconds, UTC). 채팅 메시지의 {@code msgTime} 필드.
 *                   초 단위로 다루려면 1000으로 나눠 변환하면 됩니다. 알 수 없으면 {@code null}.
 * @param elapsedSec 방송 시작 후 경과 시간(초). {@code msgTime}(UTC ms)에서 방송 시작 시각
 *                   (live-detail의 openDate, KST)을 빼서 계산합니다. 방송 시작 시각을 알 수
 *                   없으면 {@code null}. 분 단위가 필요하면 {@code elapsedSec / 60}을 쓰세요.
 */
public record ChzzkChat(ChzzkUser user, String msg, Long msgTime, Double elapsedSec) {

    /** {@code msgTime}, {@code elapsedSec} 없이 생성합니다. (Python의 기본값 None 대응) */
    public ChzzkChat(ChzzkUser user, String msg) {
        this(user, msg, null, null);
    }

    /** {@code elapsedSec} 없이 생성합니다. (Python의 기본값 None 대응) */
    public ChzzkChat(ChzzkUser user, String msg, Long msgTime) {
        this(user, msg, msgTime, null);
    }
}