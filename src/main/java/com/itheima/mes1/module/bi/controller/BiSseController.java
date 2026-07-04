package com.itheima.mes1.module.bi.controller;

import com.itheima.mes1.module.bi.mapper.BiAlertRecordMapper;
import com.itheima.mes1.module.bi.service.BiAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/bi")
public class BiSseController {

    @Autowired
    private BiAlertService alertService;

    /** 活跃的 SSE 连接列表 */
    private static final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /** 订阅实时推送 */
    @GetMapping("/alerts/stream")
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(0L); // 永不过期
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // 立即发送当前未读数
        try {
            long count = alertService.unreadCount();
            emitter.send(SseEmitter.event().name("alertCount").data(Map.of("unread", count)));
        } catch (Exception e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    /** 广播未读数到所有连接的客户端（由告警扫描触发） */
    public static void broadcast(long unreadCount) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("alertCount").data(Map.of("unread", unreadCount)));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }
}
