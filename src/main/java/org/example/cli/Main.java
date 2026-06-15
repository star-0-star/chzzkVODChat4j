package org.example.cli;

import java.io.IOException;
import java.util.Scanner;

import org.json.JSONException;

import org.example.api.ChzzkApiClient;
import org.example.collector.CollectRange;
import org.example.collector.VodChatCollector;
import org.example.csv.ChatCsvWriter;
import org.example.csv.ChatFileNameBuilder;
import org.example.model.ChzzkVideoDetail;

/** CLI 진입점. 협력 객체들을 조립해 VOD 채팅을 수집·저장합니다. */
public class Main {

    public static void main(String[] args) {
        ChzzkApiClient apiClient = new ChzzkApiClient();
        VodChatCollector collector = new VodChatCollector(apiClient);
        ChatFileNameBuilder fileNameBuilder = new ChatFileNameBuilder();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("다시보기 ID: ");
            String vodId = scanner.nextLine();
            collectAndSave(apiClient, collector, fileNameBuilder, vodId);

            // TODO: 클립 구간 라벨링 (추후 구현)
            // 스트리밍 저장이라 라벨을 반영하려면 수집한 채팅을 모아 ClipLabeler 적용 후 재작성해야 함:
            // List<ChzzkChat> labeled = ClipLabeler.markClipRange(chats, startSec, endSec);
        }
    }

    private static void collectAndSave(
            ChzzkApiClient apiClient,
            VodChatCollector collector,
            ChatFileNameBuilder fileNameBuilder,
            String vodId) {
        ChzzkVideoDetail detail = fetchDetailQuietly(apiClient, vodId);
        String filePath = fileNameBuilder.build(vodId, detail);

        try (ChatCsvWriter writer = new ChatCsvWriter(filePath)) {
            collector.collect(vodId, CollectRange.full(), writer::write);
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

    /** 방송 정보를 조회합니다. 파일명 생성을 위한 부가 정보이므로 실패해도 {@code null} 반환(크롤링 계속). */
    private static ChzzkVideoDetail fetchDetailQuietly(ChzzkApiClient apiClient, String vodId) {
        try {
            return apiClient.fetchVideoDetail(vodId);
        } catch (IOException | JSONException e) {
            System.out.println("방송 정보를 알 수 없어 기본 파일명으로 저장합니다: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("방송 정보 조회가 중단되었습니다: " + e.getMessage());
            return null;
        }
    }
}
