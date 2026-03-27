package org.example.board_cafe_kiosk_2603.service.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.product.Game;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.GameResponseDTO;
import org.example.board_cafe_kiosk_2603.repository.admin.product.GameMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * GameService 구현체
 * ModelMapper를 사용하여 Domain ↔ DTO 변환 처리
 */
@Log4j2
@Service
@RequiredArgsConstructor
class GameServiceImpl implements GameService {

    private final GameMapper gameMapper;
    private final ModelMapper modelMapper;

    /**
     * 전체 게임 목록 조회
     */
    @Override
    public List<GameResponseDTO> getAll() {
        log.debug("GameServiceImpl.getAll() 실행");
        List<GameResponseDTO> list = gameMapper.findAll();
        log.debug("조회된 게임 수: {}", list.size());
        return list;
    }

    /**
     * category_id 기준 게임 목록 조회
     */
    @Override
    public List<GameResponseDTO> getByCategoryId(int categoryId) {
        log.debug("GameServiceImpl.getByCategoryId() 실행 - categoryId: {}", categoryId);
        List<GameResponseDTO> list = gameMapper.findByCategoryId(categoryId);
        log.debug("조회된 게임 수 (categoryId={}): {}", categoryId, list.size());
        return list;
    }

    /**
     * 활성 여부 기준 게임 목록 조회
     */
    @Override
    public List<GameResponseDTO> getByIsActive(boolean isActive) {
        log.debug("GameServiceImpl.getByIsActive() 실행 - isActive: {}", isActive);
        List<GameResponseDTO> list = gameMapper.findByIsActive(isActive);
        log.debug("조회된 게임 수 (isActive={}): {}", isActive, list.size());
        return list;
    }

    /**
     * PK로 게임 단건 조회
     */
    @Override
    public GameResponseDTO getById(int id) {
        log.debug("GameServiceImpl.getById() 실행 - id: {}", id);
        return gameMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("게임 없음 - id: {}", id);
                    return new NoSuchElementException("게임을 찾을 수 없습니다. id=" + id);
                });
    }

    /**
     * 게임 등록
     */
    @Override
    public void register(GameRequestDTO gameRequestDTO) {
        log.debug("GameServiceImpl.register() 실행 - dto: {}", gameRequestDTO);
        Game game = modelMapper.map(gameRequestDTO, Game.class);
        int result = gameMapper.insert(game);
        log.debug("게임 등록 결과 - affected rows: {}, generated id: {}", result, game.getId());
    }

    /**
     * 게임 수정 (존재 여부 선확인)
     */
    @Override
    public void modify(int id, GameRequestDTO gameRequestDTO) {
        log.debug("GameServiceImpl.modify() 실행 - id: {}, dto: {}", id, gameRequestDTO);
        gameMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("수정 대상 게임 없음 - id: {}", id);
                    return new NoSuchElementException("게임을 찾을 수 없습니다. id=" + id);
                });
        Game game = Game.builder()
                .id(id)
                .categoryId(gameRequestDTO.getCategoryId())
                .name(gameRequestDTO.getName())
                .minPlayers(gameRequestDTO.getMinPlayers())
                .maxPlayers(gameRequestDTO.getMaxPlayers())
                .playTime(gameRequestDTO.getPlayTime())
                .isActive(gameRequestDTO.isActive())
                .build();
        int result = gameMapper.update(game);
        log.debug("게임 수정 결과 - affected rows: {}", result);
    }

    /**
     * 게임 삭제 (game_item ON DELETE CASCADE 로 자동 삭제, 존재 여부 선확인)
     */
    @Override
    public void remove(int id) {
        log.debug("GameServiceImpl.remove() 실행 - id: {}", id);
        gameMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("삭제 대상 게임 없음 - id: {}", id);
                    return new NoSuchElementException("게임을 찾을 수 없습니다. id=" + id);
                });
        int result = gameMapper.delete(id);
        log.debug("게임 삭제 결과 - affected rows: {}", result);
    }

    /**
     * 게임 활성 상태 토글 (is_active 반전, 존재 여부 선확인)
     */
    @Override
    public void toggleActive(int id) {
        log.debug("GameServiceImpl.toggleActive() 실행 - id: {}", id);
        gameMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("토글 대상 게임 없음 - id: {}", id);
                    return new NoSuchElementException("게임을 찾을 수 없습니다. id=" + id);
                });
        int result = gameMapper.toggleActive(id);
        log.debug("게임 활성 상태 토글 결과 - affected rows: {}", result);
    }
}