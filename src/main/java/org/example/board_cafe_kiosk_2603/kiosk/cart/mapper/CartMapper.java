package org.example.board_cafe_kiosk_2603.kiosk.cart.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.kiosk.cart.model.Cart;

@Mapper

/*
 * 작성자 : 김민기
 * 기능 : 장바구니 데이터 접근 인터페이스
 * 날짜 : 2026-03-27
 */
public interface CartMapper {

    /**
     * 테이블 번호로 테이블 ID 조회합니다.
     *
     * @param tableNumber 전달받은 tableNumber 값
     * @return 처리 결과
     */
    Integer findCafeTableIdByTableNumber(int tableNumber);

    /**
     * 테이블 ID 기준 조회합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @return 처리 결과
     */
    Cart findByTableId(int tableId);

    /**
     * 데이터 등록합니다.
     *
     * @param cart 전달받은 cart 값
     */
    void insert(Cart cart);

    /**
     * 테이블 기준 장바구니 삭제합니다.
     *
     * @param tableId 전달받은 tableId 값
     */
    void deleteByTableId(int tableId);

    /**
     * 장바구니 갱신 시각 수정합니다.
     *
     * @param cartId 전달받은 cartId 값
     */
    void updateTimestamp(int cartId);
}
