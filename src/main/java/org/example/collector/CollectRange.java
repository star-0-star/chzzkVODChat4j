package org.example.collector;

/**
 * 채팅 수집 구간입니다. 방송 경과시간({@code playerMessageTime}, ms) 기준입니다.
 *
 * @param startMillis    수집 시작 지점(ms). {@code null}이면 영상 처음부터.
 * @param durationMillis 수집 지속 시간(ms). {@code null}이면 끝까지.
 */
public record CollectRange(Long startMillis, Long durationMillis) {

    /** 영상 전체를 수집하는 구간. */
    public static CollectRange full() {
        return new CollectRange(null, null);
    }

    /** {@code startMillis}부터 {@code durationMillis}만큼 수집하는 구간. */
    public static CollectRange of(long startMillis, long durationMillis) {
        return new CollectRange(startMillis, durationMillis);
    }

    /** 시작 지점(ms). 미지정이면 0. */
    public long startOffsetMillis() {
        return (startMillis != null) ? startMillis : 0L;
    }

    /** 종료 지점(ms). 지속 시간 미지정이면 {@code null}(끝까지). */
    public Long endMillis() {
        return (durationMillis != null) ? startOffsetMillis() + durationMillis : null;
    }
}
