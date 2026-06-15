package org.example.collector;

import java.io.IOException;

import org.example.model.ChzzkChat;

/** 수집된 채팅을 한 건씩 받는 소비자입니다. {@link java.util.function.Consumer}와 달리 {@link IOException}을 던질 수 있습니다. */
@FunctionalInterface
public interface ChatSink {

    void accept(ChzzkChat chat) throws IOException;
}
