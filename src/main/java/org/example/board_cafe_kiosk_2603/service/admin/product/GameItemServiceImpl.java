package org.example.board_cafe_kiosk_2603.service.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.product.GameItem;
import org.example.board_cafe_kiosk_2603.domain.admin.product.GameItemStatus;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameItemRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameItemResponseDTO;
import org.example.board_cafe_kiosk_2603.mapper.admin.product.GameItemMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * GameItemService 구현체
 * ModelMapper를 사용하여 Domain ↔ DTO 변환 처리
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class GameItemServiceImpl implements GameItemService {

    private final GameItemMapper gameItemMapper;
    private final ModelMapper modelMapper;

    /**
     * 전체 게임 아이템 목록 조회
     */
    @Override
    public List<GameItemResponseDTO> getAll() {
        log.debug("GameItemServiceImpl.getAll() 실행");
        List<GameItemResponseDTO> list = gameItemMapper.findAll();
        log.debug("조회된 게임 아이템 수: {}", list.size());
        return list;
    }

    /**
     * game_id 기준 게임 아이템 목록 조회
     */
    @Override
    public List<GameItemResponseDTO> getByGameId(int gameId) {
        log.debug("GameItemServiceImpl.getByGameId() 실행 - gameId: {}", gameId);
        List<GameItemResponseDTO> list = gameItemMapper.findByGameId(gameId);
        log.debug("조회된 게임 아이템 수 (gameId={}): {}", gameId, list.size());
        return list;
    }

    /**
     * status 기준 게임 아이템 목록 조회
     */
    @Override
    public List<GameItemResponseDTO> getByStatus(GameItemStatus gameItemStatus) {
        log.debug("GameItemServiceImpl.getByStatus() 실행 - gameItemStatus: {}", gameItemStatus);
        List<GameItemResponseDTO> list = gameItemMapper.findByStatus(gameItemStatus);
        log.debug("조회된 게임 아이템 수 (status={}): {}", gameItemStatus, list.size());
        return list;
    }

    /**
     * PK로 게임 아이템 단건 조회
     */
    @Override
    public GameItemResponseDTO getById(int id) {
        log.debug("GameItemServiceImpl.getById() 실행 - id: {}", id);
        return gameItemMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("게임 아이템 없음 - id: {}", id);
                    return new NoSuchElementException("게임 아이템을 찾을 수 없습니다. id=" + id);
                });
    }

    /**
     * 게임 아이템 등록
     */
    @Override
    public void register(GameItemRequestDTO gameItemRequestDTO) {
        log.debug("GameItemServiceImpl.register() 실행 - gameItemRequestDTO: {}", gameItemRequestDTO);
        GameItem gameItem = modelMapper.map(gameItemRequestDTO, GameItem.class);
        int result = gameItemMapper.insert(gameItem);
        log.debug("게임 아이템 등록 결과 - affected rows: {}, generated id: {}", result, gameItem.getId());
    }

    /**
     * 게임 아이템 수정 (존재 여부 선확인)
     */
    @Override
    public void modify(int id, GameItemRequestDTO gameItemRequestDTO) {
        log.debug("GameItemServiceImpl.modify() 실행 - id: {}, dto: {}", id, gameItemRequestDTO);
        gameItemMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("수정 대상 게임 아이템 없음 - id: {}", id);
                    return new NoSuchElementException("게임 아이템을 찾을 수 없습니다. id=" + id);
                });
        GameItem gameItem = GameItem.builder()
                .id(id)
                .gameId(gameItemRequestDTO.getGameId())
                .serialNumber(gameItemRequestDTO.getSerialNumber())
                .status(gameItemRequestDTO.getStatus())
                .build();
        int result = gameItemMapper.update(gameItem);
        log.debug("게임 아이템 수정 결과 - affected rows: {}", result);
    }

    /**
     * 게임 아이템 삭제 (존재 여부 선확인)
     */
    @Override
    public void remove(int id) {
        log.debug("GameItemServiceImpl.remove() 실행 - id: {}", id);
        gameItemMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("삭제 대상 게임 아이템 없음 - id: {}", id);
                    return new NoSuchElementException("게임 아이템을 찾을 수 없습니다. id=" + id);
                });
        int result = gameItemMapper.delete(id);
        log.debug("게임 아이템 삭제 결과 - affected rows: {}", result);
    }

    /**
     * 게임 아이템 상태 변경 (존재 여부 선확인)
     */
    @Override
    public void changeStatus(int id, GameItemStatus status) {
        log.debug("GameItemServiceImpl.changeStatus() 실행 - id: {}, status: {}", id, status);
        gameItemMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("상태 변경 대상 게임 아이템 없음 - id: {}", id);
                    return new NoSuchElementException("게임 아이템을 찾을 수 없습니다. id=" + id);
                });
        int result = gameItemMapper.updateStatus(id, status);
        log.debug("게임 아이템 상태 변경 결과 - affected rows: {}", result);
    }
}
