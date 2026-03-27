package org.example.board_cafe_kiosk_2603.service.admin.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.product.Category;
import org.example.board_cafe_kiosk_2603.domain.admin.product.CategoryType;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.admin.product.CategoryResponseDTO;
import org.example.board_cafe_kiosk_2603.repository.admin.product.CategoryMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * CategoryService 구현체
 * ModelMapper를 사용하여 Domain ↔ DTO 변환 처리
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final ModelMapper modelMapper;

    /**
     * 카테고리 전체 목록 조회
     */
    @Override
    public List<CategoryResponseDTO> getAll() {
        log.debug("CategoryServiceImpl.getAll() 실행");
        List<Category> list = categoryMapper.findAll();
        log.debug("조회된 카테고리 수: {}", list.size());
        return list.stream()
                .map(c -> modelMapper.map(c, CategoryResponseDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * type 기준 카테고리 목록 조회
     */
    @Override
    public List<CategoryResponseDTO> getByType(CategoryType type) {
        log.debug("CategoryServiceImpl.getByType() 실행 - type: {}", type);
        List<Category> list = categoryMapper.findByType(type);
        log.debug("조회된 카테고리 수 (type={}): {}", type, list.size());
        return list.stream()
                .map(c -> modelMapper.map(c, CategoryResponseDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * PK로 카테고리 단건 조회
     */
    @Override
    public CategoryResponseDTO getById(int id) {
        log.debug("CategoryServiceImpl.getById() 실행 - id: {}", id);
        Category category = categoryMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("카테고리 없음 - id: {}", id);
                    return new NoSuchElementException("카테고리를 찾을 수 없습니다. id=" + id);
                });
        return modelMapper.map(category, CategoryResponseDTO.class);
    }

    /**
     * 카테고리 등록
     */
    @Override
    public void register(CategoryRequestDTO categoryRequestDTO) {
        log.debug("CategoryServiceImpl.register() 실행 - dto: {}", categoryRequestDTO);
        Category category = modelMapper.map(categoryRequestDTO, Category.class);
        int result = categoryMapper.insert(category);
        log.debug("카테고리 등록 결과 - affected rows: {}, generated id: {}", result, category.getId());
    }

    /**
     * 카테고리 수정
     */
    @Override
    public void modify(int id, CategoryRequestDTO categoryRequestDTO) {
        log.debug("CategoryServiceImpl.modify() 실행 - id: {}, dto: {}", id, categoryRequestDTO);
        categoryMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("수정 대상 카테고리 없음 - id: {}", id);
                    return new NoSuchElementException("카테고리를 찾을 수 없습니다. id=" + id);
                });
        Category category = Category.builder()
                .id(id)
                .name(categoryRequestDTO.getName())
                .type(categoryRequestDTO.getType())
                .build();
        int result = categoryMapper.update(category);
        log.debug("카테고리 수정 결과 - affected rows: {}", result);
    }

    /**
     * 카테고리 삭제
     */
    @Override
    public void remove(int id) {
        log.debug("CategoryServiceImpl.remove() 실행 - id: {}", id);
        categoryMapper.findById(id)
                .orElseThrow(() -> {
                    log.warn("삭제 대상 카테고리 없음 - id: {}", id);
                    return new NoSuchElementException("카테고리를 찾을 수 없습니다. id=" + id);
                });
        int result = categoryMapper.delete(id);
        log.debug("카테고리 삭제 결과 - affected rows: {}", result);
    }
}
