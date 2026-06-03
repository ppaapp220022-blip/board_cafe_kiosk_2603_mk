package org.example.board_cafe_kiosk_2603.kiosk.order.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.admin.product.dto.GameResponseDTO;
import org.example.board_cafe_kiosk_2603.kiosk.order.dto.OrderCreateRequest;
import org.example.board_cafe_kiosk_2603.kiosk.order.dto.OrderItemDTO;
import org.example.board_cafe_kiosk_2603.kiosk.order.dto.OrdersDTO;
import org.example.board_cafe_kiosk_2603.admin.product.service.GameService;
import org.example.board_cafe_kiosk_2603.kiosk.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
/*
 * 작성자 : 김민기
 * 기능 : 키오스크 주문 관리 컨트롤러.
 * 날짜 : 2026-03-27
 */

@Log4j2
@Controller
@RequestMapping("/kiosk/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final GameService gameService;

    /**
     * 주문 상세 페이지 조회합니다.
     *
     * @param orderId 전달받은 orderId 값
     * @param model 전달받은 model 값
     * @param session 전달받은 session 값
     * @return 처리 결과
     */
    @GetMapping("/{orderId}")
    public String orderDetailPage(@PathVariable int orderId, Model model, HttpSession session) {
        Integer tableNumber = sessionTableNumber(session);
        if (tableNumber == null) return "redirect:/kiosk";

        if (!orderService.isOrderOwnedByTableNumber(orderId, tableNumber)) {
            log.warn("주문 상세 접근 차단 - orderId: {}, tableNumber: {}", orderId, tableNumber);
            return "redirect:/kiosk/cart";
        }

        // 주문 정보 조회
        OrdersDTO order = orderService.getOrder(orderId);
        if (!order.isSuccess()) {
            log.warn("주문 조회 실패 - orderId: {}", orderId);
            return "redirect:/kiosk/cart";
        }

        // 모델에 데이터 추가
        boolean isGameOnlyOrder = isGameOnlyOrder(order);
        model.addAttribute("order", order);
        model.addAttribute("orderId", orderId);
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("isGameOnlyOrder", isGameOnlyOrder);
        model.addAttribute("statusDisplay", getStatusDisplay(order.getStatus(), isGameOnlyOrder));

        return "kiosk/order_detail";
    }

    /**
     * 게임 주문 상세 페이지 작업을 수행합니다.
     *
     * @param orderId 전달받은 orderId 값
     * @param model 전달받은 model 값
     * @param session 전달받은 session 값
     * @return 처리 결과
     */
    @GetMapping("/game/{orderId}")
    public String gameDetailPage(@PathVariable int orderId, Model model, HttpSession session) {
        Integer tableNumber = sessionTableNumber(session);
        if (tableNumber == null) return "redirect:/kiosk";

        if (!orderService.isOrderOwnedByTableNumber(orderId, tableNumber)) {
            return "redirect:/kiosk/cart";
        }

        OrdersDTO order = orderService.getOrder(orderId);
        if (!order.isSuccess()) {
            return "redirect:/kiosk/cart";
        }

        List<OrdersDTO> sessionOrders = orderService.getActiveSessionOrders(tableNumber);
        List<OrderItemDTO> gameItems = sessionOrders.stream()
                .filter(sessionOrder -> sessionOrder != null && sessionOrder.getItems() != null)
                .flatMap(sessionOrder -> sessionOrder.getItems().stream())
                .filter(item -> item != null && item.getPrice() == 0)
                .toList();

        if (gameItems.isEmpty()) {
            List<OrderItemDTO> orderItems = order.getItems() == null ? List.of() : order.getItems();
            gameItems = orderItems.stream()
                    .filter(item -> item != null && item.getPrice() == 0)
                    .toList();
        }

        if (gameItems.isEmpty()) {
            return "redirect:/kiosk/order/" + orderId;
        }

        List<String> gameNames = gameItems.stream()
                .map(OrderItemDTO::getMenuName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .toList();

        Map<String, GameResponseDTO> gameInfoMap = gameService.getByNames(gameNames).stream()
                .collect(Collectors.toMap(GameResponseDTO::getName, Function.identity(), (a, b) -> a));

        List<Map<String, Object>> requestedGames = new ArrayList<>();
        for (OrderItemDTO gameItem : gameItems) {
            String gameName = gameItem.getMenuName();
            GameResponseDTO info = gameInfoMap.get(gameName);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", gameName);
            row.put("quantity", gameItem.getQuantity());
            row.put("imageUrl", info != null ? info.getImageUrl() : null);
            row.put("minPlayers", info != null ? info.getMinPlayers() : null);
            row.put("maxPlayers", info != null ? info.getMaxPlayers() : null);
            row.put("playTime", info != null ? info.getPlayTime() : null);
            row.put("description", info != null ? info.getDescription() : null);
            row.put("orderId", gameItem.getOrderId());
            requestedGames.add(row);
        }

        model.addAttribute("order", order);
        model.addAttribute("orderId", orderId);
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("requestedGames", requestedGames);
        return "kiosk/game_detail";
    }

    /**
     * 주문 생성 처리합니다.
     *
     * @param request 전달받은 request 값
     * @param session 전달받은 session 값
     * @return 처리 결과
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<OrdersDTO> createOrder(
            @RequestBody OrderCreateRequest request,
            HttpSession session) {
        Integer tableNumber = sessionTableNumber(session);

        if (tableNumber == null) {
            log.warn("테이블 번호 없음 - 주문 생성 실패");
            return ResponseEntity.badRequest()
                    .body(OrdersDTO.builder()
                            .success(false)
                            .message("테이블 정보가 없습니다.")
                            .build());
        }

        try {
            OrdersDTO result = orderService.createOrderFromCart(
                    tableNumber,
                    request.getCustomerPhone(),
                    request.getTotalAmount()
            );

            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                log.warn("주문 생성 실패 - message: {}", result.getMessage());
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("주문 생성 중 예외 발생", e);
            return ResponseEntity.internalServerError()
                    .body(OrdersDTO.builder()
                            .success(false)
                            .message("주문 생성 중 오류가 발생했습니다: " + e.getMessage())
                            .build());
        }
    }

    /**
     * 대기 주문 목록 조회합니다.
     *
     * @return 처리 결과
     */
    @GetMapping("/pending")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPendingOrders() {
        try {
            List<OrdersDTO> orders = orderService.getNewOrders();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "orders", orders,
                    "count", orders.size()
            ));
        } catch (Exception e) {
            log.error("신규 주문 조회 실패", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "신규 주문 조회 실패"
                    ));
        }
    }

    /**
     * 주문 단건 조회 (JSON) 작업을 수행합니다.
     *
     * @param orderId 전달받은 orderId 값
     * @param session 전달받은 session 값
     * @return 처리 결과
     */
    @GetMapping("/api/{orderId}")
    @ResponseBody
    public ResponseEntity<OrdersDTO> getOrderApi(@PathVariable int orderId, HttpSession session) {
        Integer tableNumber = sessionTableNumber(session);
        if (tableNumber == null) return ResponseEntity.badRequest().build();
        if (!orderService.isOrderOwnedByTableNumber(orderId, tableNumber)) {
            log.warn("주문 API 접근 차단 - orderId: {}, tableNumber: {}", orderId, tableNumber);
            return ResponseEntity.status(403).build();
        }
        OrdersDTO result = orderService.getOrder(orderId);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    /**
     * 테이블 최근 주문 조회 (JSON) 작업을 수행합니다.
     *
     * @param session 전달받은 session 값
     * @return 처리 결과
     */
    @GetMapping("/latest")
    @ResponseBody
    public ResponseEntity<OrdersDTO> getLatestOrder(HttpSession session) {
        Integer tableNumber = sessionTableNumber(session);
        if (tableNumber == null) return ResponseEntity.badRequest().build();
        OrdersDTO result = orderService.getLatestOrderByTableNumber(tableNumber);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    /**
     * 세션 주문 목록 조회 (JSON) 작업을 수행합니다.
     *
     * @param sessionId 전달받은 sessionId 값
     * @param session 전달받은 session 값
     * @return 처리 결과
     */
    @GetMapping("/session/{sessionId}")
    @ResponseBody
    public ResponseEntity<List<OrdersDTO>> getOrdersBySession(@PathVariable long sessionId, HttpSession session) {
        Integer tableNumber = sessionTableNumber(session);
        if (tableNumber == null) return ResponseEntity.badRequest().build();
        if (!orderService.isSessionOwnedByTableNumber(sessionId, tableNumber)) {
            log.warn("세션 주문 목록 접근 차단 - sessionId: {}, tableNumber: {}", sessionId, tableNumber);
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(orderService.getOrdersBySessionId(sessionId));
    }

    /**
     * 현재 활성 세션의 주문 목록 조회 (장바구니 진입 버튼용) 작업을 수행합니다.
     *
     * @param session 전달받은 session 값
     * @return 처리 결과
     */
    @GetMapping("/active")
    @ResponseBody
    public ResponseEntity<List<OrdersDTO>> getActiveOrders(HttpSession session) {
        Integer tableNumber = sessionTableNumber(session);
        if (tableNumber == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(orderService.getActiveSessionOrders(tableNumber));
    }

    /**
     * 주문 상태 변경 처리합니다.
     *
     * @param orderId 전달받은 orderId 값
     * @param body 전달받은 body 값
     * @param session 전달받은 session 값
     * @return 처리 결과
     */
    @PatchMapping("/{orderId}/status")
    @ResponseBody
    public ResponseEntity<OrdersDTO> updateStatus(@PathVariable int orderId,
                                                  @RequestBody Map<String, String> body,
                                                  HttpSession session) {
        Integer tableNumber = sessionTableNumber(session);
        if (tableNumber == null) return ResponseEntity.badRequest().build();
        if (!orderService.isOrderOwnedByTableNumber(orderId, tableNumber)) {
            log.warn("주문 상태 변경 차단 - orderId: {}, tableNumber: {}", orderId, tableNumber);
            return ResponseEntity.status(403).build();
        }
        String status = body.get("status");
        if (status == null || status.isBlank()) return ResponseEntity.badRequest().build();
        OrdersDTO result = orderService.updateStatus(orderId, status);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    /**
     * 주문 취소 작업을 수행합니다.
     *
     * @param orderId 전달받은 orderId 값
     * @param session 전달받은 session 값
     * @return 처리 결과
     */
    @DeleteMapping("/{orderId}")
    @ResponseBody
    public ResponseEntity<OrdersDTO> cancelOrder(@PathVariable int orderId, HttpSession session) {
        Integer tableNumber = sessionTableNumber(session);
        if (tableNumber == null) return ResponseEntity.badRequest().build();
        if (!orderService.isOrderOwnedByTableNumber(orderId, tableNumber)) {
            log.warn("주문 취소 차단 - orderId: {}, tableNumber: {}", orderId, tableNumber);
            return ResponseEntity.status(403).build();
        }
        OrdersDTO result = orderService.cancelOrder(orderId);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    /**
     * 주문 상태 표시명 반환 작업을 수행합니다.
     *
     * @param status 전달받은 status 값
     * @param isGameOnlyOrder 전달받은 isGameOnlyOrder 값
     * @return 처리 결과
     */
    private String getStatusDisplay(String status, boolean isGameOnlyOrder) {
        if (isGameOnlyOrder) {
            return switch (status) {
                case "ORDERED" -> "게임 요청 완료! 일련번호 배정 대기 중...";
                case "CONFIRMED" -> "일련번호 배정 완료! 대여 준비가 끝났어요.";
                case "COOKING", "DELIVERING" -> "게임 대여 처리 중...";
                case "COMPLETED" -> "게임 처리 완료!";
                case "CANCELLED" -> "요청 취소됨";
                default -> "상태 확인 중...";
            };
        }
        return switch (status) {
            case "ORDERED"   -> "주문 완료! 관리자 확인 중...";
            case "CONFIRMED" -> "주문 확인! 조리 시작...";
            case "COOKING"   -> "조리 중입니다...";
            case "DELIVERING"-> "조리 완료! 서빙 중...";
            case "COMPLETED" -> "서빙 완료!";
            case "CANCELLED" -> "주문 취소됨";
            default -> "상태 확인 중...";
        };
    }

    /**
     * isGameOnlyOrder 동작을 수행합니다.
     *
     * @param order 전달받은 order 값
     * @return 처리 결과 여부
     */
    private boolean isGameOnlyOrder(OrdersDTO order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return false;
        }
        return order.getItems().stream()
                .allMatch(item -> item != null && item.getPrice() == 0);
    }

    /**
     * sessionTableNumber 동작을 수행합니다.
     *
     * @param session 전달받은 session 값
     * @return 처리 결과
     */
    private Integer sessionTableNumber(HttpSession session) {
        Object raw = session.getAttribute("tableNumber");
        if (raw == null) return null;
        if (raw instanceof Integer n) return n;
        try {
            int parsed = Integer.parseInt(raw.toString());
            session.setAttribute("tableNumber", parsed);
            return parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
