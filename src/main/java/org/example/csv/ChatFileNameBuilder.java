package org.example.csv;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.example.model.ChzzkVideoDetail;

/**
 * 저장 파일명을 만듭니다. 형식: {@code 방송시작일_장르_플랫폼_스트리머.csv}.
 *
 * <p>장르/플랫폼은 현재 하드코딩합니다.
 */
public final class ChatFileNameBuilder {

    private static final String GENRE = "버튜버";
    private static final String PLATFORM = "CHZZK";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FILE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 방송 정보로 파일명을 만듭니다. 방송 정보를 알 수 없으면 기본 파일명({@code chatLog-{vodId}.csv}).
     */
    public String build(String vodId, ChzzkVideoDetail detail) {
        if (detail == null || detail.liveOpenDateMillis() == null) {
            return "chatLog-" + vodId + ".csv";
        }

        String date = FILE_DATE_FORMATTER.format(
                Instant.ofEpochMilli(detail.liveOpenDateMillis()).atZone(KST));
        String streamer = sanitize(detail.channelName());
        return date + "_" + GENRE + "_" + PLATFORM + "_" + streamer + ".csv";
    }

    /** 파일명에 쓸 수 없는 문자를 제거합니다. 결과가 비면 {@code "unknown"}. */
    private static String sanitize(String value) {
        if (value == null) {
            return "unknown";
        }
        String cleaned = value.replaceAll("[\\\\/:*?\"<>|\\s]", "");
        return cleaned.isEmpty() ? "unknown" : cleaned;
    }
}
