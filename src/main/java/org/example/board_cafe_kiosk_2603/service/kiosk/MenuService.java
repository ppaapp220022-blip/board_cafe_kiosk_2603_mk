package org.example.board_cafe_kiosk_2603.service.kiosk;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.kiosk.Game;
import org.example.board_cafe_kiosk_2603.domain.kiosk.Menu;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.GameMapper;
import org.example.board_cafe_kiosk_2603.mapper.kiosk.MenuMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 키오스크 메뉴 목록 데이터를 제공하는 서비스.
 * DB의 menu 테이블 + category 테이블을 기반으로 조회합니다.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;
    private final GameMapper gameMapper;

    // ===================================================
    // 음료 메뉴 (category.type = 'DRINK')
    // ===================================================
    public List<Map<String, Object>> getDrinkItems() {
        return menuMapper.findByCategoryType("DRINK").stream()
                .map(m -> toMenuMap(m, "☕"))
                .collect(Collectors.toList());
    }

    // ===================================================
    // 음식 메뉴 (category.type = 'FOOD')
    // ===================================================
    public List<Map<String, Object>> getFoodItems() {
        return menuMapper.findByCategoryType("FOOD").stream()
                .map(m -> toMenuMap(m, "🍽️"))
                .collect(Collectors.toList());
    }

    // ===================================================
    // 게임 메뉴 (game 테이블 + game_item stock 집계)
    // ===================================================
    public List<Map<String, Object>> getGameItems() {
        return gameMapper.findAllActive().stream()
                .map(this::toGameMap)
                .collect(Collectors.toList());
    }

    // ===================================================
    // 헬퍼
    // ===================================================

    private Map<String, Object> toMenuMap(Menu menu, String defaultEmoji) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("itemName",  menu.getName());
        item.put("menuName",  menu.getName());
        item.put("price",     menu.getPrice());
        item.put("menuPrice", menu.getPrice());
        item.put("emoji",     defaultEmoji);
        item.put("stock",     -1);  // 음료·음식 재고 개념 없음
        return item;
    }

    private Map<String, Object> toGameMap(Game game) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("itemName",  game.getName());
        item.put("menuName",  game.getName());
        item.put("price",     0);              // 게임 대여는 무료
        item.put("menuPrice", 0);
        item.put("emoji",     "🎮");
        item.put("stock",     game.getStock()); // NORMAL 재고 수
        return item;
    }
}
