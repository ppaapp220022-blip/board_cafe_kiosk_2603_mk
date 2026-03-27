package org.example.board_cafe_kiosk_2603.service.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.product.Menu;
import org.example.board_cafe_kiosk_2603.dto.admin.product.MenuRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.MenuResponseDTO;
import org.example.board_cafe_kiosk_2603.repository.admin.product.MenuMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * MenuService 구현체
 * ModelMapper를 사용하여 Domain ↔ DTO 변환 처리
 */
@Log4j2
@Service
@RequiredArgsConstructor  // 생성자 주입을 자동으로 함 (final 필드 대상)
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final ModelMapper modelMapper;

    /**
     * 전체 메뉴 목록 조회 (소프트 삭제 제외)
     */
    @Override
    public List<MenuResponseDTO> getAll() {
        log.debug("MenuServiceImpl.getAll() 실행");
        List<MenuResponseDTO> list = menuMapper.findAll();
        log.debug("조회된 메뉴 수: {}", list.size());
        return list;
    }

    /**
     * category_id 기준 메뉴 목록 조회
     */
    @Override
    public List<MenuResponseDTO> getByCategoryId(int categoryId) {
        log.debug("MenuServiceImpl.getByCategoryId() 실행 - categoryId: {}", categoryId);
        List<MenuResponseDTO> list = menuMapper.findByCategoryId(categoryId);
        log.debug("조회된 메뉴 수 (categoryId={}): {}", categoryId, list.size());
        return list;
    }

    /**
     * category type 기준 메뉴 목록 조회 (FOOD / DRINK / GUEST)
     */
    @Override
    public List<MenuResponseDTO> getByType(String type) {
        log.debug("MenuServiceImpl.getByType() 실행 - type: {}", type);
        List<MenuResponseDTO> list = menuMapper.findByType(type);
        log.debug("조회된 메뉴 수 (type={}): {}", type, list.size());
        return list;
    }

    /**
     * 판매 가능 여부 기준 메뉴 목록 조회
     */
    @Override
    public List<MenuResponseDTO> getByIsAvailable(boolean isAvailable) {
        log.debug("MenuServiceImpl.getByIsAvailable() 실행 - isAvailable: {}", isAvailable);
        List<MenuResponseDTO> list = menuMapper.findByIsAvailable(isAvailable);
        log.debug("조회된 메뉴 수 (isAvailable={}): {}", isAvailable, list.size());
        return list;
    }

    /**
     * PK로 메뉴 단건 조회
     */
    @Override
    public MenuResponseDTO getById(int id) {
        log.debug("MenuServiceImpl.getById() 실행 - id: {}", id);
        return menuMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("메뉴 없음 - id: {}", id);
                    return new NoSuchElementException("메뉴를 찾을 수 없습니다. id=" + id);
                });
    }

    /**
     * 메뉴 등록 - Builder 패턴으로 Menu VO 생성
     */
    @Override
    public void register(MenuRequestDTO dto) {
        log.debug("MenuServiceImpl.register() 실행 - dto: {}", dto);
        Menu menu = Menu.builder()
                .categoryId(dto.getCategoryId())
                .name(dto.getName())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .isAvailable(dto.isAvailable())
                .build();
        int result = menuMapper.insert(menu);
        log.debug("메뉴 등록 결과 - affected rows: {}, generated id: {}", result, menu.getId());
    }

    /**
     * 메뉴 수정 - Builder로 id 포함 Menu VO 재생성
     */
    @Override
    public void modify(int id, MenuRequestDTO dto) {
        log.debug("MenuServiceImpl.modify() 실행 - id: {}, dto: {}", id, dto);
        menuMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("수정 대상 메뉴 없음 - id: {}", id);
                    return new NoSuchElementException("메뉴를 찾을 수 없습니다. id=" + id);
                });
        Menu menu = Menu.builder()
                .id(id)
                .categoryId(dto.getCategoryId())
                .name(dto.getName())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .isAvailable(dto.isAvailable())
                .build();
        int result = menuMapper.update(menu);
        log.debug("메뉴 수정 결과 - affected rows: {}", result);
    }

    /**
     * 메뉴 소프트 삭제 (is_deleted = true)
     */
    @Override
    public void remove(int id) {
        log.debug("MenuServiceImpl.remove() 실행 - id: {}", id);
        menuMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("삭제 대상 메뉴 없음 - id: {}", id);
                    return new NoSuchElementException("메뉴를 찾을 수 없습니다. id=" + id);
                });
        int result = menuMapper.softDelete(id);
        log.debug("메뉴 소프트 삭제 결과 - affected rows: {}", result);
    }

    /**
     * 메뉴 판매 상태 토글 (is_available 반전)
     */
    @Override
    public void toggleAvailable(int id) {
        log.debug("MenuServiceImpl.toggleAvailable() 실행 - id: {}", id);
        menuMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("토글 대상 메뉴 없음 - id: {}", id);
                    return new NoSuchElementException("메뉴를 찾을 수 없습니다. id=" + id);
                });
        int result = menuMapper.toggleAvailable(id);
        log.debug("메뉴 판매 상태 토글 결과 - affected rows: {}", result);
    }
}