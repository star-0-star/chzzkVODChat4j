package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChzzkChatCrawler {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void fetchAndSaveChatData(String vodId) {
        String nextPlayerMessageTime = "0";
        String filePath = "chatLog-" + vodId + ".csv";

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath, StandardCharsets.UTF_8))) {

            // Excel에서 한글이 깨지지 않도록 UTF-8 BOM 기록
            writer.write('\uFEFF');
            // CSV 헤더
            writer.write("시간,닉네임,내용,사용자ID");
            writer.newLine();

            while (true) {
                String url = "https://api.chzzk.naver.com/service/v1/videos/"
                        + vodId + "/chats?playerMessageTime=" + nextPlayerMessageTime;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                                        + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/")
                        .GET()
                        .build();

                HttpResponse<String> response =
                        HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    System.out.println("HTTP 오류가 발생했습니다: " + response.statusCode());
                    break;
                }

                JSONObject data = new JSONObject(response.body());
                int code = data.optInt("code", -1);
                JSONObject content = data.optJSONObject("content");
                JSONArray videoChats =
                        (content != null) ? content.optJSONArray("videoChats") : null;

                if (code == 200 && videoChats != null && videoChats.length() > 0) {

                    // 비디오 채팅 데이터를 CSV로 기록
                    for (int i = 0; i < videoChats.length(); i++) {
                        JSONObject chat = videoChats.getJSONObject(i);

                        long messageTime = chat.getLong("messageTime");
                        String userIdHash = chat.optString("userIdHash", "");
                        String chatContent = chat.optString("content", "");

                        // 유닉스 타임스탬프(ms)를 한국 시간으로 변환
                        ZonedDateTime kstTime =
                                Instant.ofEpochMilli(messageTime).atZone(KST);
                        String formattedTime = kstTime.format(FORMATTER);

                        // 프로필에서 닉네임 가져오기
                        String nickname = "Unknown";
                        if (chat.has("profile") && !chat.isNull("profile")) {
                            String profileStr = chat.getString("profile");
                            if (!profileStr.isEmpty() && !profileStr.equals("null")) {
                                try {
                                    JSONObject profile = new JSONObject(profileStr);
                                    nickname = profile.optString("nickname", "Unknown");
                                } catch (JSONException e) {
                                    nickname = "Unknown";
                                }
                            }
                        }

                        // CSV 한 줄 기록 (각 필드 이스케이프 처리)
                        String row = csvEscape(formattedTime) + ","
                                + csvEscape(nickname) + ","
                                + csvEscape(chatContent) + ","
                                + csvEscape(userIdHash);
                        writer.write(row);
                        writer.newLine();
                    }

                    // 다음 메시지 시간이 null이면 크롤링 종료
                    if (content.isNull("nextPlayerMessageTime")) {
                        System.out.println("마지막 채팅 페이지입니다. 데이터를 모두 파싱하고 종료합니다.");
                        break;
                    }

                    nextPlayerMessageTime =
                            String.valueOf(content.getLong("nextPlayerMessageTime"));
                    System.out.println("채팅 페이지가 업데이트되었습니다. "
                            + "(nextPlayerMessageTime: " + nextPlayerMessageTime + ")");

                    // API 부담을 줄이기 위한 페이지 단위 대기
                    Thread.sleep(300);
                } else {
                    System.out.println("유효한 채팅 데이터가 없거나 요청이 완료되었습니다.");
                    break;
                }
            }

            System.out.println("모든 채팅 로그가 " + filePath + "에 저장되었습니다.");

        } catch (IOException | InterruptedException e) {
            System.out.println("데이터를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
        } catch (JSONException e) {
            System.out.println("JSON 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /** RFC 4180 방식의 CSV 필드 이스케이프 */
    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("다시보기 ID: ");
            String vodId = scanner.nextLine();
            fetchAndSaveChatData(vodId);
        }
    }
}