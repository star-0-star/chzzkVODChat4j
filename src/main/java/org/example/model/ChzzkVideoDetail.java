package org.example.model;

/**
 * 치지직 VOD 상세 정보(service/v3/videos/{vodId})에서 파일명 생성에 필요한 방송 정보입니다.
 *
 * @param channelName        스트리머(채널) 이름. {@code channel.channelName} 필드.
 * @param liveOpenDateMillis 방송 시작 시각(Unix epoch milliseconds). {@code liveOpenDate}(KST)를
 *                           변환한 값. 알 수 없으면 {@code null}.
 */
public record ChzzkVideoDetail(String channelName, Long liveOpenDateMillis) {
}
