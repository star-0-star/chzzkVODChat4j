package org.example;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.json.JSONObject;

/**
 * 치지직 VOD 상세 정보(service/v3/videos/{vodId})에서 필요한 방송 메타 정보입니다.
 *
 * @param title             방송 제목. {@code videoTitle} 필드.
 * @param channelName       채널 이름. {@code channel.channelName} 필드.
 * @param category          카테고리 표시값. {@code videoCategoryValue} 필드. 없으면 {@code null}.
 * @param liveOpenDateMillis 방송 시작 시각(Unix epoch milliseconds). {@code liveOpenDate}(KST)를
 *                          변환한 값. 알 수 없으면 {@code null}.
 */
public record ChzzkVideoDetail(
        String title,
        String channelName,
        String category,
        Long liveOpenDateMillis) {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter OPEN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** VOD 상세 응답의 {@code content}에서 방송 정보를 파싱합니다. */
    public static ChzzkVideoDetail from(JSONObject content) {
        JSONObject channel = content.optJSONObject("channel");
        String channelName = (channel != null)
                ? channel.optString("channelName", "unknown")
                : "unknown";

        return new ChzzkVideoDetail(
                content.optString("videoTitle", ""),
                channelName,
                content.optString("videoCategoryValue", null),
                parseOpenDate(content.optString("liveOpenDate", null)));
    }

    /** KST 문자열 {@code "yyyy-MM-dd HH:mm:ss"}를 epoch milliseconds로 변환합니다. 없으면 {@code null}. */
    static Long parseOpenDate(String openDate) {
        if (openDate == null || openDate.isEmpty()) {
            return null;
        }
        LocalDateTime localDateTime = LocalDateTime.parse(openDate, OPEN_DATE_FORMATTER);
        return localDateTime.atZone(KST).toInstant().toEpochMilli();
    }
}
