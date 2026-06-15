package org.example;

import org.json.JSONException;
import org.json.JSONObject;

/** 치지직 비디오 채팅 JSON을 도메인 레코드({@link ChzzkChat})로 변환합니다. */
public final class ChzzkChatParser {

    private ChzzkChatParser() {
    }

    /** 비디오 채팅 JSON 한 건을 {@link ChzzkChat}으로 변환합니다. */
    public static ChzzkChat parseChat(JSONObject chat) {
        long messageTime = chat.getLong("messageTime");
        String userIdHash = chat.optString("userIdHash", "");
        String content = chat.optString("content", "");
        String nickname = parseNickname(chat);

        ChzzkUser user = new ChzzkUser(userIdHash, nickname);
        return new ChzzkChat(user, content, messageTime);
    }

    /** 중첩 JSON 문자열인 {@code profile}에서 닉네임을 추출합니다. 실패 시 {@code "Unknown"}. */
    static String parseNickname(JSONObject chat) {
        if (!chat.has("profile") || chat.isNull("profile")) {
            return "Unknown";
        }
        String profileStr = chat.getString("profile");
        if (profileStr.isEmpty() || profileStr.equals("null")) {
            return "Unknown";
        }
        try {
            return new JSONObject(profileStr).optString("nickname", "Unknown");
        } catch (JSONException e) {
            return "Unknown";
        }
    }
}
