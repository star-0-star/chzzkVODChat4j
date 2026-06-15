package org.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChzzkChatCrawler {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/";

    public static void fetchAndSaveChatData(String vodId) {
        String nextPlayerMessageTime = "0";
        String filePath = "chatLog-" + vodId + ".csv";

        // 방송 시작 시각(openDate)을 미리 조회해 경과 시간 계산에 사용한다. (조회 실패 시 null)
        ChzzkVideoDetail videoDetail = fetchVideoDetail(vodId);
        Long openDateMillis =
                (videoDetail != null) ? videoDetail.liveOpenDateMillis() : null;
        if (openDateMillis == null) {
            System.out.println("방송 시작 시각을 알 수 없어 경과 시간 없이 저장합니다.");
        }

        try (ChatCsvWriter csvWriter = new ChatCsvWriter(filePath)) {

            while (true) {
                String url = "https://api.chzzk.naver.com/service/v1/videos/"
                        + vodId + "/chats?playerMessageTime=" + nextPlayerMessageTime;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", USER_AGENT)
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
                        ChzzkChat chat = ChzzkChatParser.parseChat(
                                videoChats.getJSONObject(i), openDateMillis);
                        csvWriter.write(chat);
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

        } catch (IOException e) {
            System.out.println("데이터를 가져오는 중 오류가 발생했습니다: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("크롤링이 중단되었습니다: " + e.getMessage());
        } catch (JSONException e) {
            System.out.println("JSON 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * VOD 상세 정보를 조회합니다. 경과 시간 계산을 위한 부가 정보이므로 실패해도
     * 채팅 크롤링을 막지 않도록 오류 시 {@code null}을 반환합니다.
     */
    private static ChzzkVideoDetail fetchVideoDetail(String vodId) {
        String url = "https://api.chzzk.naver.com/service/v3/videos/" + vodId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("방송 정보를 가져오지 못했습니다 (HTTP "
                        + response.statusCode() + ")");
                return null;
            }

            JSONObject content = new JSONObject(response.body()).optJSONObject("content");
            return (content != null) ? ChzzkVideoDetail.from(content) : null;

        } catch (IOException e) {
            System.out.println("방송 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("방송 정보 조회가 중단되었습니다: " + e.getMessage());
            return null;
        } catch (JSONException e) {
            System.out.println("방송 정보 파싱 중 오류가 발생했습니다: " + e.getMessage());
            return null;
        }
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
