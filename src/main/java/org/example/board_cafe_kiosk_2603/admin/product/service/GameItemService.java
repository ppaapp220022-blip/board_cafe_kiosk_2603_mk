package org.example.board_cafe_kiosk_2603.admin.product.service;

import org.example.board_cafe_kiosk_2603.admin.product.model.GameItemStatus;
import org.example.board_cafe_kiosk_2603.admin.product.dto.GameItemRequestDTO;
import org.example.board_cafe_kiosk_2603.admin.product.dto.GameItemResponseDTO;

import java.util.List;
import java.util.Map;

public interface GameItemService {

    List<GameItemResponseDTO> getAll();

    List<GameItemResponseDTO> getByGameId(int gameId);

    List<GameItemResponseDTO> getByStatus(GameItemStatus gameItemStatus);

    /**
     * ID로 단건 조회합니다.
     *
     * @param id 전달받은 id 값
     * @return 처리 결과
     */
    GameItemResponseDTO getById(int id);

    /**
     * 데이터 등록합니다.
     *
     * @param gameItemRequestDTO 전달받은 gameItemRequestDTO 값
     */
    void register(GameItemRequestDTO gameItemRequestDTO);

    /**
     * 데이터 수정합니다.
     *
     * @param id 전달받은 id 값
     * @param gameItemRequestDTO 전달받은 gameItemRequestDTO 값
     */
    void modify(int id, GameItemRequestDTO gameItemRequestDTO);

    void remove(int id);

    void changeStatus(int id, GameItemStatus gameItemStatus);

    /**
     * 게임명 기준 대여 가능 재고 조회합니다.
     *
     * @param gameName 전달받은 gameName 값
     * @return 처리 결과
     */
    List<GameItemResponseDTO> getAvailableByGameName(String gameName);

    /**
     * 주문에 게임 재고 할당 작업을 수행합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @param orderId 전달받은 orderId 값
     * @param gameName 전달받은 gameName 값
     * @param gameItemIds 전달받은 gameItemIds 값
     */
    void assignGameItemsToOrder(int tableId, int orderId, String gameName, List<Integer> gameItemIds);

    /**
     * 테이블의 활성 대여 게임 조회합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @return 처리 결과
     */
    List<Map<String, Object>> getActiveGameRentalsByTable(int tableId);

    /**
     * 테이블의 게임 대여 이력 조회합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @return 처리 결과
     */
    List<Map<String, Object>> getGameRentalHistoryByTable(int tableId);

    /**
     * 게임 대여 정산 처리합니다.
     *
     * @param tableId 전달받은 tableId 값
     * @param updates 전달받은 updates 값
     */
    void settleGameRentals(int tableId, List<Map<String, Object>> updates);
}
