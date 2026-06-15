package org.example;

import java.util.ArrayList;
import java.util.List;

/** 채팅에 클립 구간 여부({@link ChzzkChat#clip()})를 표시합니다. 구간은 방송 경과시간(초) 기준입니다. */
public final class ClipLabeler {

    private ClipLabeler() {
    }

    /**
     * {@code startSec}부터 {@code endSec}까지(양 끝 포함) 구간에 속하는 채팅의 {@code clip}을 {@code true}로
     * 바꾼 새 리스트를 반환합니다. 구간 밖 채팅의 기존 {@code clip} 값은 유지됩니다(여러 구간을 누적 적용 가능).
     *
     * @param chats    원본 채팅 목록
     * @param startSec 구간 시작(방송 경과 초)
     * @param endSec   구간 끝(방송 경과 초)
     */
    public static List<ChzzkChat> markClipRange(
            List<ChzzkChat> chats, double startSec, double endSec) {
        List<ChzzkChat> result = new ArrayList<>(chats.size());
        for (ChzzkChat chat : chats) {
            Double elapsedSec = chat.elapsedSec();
            boolean inRange =
                    elapsedSec != null && elapsedSec >= startSec && elapsedSec <= endSec;
            result.add(inRange ? chat.withClip(true) : chat);
        }
        return result;
    }
}
