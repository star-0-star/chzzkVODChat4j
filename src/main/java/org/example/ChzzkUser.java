package org.example;

/**
 * 치지직 채팅을 보낸 유저 정보입니다.
 *
 * @param uid      유저 고유 ID. 채팅 메시지의 {@code uid} 필드 (= profile의 userIdHash).
 * @param nickname 채팅에 표시되는 닉네임. 채팅 메시지 profile의 {@code nickname} 필드.
 */
public record ChzzkUser(String uid, String nickname) {
}
