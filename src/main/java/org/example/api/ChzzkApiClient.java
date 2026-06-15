package org.example.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import org.example.model.ChzzkChat;
import org.example.model.ChzzkVideoDetail;

/** 치지직 오픈 API와 통신해 VOD 채팅/방송 정보를 가져옵니다. */
public class ChzzkApiClient {

    private static final String CHAT_URL =
            "https://api.chzzk.naver.com/service/v1/videos/%s/chats?playerMessageTime=%s";
    private static final String VIDEO_DETAIL_URL =
            "https://api.chzzk.naver.com/service/v3/videos/%s";
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter OPEN_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final HttpClient httpClient;

    public ChzzkApiClient() {
        this(HttpClient.newHttpClient());
    }

    /** {@link HttpClient}를 주입받습니다. (테스트 시 교체용) */
    public ChzzkApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /** 지정한 커서 위치부터 채팅 한 페이지를 가져옵니다. 데이터가 없으면 빈 페이지를 반환합니다. */
    public ChatPage fetchChatPage(String vodId, long playerMessageTime)
            throws IOException, InterruptedException {
        JSONObject data = new JSONObject(send(String.format(CHAT_URL, vodId, playerMessageTime)));

        int code = data.optInt("code", -1);
        JSONObject content = data.optJSONObject("content");
        JSONArray videoChats = (content != null) ? content.optJSONArray("videoChats") : null;
        if (code != 200 || videoChats == null || videoChats.length() == 0) {
            return new ChatPage(List.of(), null);
        }

        List<ChzzkChat> chats = new ArrayList<>(videoChats.length());
        for (int i = 0; i < videoChats.length(); i++) {
            chats.add(ChzzkChatParser.parseChat(videoChats.getJSONObject(i)));
        }
        Long next = content.isNull("nextPlayerMessageTime")
                ? null
                : content.getLong("nextPlayerMessageTime");
        return new ChatPage(chats, next);
    }

    /** VOD 상세 정보(채널명/방송 시작 시각)를 가져옵니다. 응답에 정보가 없으면 {@code null}. */
    public ChzzkVideoDetail fetchVideoDetail(String vodId)
            throws IOException, InterruptedException {
        JSONObject content =
                new JSONObject(send(String.format(VIDEO_DETAIL_URL, vodId))).optJSONObject("content");
        if (content == null) {
            return null;
        }

        JSONObject channel = content.optJSONObject("channel");
        String channelName = (channel != null)
                ? channel.optString("channelName", "unknown")
                : "unknown";
        return new ChzzkVideoDetail(
                channelName, parseOpenDate(content.optString("liveOpenDate", null)));
    }

    private String send(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP 오류: " + response.statusCode());
        }
        return response.body();
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
