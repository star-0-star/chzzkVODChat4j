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
        fetchAndSaveChatData(vodId, null, null);
    }

    /**
     * 지정한 구간의 채팅만 수집합니다. 구간은 방송 경과시간({@code playerMessageTime}, ms) 기준입니다.
     *
     * <p>분 단위로 다루려면 {@code 분 * 60_000}으로 변환해 넘기면 됩니다.
     * (예: 90분부터 10분간 → {@code start=5_400_000, duration=600_000})
     *
     * @param startMillis    수집 시작 지점(ms). {@code null}이면 영상 처음부터.
     * @param durationMillis 수집 지속 시간(ms). {@code null}이면 끝까지.
     */
    public static void fetchAndSaveChatData(
            String vodId, Long startMillis, Long durationMillis) {
        long startOffset = (startMillis != null) ? startMillis : 0L;
        Long endMillis = (durationMillis != null) ? startOffset + durationMillis : null;

        String nextPlayerMessageTime = String.valueOf(startOffset);
        String filePath = "chatLog-" + vodId + ".csv";

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

                    // 수집 구간에 해당하는 채팅만 CSV로 기록
                    boolean reachedEnd = false;
                    for (int i = 0; i < videoChats.length(); i++) {
                        JSONObject chatJson = videoChats.getJSONObject(i);
                        long playerMessageTime = chatJson.optLong("playerMessageTime", -1);

                        // 시작 지점 이전 채팅은 건너뜀
                        if (playerMessageTime >= 0 && playerMessageTime < startOffset) {
                            continue;
                        }
                        // 종료 지점을 넘으면 수집 종료
                        if (endMillis != null && playerMessageTime > endMillis) {
                            reachedEnd = true;
                            break;
                        }
                        csvWriter.write(ChzzkChatParser.parseChat(chatJson));
                    }

                    if (reachedEnd) {
                        System.out.println("지정한 수집 구간 끝에 도달하여 종료합니다.");
                        break;
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

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("다시보기 ID: ");
            String vodId = scanner.nextLine();
            fetchAndSaveChatData(vodId);

            // TODO: 클립 구간 라벨링 (추후 구현)
            // 채팅을 리스트로 모은 뒤 클립 구간(시작~끝, 초)을 1로 표시:
            // List<ChzzkChat> labeled = ClipLabeler.markClipRange(chats, startSec, endSec);
        }
    }
}
