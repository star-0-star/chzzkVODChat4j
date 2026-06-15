package org.example.api;

import java.util.List;

import org.example.model.ChzzkChat;

/**
 * 채팅 한 페이지의 조회 결과입니다.
 *
 * @param chats                 이 페이지의 채팅 목록. 더 이상 데이터가 없으면 빈 리스트.
 * @param nextPlayerMessageTime 다음 페이지 커서. 마지막 페이지면 {@code null}.
 */
public record ChatPage(List<ChzzkChat> chats, Long nextPlayerMessageTime) {
}
