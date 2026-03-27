package org.example.board_cafe_kiosk_2603.domain.admin.product;

import lombok.*;

/**
 * category 테이블과 1:1 매핑되는 도메인(VO) 클래스
 * menu, game, gameItem 이 공통으로 참조하는 대분류 카테고리
 */
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private int id;
    private String name;

    /* DRINK / FOOD / GAME / GUEST */
    private CategoryType type;
}
