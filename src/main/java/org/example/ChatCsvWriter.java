package org.example;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 치지직 채팅을 CSV 파일로 기록합니다.
 *
 * <p>Excel 호환을 위해 UTF-8 BOM을 기록하고, 각 필드는 RFC 4180 방식으로 이스케이프합니다.
 * 생성 시 헤더까지 기록하므로, 닫을 때까지 {@link #write(ChzzkChat)}만 반복 호출하면 됩니다.
 */
public final class ChatCsvWriter implements Closeable {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BufferedWriter writer;

    public ChatCsvWriter(String filePath) throws IOException {
        this.writer = new BufferedWriter(
                new FileWriter(filePath, StandardCharsets.UTF_8));
        // Excel에서 한글이 깨지지 않도록 UTF-8 BOM 기록
        writer.write('\uFEFF');
        // CSV 헤더
        writer.write("시간,경과시간(초),메시지타입,유저ID,닉네임,내용");
        writer.newLine();
    }

    /** 채팅 한 건을 CSV 한 줄로 기록합니다. (각 필드 이스케이프 처리) */
    public void write(ChzzkChat chat) throws IOException {
        String row = csvEscape(formatTime(chat.msgTime())) + ","
                + csvEscape(formatElapsedSeconds(chat.elapsedSec())) + ","
                + csvEscape(formatChatType(chat.chatType())) + ","
                + csvEscape(chat.user().uid()) + ","
                + csvEscape(chat.user().nickname()) + ","
                + csvEscape(chat.msg());
        writer.write(row);
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    /** 메시지 종류를 이름 문자열로 변환합니다. {@code null}이면 빈 문자열. */
    static String formatChatType(ChatType chatType) {
        if (chatType == null) {
            return "";
        }
        return chatType.name();
    }

    /** 경과 시간(초)을 소수점 문자열로 변환합니다. {@code null}이면 빈 문자열. */
    static String formatElapsedSeconds(Double elapsedSec) {
        if (elapsedSec == null) {
            return "";
        }
        return String.valueOf(elapsedSec);
    }

    /** 유닉스 타임스탬프(ms)를 한국 시간 문자열로 변환합니다. {@code null}이면 빈 문자열. */
    static String formatTime(Long epochMilli) {
        if (epochMilli == null) {
            return "";
        }
        return Instant.ofEpochMilli(epochMilli).atZone(KST).format(FORMATTER);
    }

    /** RFC 4180 방식의 CSV 필드 이스케이프 */
    static String csvEscape(String value) {
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
}
