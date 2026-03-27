package org.example.board_cafe_kiosk_2603.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Log4j2
@Controller
@RequestMapping("/kiosk")
public class kioskController {

    /**
     * 키오스크 메뉴 컨트롤러
     *
     * 테스트 URL:
     * - http://localhost:8080/kiosk/drinks?tableNumber=5
     * - http://localhost:8080/kiosk/food?tableNumber=5
     * - http://localhost:8080/kiosk/games?tableNumber=5
     * - http://localhost:8080/kiosk/cart?tableNumber=5
     * - http://localhost:8080/kiosk/checkout?tableNumber=5
     */

    // ===== 메뉴 페이지 =====

    /**
     * 음료 메뉴 페이지
     * GET /kiosk/drinks
     */
    @GetMapping("/drinks")
    public String drinks(
            @RequestParam(required = false, defaultValue = "5") Integer tableNumber,
            HttpSession session,
            Model model) {

        log.info("=== 음료 메뉴 요청 ===");
        log.info("테이블 번호: {}", tableNumber);

        // 세션 초기화 (첫 접속 시)
        initializeSessionIfNeeded(session, tableNumber);

        // Model에 필수 데이터 추가
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        model.addAttribute("currentMenu", "drinks");
        model.addAttribute("menuItems", getDrinkItems());
        model.addAttribute("cartCount", getCartCount(session));
        model.addAttribute("pageTitle", "음료");

        log.info("음료 메뉴 아이템 {}개 로드됨, 장바구니 아이템 {}개",
                getDrinkItems().size(), getCartCount(session));

        return "layout/kiosk_layout";
    }

    /**
     * 음식 메뉴 페이지
     * GET /kiosk/food
     */
    @GetMapping("/food")
    public String food(
            @RequestParam(required = false, defaultValue = "5") Integer tableNumber,
            HttpSession session,
            Model model) {

        log.info("=== 음식 메뉴 요청 ===");
        log.info("테이블 번호: {}", tableNumber);

        initializeSessionIfNeeded(session, tableNumber);

        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        model.addAttribute("currentMenu", "food");
        model.addAttribute("menuItems", getFoodItems());
        model.addAttribute("cartCount", getCartCount(session));
        model.addAttribute("pageTitle", "음식");

        log.info("음식 메뉴 아이템 {}개 로드됨, 장바구니 아이템 {}개",
                getFoodItems().size(), getCartCount(session));

        return "layout/kiosk_layout";
    }

    /**
     * 게임 메뉴 페이지
     * GET /kiosk/games
     */
    @GetMapping("/games")
    public String games(
            @RequestParam(required = false, defaultValue = "5") Integer tableNumber,
            HttpSession session,
            Model model) {

        log.info("=== 게임 메뉴 요청 ===");
        log.info("테이블 번호: {}", tableNumber);

        initializeSessionIfNeeded(session, tableNumber);

        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        model.addAttribute("currentMenu", "games");
        model.addAttribute("menuItems", getGameItems());
        model.addAttribute("cartCount", getCartCount(session));
        model.addAttribute("pageTitle", "게임");

        log.info("게임 메뉴 아이템 {}개 로드됨, 장바구니 아이템 {}개",
                getGameItems().size(), getCartCount(session));

        return "layout/kiosk_layout";
    }

    // ===== 장바구니 =====

    /*
     * 장바구니 페이지
     * GET /kiosk/cart
     */
    @GetMapping("/cart")
    public String cart(
            @RequestParam(required = false, defaultValue = "5") Integer tableNumber,
            HttpSession session,
            Model model) {

        log.info("=== 장바구니 요청 ===");
        log.info("테이블 번호: {}", tableNumber);

        List<Map<String, Object>> cartItems = getCartItems(session);
        int totalPrice = calculateTotalPrice(cartItems);

        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cartCount", cartItems.size());

        log.info("장바구니 조회: 아이템 {}개, 총액 ₩{}", cartItems.size(), totalPrice);

        return "kiosk/cart";
    }

    /*
     * 정산 페이지
     * GET /kiosk/checkout
     */
    @GetMapping("/checkout")
    public String checkout(
            @RequestParam(required = false, defaultValue = "5") Integer tableNumber,
            HttpSession session,
            Model model) {

        log.info("=== 정산 페이지 요청 ===");
        log.info("테이블 번호: {}", tableNumber);

        List<Map<String, Object>> cartItems = getCartItems(session);
        int totalPrice = calculateTotalPrice(cartItems);
        int sessionDuration = getSessionDuration(session);

        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("sessionDuration", sessionDuration);
        model.addAttribute("sessionMinutes", sessionDuration % 60);
        model.addAttribute("sessionHours", sessionDuration / 60);

        log.info("정산 페이지: 아이템 {}개, 총액 ₩{}, 세션 {}분",
                cartItems.size(), totalPrice, sessionDuration);

        return "kiosk/checkout";
    }

    // ===== AJAX API =====

    /**
     * 장바구니에 상품 추가
     * POST /api/cart/add
     */
    @PostMapping("/api/cart/add")
    @ResponseBody
    public Map<String, Object> addToCart(
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        String itemName = (String) request.get("itemName");
        Integer price = ((Number) request.get("price")).intValue();
        Integer quantity = ((Number) request.get("quantity")).intValue();

        log.info("장바구니 추가: {} x{} (₩{})", itemName, quantity, price);

        List<Map<String, Object>> cart = getCartItems(session);

        // 동일 상품 존재 여부 확인
        boolean found = false;
        for (Map<String, Object> item : cart) {
            if (item.get("itemName").equals(itemName) && item.get("price").equals(price)) {
                Integer currentQty = ((Number) item.get("quantity")).intValue();
                item.put("quantity", currentQty + quantity);
                found = true;
                log.info("기존 상품 수량 업데이트: {}", currentQty + quantity);
                break;
            }
        }

        // 새로운 상품 추가
        if (!found) {
            Map<String, Object> newItem = new LinkedHashMap<>();
            newItem.put("itemName", itemName);
            newItem.put("price", price);
            newItem.put("quantity", quantity);
            cart.add(newItem);
            log.info("새 상품 추가");
        }

        session.setAttribute("cart", cart);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("cartCount", cart.size());
        response.put("totalPrice", calculateTotalPrice(cart));
        response.put("message", itemName + "이(가) 장바구니에 추가되었습니다.");

        return response;
    }

    /**
     * 장바구니 조회 (JSON)
     * GET /api/cart
     */
    @GetMapping("/api/cart")
    @ResponseBody
    public Map<String, Object> getCart(HttpSession session) {
        List<Map<String, Object>> cartItems = getCartItems(session);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("cartItems", cartItems);
        response.put("totalPrice", calculateTotalPrice(cartItems));
        response.put("cartCount", cartItems.size());

        return response;
    }

    /**
     * 장바구니 상품 수량 업데이트
     * PUT /api/cart/update
     */
    @PutMapping("/api/cart/update")
    @ResponseBody
    public Map<String, Object> updateCartItem(
            @RequestBody Map<String, Object> request,
            HttpSession session) {

        String itemName = (String) request.get("itemName");
        Integer price = ((Number) request.get("price")).intValue();
        Integer quantity = ((Number) request.get("quantity")).intValue();

        log.info("장바구니 수량 업데이트: {} -> {}", itemName, quantity);

        List<Map<String, Object>> cart = getCartItems(session);

        if (quantity <= 0) {
            // 수량이 0 이하면 삭제
            cart.removeIf(item ->
                    item.get("itemName").equals(itemName) && item.get("price").equals(price)
            );
            log.info("상품 제거: {}", itemName);
        } else {
            // 수량 업데이트
            for (Map<String, Object> item : cart) {
                if (item.get("itemName").equals(itemName) && item.get("price").equals(price)) {
                    item.put("quantity", quantity);
                    break;
                }
            }
        }

        session.setAttribute("cart", cart);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("cartCount", cart.size());
        response.put("totalPrice", calculateTotalPrice(cart));

        return response;
    }

    /**
     * 장바구니 비우기
     * DELETE /api/cart
     */
    @DeleteMapping("/api/cart")
    @ResponseBody
    public Map<String, Object> clearCart(HttpSession session) {
        session.setAttribute("cart", new ArrayList<>());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "장바구니가 비워졌습니다.");

        return response;
    }

    /**
     * 결제 처리
     * POST /api/checkout/pay
     */
    @PostMapping("/api/checkout/pay")
    @ResponseBody
    public Map<String, Object> processPayment(
            @RequestBody Map<String, Object> request,
            @RequestParam Integer tableNumber,
            HttpSession session) {

        String paymentMethod = (String) request.get("paymentMethod");
        List<Map<String, Object>> cartItems = getCartItems(session);
        int totalPrice = calculateTotalPrice(cartItems);

        log.info("=== 결제 처리 ===");
        log.info("테이블: {}, 금액: ₩{}, 결제 수단: {}", tableNumber, totalPrice, paymentMethod);

        // 여기에 실제 결제 로직 추가
        // - 결제 게이트웨이 호출
        // - 주문 정보 DB 저장
        // - 영수증 생성 등

        // 결제 성공 후 장바구니 비우기
        session.setAttribute("cart", new ArrayList<>());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "결제가 완료되었습니다.");
        response.put("orderId", "ORD-" + System.currentTimeMillis());
        response.put("totalPrice", totalPrice);

        return response;
    }

    // ===== 헬퍼 메서드 =====

    /**
     * 세션 초기화 (첫 접속 시)
     */
    @SuppressWarnings("unchecked")
    private void initializeSessionIfNeeded(HttpSession session, Integer tableNumber) {
        if (session.getAttribute("tableNumber") == null) {
            session.setAttribute("tableNumber", tableNumber);
            session.setAttribute("cart", new ArrayList<>());
            session.setAttribute("sessionStartTime", System.currentTimeMillis());
            log.info("세션 초기화 - 테이블: {}", tableNumber);
        }
        if (session.getAttribute("partySize") == null) {
            session.setAttribute("partySize", 1);
        }
    }

    /**
     * 장바구니 아이템 조회
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getCartItems(HttpSession session) {
        List<Map<String, Object>> cart = (List<Map<String, Object>>) session.getAttribute("cart");
        return cart != null ? cart : new ArrayList<>();
    }

    /**
     * 장바구니 아이템 개수
     */
    private int getCartCount(HttpSession session) {
        return getCartItems(session).size();
    }

    /**
     * 총 가격 계산
     */
    private int calculateTotalPrice(List<Map<String, Object>> cartItems) {
        return cartItems.stream()
                .mapToInt(item -> {
                    Integer price = (Integer) item.get("price");
                    Integer quantity = ((Number) item.get("quantity")).intValue();
                    return price * quantity;
                })
                .sum();
    }

    /**
     * 인원 수 조회
     */
    private Integer getPartySize(HttpSession session) {
        Integer partySize = (Integer) session.getAttribute("partySize");
        return partySize != null ? partySize : 2;
    }

    /**
     * 세션 지속 시간 (분)
     */
    private int getSessionDuration(HttpSession session) {
        Long startTime = (Long) session.getAttribute("sessionStartTime");
        if (startTime == null) return 0;
        return (int) ((System.currentTimeMillis() - startTime) / 60000);
    }

    // ===== 메뉴 데이터 =====

    /**
     * 음료 메뉴 아이템
     */
    private List<Map<String, Object>> getDrinkItems() {
        List<Map<String, Object>> items = new ArrayList<>();

        items.add(createMenuItem("아메리카노", 4500, "☕"));
        items.add(createMenuItem("카페라테", 5000, "☕"));
        items.add(createMenuItem("아이스 아메리카노", 5000, "🧊"));
        items.add(createMenuItem("아이스 카페라테", 5500, "🧊"));
        items.add(createMenuItem("카라멜 마키아또", 6000, "☕"));
        items.add(createMenuItem("에스프레소", 3500, "☕"));
        items.add(createMenuItem("캔 코카콜라", 2000, "🥤"));
        items.add(createMenuItem("캔 스프라이트", 2000, "🥤"));

        return items;
    }

    /**
     * 음식 메뉴 아이템
     */
    private List<Map<String, Object>> getFoodItems() {
        List<Map<String, Object>> items = new ArrayList<>();

        items.add(createMenuItem("스파게티", 12000, "🍝"));
        items.add(createMenuItem("까르보나라", 13000, "🍝"));
        items.add(createMenuItem("햄버거", 10000, "🍔"));
        items.add(createMenuItem("치킨버거", 11000, "🍔"));
        items.add(createMenuItem("피자", 15000, "🍕"));
        items.add(createMenuItem("샐러드", 8000, "🥗"));

        return items;
    }

    /**
     * 게임 메뉴 아이템
     */
    private List<Map<String, Object>> getGameItems() {
        List<Map<String, Object>> items = new ArrayList<>();

        items.add(createMenuItem("맞춤법 게임", 0, "🎮"));
        items.add(createMenuItem("숫자 맞추기", 0, "🎯"));
        items.add(createMenuItem("동물 맞추기", 0, "🐾"));
        items.add(createMenuItem("색상 맞추기", 0, "🎨"));
        items.add(createMenuItem("스피드 게임", 0, "⚡"));
        items.add(createMenuItem("퀴즈 게임", 0, "❓"));

        return items;
    }

    /**
     * 메뉴 아이템 생성 헬퍼
     */
    private Map<String, Object> createMenuItem(String name, Integer price, String emoji) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("itemName", name);
        item.put("price", price);
        item.put("emoji", emoji);
        return item;
    }

    // 활동 없음을 감지하면 화면 보호 모드로 이동
    @GetMapping("/screensaver")
    public String screensaver() {
        // templates/kiosk/screensaver.html 파일을 호출합니다.
        return "kiosk/screensaver";
    }
}
