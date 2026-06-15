package org.example.collector;

import java.io.IOException;

import org.example.api.ChatPage;
import org.example.api.ChzzkApiClient;
import org.example.model.ChzzkChat;

/** VOD 채팅을 페이지 단위로 수집해 지정한 구간의 채팅을 {@code sink}로 흘려보냅니다. (스트리밍) */
public class VodChatCollector {

    private static final long PAGE_DELAY_MILLIS = 300;

    private final ChzzkApiClient apiClient;

    public VodChatCollector(ChzzkApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 채팅을 수집해 한 건씩 {@code sink}로 전달합니다.
     *
     * @param range 수집 구간(방송 경과시간 기준). {@link CollectRange#full()}이면 전체.
     * @param sink  수집된 채팅을 받을 소비자 (예: CSV 기록).
     */
    public void collect(String vodId, CollectRange range, ChatSink sink)
            throws IOException, InterruptedException {
        double startSec = range.startOffsetMillis() / 1000.0;
        Long endMillis = range.endMillis();
        Double endSec = (endMillis != null) ? endMillis / 1000.0 : null;

        long cursor = range.startOffsetMillis();
        while (true) {
            ChatPage page = apiClient.fetchChatPage(vodId, cursor);
            if (page.chats().isEmpty()) {
                break;
            }

            boolean reachedEnd = false;
            for (ChzzkChat chat : page.chats()) {
                Double elapsedSec = chat.elapsedSec();
                // 시작 지점 이전 채팅은 건너뜀
                if (elapsedSec != null && elapsedSec < startSec) {
                    continue;
                }
                // 종료 지점을 넘으면 수집 종료
                if (endSec != null && elapsedSec != null && elapsedSec > endSec) {
                    reachedEnd = true;
                    break;
                }
                sink.accept(chat);
            }

            if (reachedEnd || page.nextPlayerMessageTime() == null) {
                break;
            }
            cursor = page.nextPlayerMessageTime();

            // API 부담을 줄이기 위한 페이지 단위 대기
            Thread.sleep(PAGE_DELAY_MILLIS);
        }
    }
}
