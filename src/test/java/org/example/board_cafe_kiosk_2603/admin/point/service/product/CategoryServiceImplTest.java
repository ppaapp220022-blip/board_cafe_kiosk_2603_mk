//package org.example.board_cafe_kiosk_2603.admin.product.service;
//
//import lombok.extern.log4j.Log4j2;
//import org.example.board_cafe_kiosk_2603.admin.product.model.CategoryType;
//import org.example.board_cafe_kiosk_2603.admin.product.dto.CategoryRequestDTO;
//import org.example.board_cafe_kiosk_2603.admin.product.dto.CategoryResponseDTO;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@Log4j2
//@SpringBootTest
//@Transactional
//class CategoryServiceImplTest {
//
//    @Autowired
//    private CategoryService categoryService;
//
//    @Test
//    void getAllTest() {
//        List<CategoryResponseDTO> list = categoryService.getAll();
//        list.forEach(category -> log.info(category));
//    }
//
//    @Test
//    void getByTypeTest() {
//        List<CategoryResponseDTO> list = categoryService.getByType(CategoryType.GAME);
//        list.forEach(category -> log.info(category));
//    }
//
//    @Test
//    void getByIdTest() {
//        CategoryResponseDTO category = categoryService.getById(1);
//        log.info(category);
//    }
//
//    @Test
//    void registerTest() {
//        CategoryRequestDTO categoryRequestDTO = CategoryRequestDTO.builder()
//                .name("테스트카테고리_" + System.currentTimeMillis())
//                .type(CategoryType.GAME)
//                .build();
//        categoryService.register(categoryRequestDTO);
//        log.info("register 완료");
//    }
//
//    @Test
//    void modifyTest() {
//        CategoryRequestDTO categoryRequestDTO = CategoryRequestDTO.builder()
//                .name("수정된카테고리")
//                .type(CategoryType.FOOD)
//                .build();
//        categoryService.modify(1, categoryRequestDTO);
//        log.info("modify 완료");
//    }
//
//    @Test
//    void removeTest() {
//        CategoryRequestDTO categoryRequestDTO = CategoryRequestDTO.builder()
//                .name("삭제카테고리_" + System.currentTimeMillis())
//                .type(CategoryType.GAME)
//                .build();
//        categoryService.register(categoryRequestDTO);
//        CategoryResponseDTO created = categoryService.getAll().stream()
//                .filter(category -> category.getName().equals(categoryRequestDTO.getName()))
//                .findFirst()
//                .orElseThrow(() -> new IllegalStateException("생성한 카테고리를 찾지 못했습니다."));
//        categoryService.remove(created.getId());
//        log.info("remove 완료");
//    }
//}
