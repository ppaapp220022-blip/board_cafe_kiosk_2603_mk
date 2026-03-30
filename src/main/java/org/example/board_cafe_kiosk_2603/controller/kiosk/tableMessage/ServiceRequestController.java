package org.example.board_cafe_kiosk_2603.controller.kiosk.tableMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.tableMessage.TableMessage;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.cart.CartMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.tableMessage.TableMessageMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 키오스크 서비스 요청 → table_message DB 저장.
 * 관리자 읽음 처리 API 포함.
 * <p>
 * [키오스크]
 * POST /kiosk/service-request          → 요청 저장 (kiosk_layout.html callService())
 * <p>
 * [관리자]
 * GET  /admin/messages/unread          → 안 읽은 메시지 목록
 * GET  /admin/messages/table/{tableId} → 특정 테이블 메시지 목록
 * PUT  /admin/messages/{id}/read       → 단건 읽음 처리
 * PUT  /admin/messages/read-all        → 전체 읽음 처리
 */
@Log4j2
@RestController
@RequiredArgsConstructor
public class ServiceRequestController {

    private final TableMessageMapper tableMessageMapper;
    private final CartMapper cartMapper;

    // ===========================================================
    // 키오스크 → 관리자 요청 저장
    // ===========================================================

    @PostMapping("/kiosk/service-request")
    public Map<String, Object> serviceRequest(
            @RequestBody Map<String, Object> req) {

        String serviceType = (String) req.get("serviceType");
        int tableNumber = toInt(req.get("tableNumber"));
        Integer macroId = req.get("macroId") != null ? toInt(req.get("macroId")) : null;

        log.info("서비스 요청 - 테이블: {}, 내용: {}", tableNumber, serviceType);

        Map<String, Object> res = new LinkedHashMap<>();

        // tableId 조회
        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        if (tableId == null) {
            res.put("success", false);
            res.put("message", "테이블을 찾을 수 없습니다.");
            return res;
        }

        // table_message 저장
        TableMessage message = TableMessage.builder()
                .tableId(tableId)
                .macroId(macroId)
                .content(serviceType)
                .build();
        tableMessageMapper.insert(message);

        log.info("서비스 요청 저장 완료 - messageId: {}, tableId: {}", message.getId(), tableId);

        res.put("success", true);
        res.put("message", serviceType + " 요청이 전송되었습니다.");
        return res;
    }

    // ===========================================================
    // 관리자 — 메시지 조회
    // ===========================================================

    @GetMapping("/admin/messages/unread")
    public Map<String, Object> getUnreadMessages() {
        var messages = tableMessageMapper.findUnread();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("count", messages.size());
        res.put("messages", messages);
        return res;
    }

    @GetMapping("/admin/messages/table/{tableNumber}")
    public Map<String, Object> getMessagesByTable(
            @PathVariable int tableNumber) {

        Integer tableId = cartMapper.findCafeTableIdByTableNumber(tableNumber);
        Map<String, Object> res = new LinkedHashMap<>();
        if (tableId == null) {
            res.put("success", false);
            res.put("message", "테이블을 찾을 수 없습니다.");
            return res;
        }
        var messages = tableMessageMapper.findByTableId(tableId);
        res.put("success", true);
        res.put("count", messages.size());
        res.put("messages", messages);
        return res;
    }

    // ===========================================================
    // 관리자 — 읽음 처리
    // ===========================================================

    @PutMapping("/admin/messages/{id}/read")
    public Map<String, Object> markAsRead(@PathVariable long id) {
        tableMessageMapper.markAsRead(id);
        log.info("메시지 읽음 처리 - id: {}", id);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        return res;
    }

    @PutMapping("/admin/messages/read-all")
    public Map<String, Object> markAllAsRead() {
        tableMessageMapper.markAllAsRead();
        log.info("전체 메시지 읽음 처리");
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        return res;
    }

    // ===========================================================
    // 헬퍼
    // ===========================================================

    private int toInt(Object val) {
        if (val == null) return 0;
        return ((Number) val).intValue();
    }
}
