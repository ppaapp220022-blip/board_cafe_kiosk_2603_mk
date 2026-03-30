//package org.example.board_cafe_kiosk_2603.service.kiosk;
//
//import org.springframework.stereotype.Service;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 키오스크 메뉴 목록 서비스 (더미 데이터 — DB dummy.sql 기준).
// *
// * ※ 음료/음식: menu 테이블과 name/price 완전 일치 필요 (cart_item.menu_id FK 조회용)
// * ※ 게임: game 테이블 기준, menu_id=NULL 허용 → CartController에서 별도 처리
// *
// * kiosk_layout.html 기대 필드: menuName / menuPrice / emoji / stock
// *   stock: -1 = 무제한, 0 = 품절, 양수 = 재고 수
// */
//@Service
//public class MenuService {
//
//    // ── 음료 (category_id 1,2 / menu 테이블 기준) ──────────────────
//    public List<Map<String, Object>> getDrinkItems() {
//        return List.of(
//                item("아메리카노",          3000, "☕",  -1),
//                item("카페라떼",            3500, "☕",  -1),
//                item("카푸치노",            3500, "☕",  -1),
//                item("바닐라라떼",          4000, "☕",  -1),
//                item("레몬에이드",          4000, "🍋",  -1),
//                item("자몽에이드",          4000, "🍊",  -1),
//                item("녹차라떼",            3500, "🍵",  -1),
//                item("유자차",              3500, "🍵",  -1)
//        );
//    }
//
//    // ── 음식 (category_id 3,4 / menu 테이블 기준) ──────────────────
//    public List<Map<String, Object>> getFoodItems() {
//        return List.of(
//                item("팝콘 (오리지널)",      2000, "🍿",  -1),
//                item("팝콘 (카라멜)",        2500, "🍿",  -1),
//                item("나초 + 살사소스",      3000, "🌮",  -1),
//                item("믹스 너트",            3500, "🥜",  -1),
//                item("토스트 세트",          5000, "🍞",  -1),
//                item("컵라면",               1500, "🍜",  -1)
//        );
//    }
//
//    // ── 게임 (game 테이블 기준 — menu 테이블과 무관) ───────────────
//    public List<Map<String, Object>> getGameItems() {
//        return List.of(
//                item("맞춤법 게임",  0, "🎮",  3),
//                item("숫자 맞추기", 0, "🎯",  2),
//                item("동물 맞추기", 0, "🐾",  0),
//                item("색상 맞추기", 0, "🎨",  1),
//                item("스피드 게임", 0, "⚡",  0),
//                item("퀴즈 게임",   0, "❓",  4)
//        );
//    }
//
//    private Map<String, Object> item(String menuName, int menuPrice, String emoji, int stock) {
//        Map<String, Object> m = new LinkedHashMap<>();
//        m.put("menuName",  menuName);
//        m.put("menuPrice", menuPrice);
//        m.put("emoji",     emoji);
//        m.put("stock",     stock);
//        return m;
//    }
//}
