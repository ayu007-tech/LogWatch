
package com.logwatch.api.service;

import com.logwatch.api.model.LogEntry;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LogStreamService {

    private final List<SseEmitter> clients =
            new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        clients.add(emitter);

        emitter.onCompletion(() -> clients.remove(emitter));
        emitter.onTimeout(() -> {
            clients.remove(emitter);
            emitter.complete();
        });
        emitter.onError(error -> clients.remove(emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("LogWatch live stream connected"));
        } catch (IOException e) {
            clients.remove(emitter);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void publish(LogEntry log) {
        for (SseEmitter client : clients) {
            try {
                client.send(SseEmitter.event()
                        .name("log")
                        .data(log));
            } catch (IOException | IllegalStateException e) {
                clients.remove(client);
                client.complete();
            }
        }
    }
}
