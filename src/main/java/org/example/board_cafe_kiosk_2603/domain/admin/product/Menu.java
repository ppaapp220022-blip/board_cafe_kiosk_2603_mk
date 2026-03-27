package org.example.board_cafe_kiosk_2603.domain.admin.product;

import lombok.*;

import java.time.LocalDateTime;

/**
 * menu 테이블과 1:1 매핑되는 도메인(VO) 클래스
 * 음료·음식·인원추가 상품 정보를 담으며, category 테이블을 FK로 참조
 */
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    private int id;

    /** FK → category.id (ON DELETE SET NULL) */
    private Integer categoryId;

    private String name;
    private int price;
    private String description;
    private String imageUrl;

    /** 판매 가능 여부 (true: 판매중 / false: 판매중지) */
    private boolean isAvailable;

    /** 소프트 삭제 여부 (true: 삭제됨) */
    private boolean isDeleted;

    private LocalDateTime createdAt;
}
