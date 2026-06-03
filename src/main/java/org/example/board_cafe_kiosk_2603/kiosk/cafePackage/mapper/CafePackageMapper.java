package org.example.board_cafe_kiosk_2603.kiosk.cafePackage.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.board_cafe_kiosk_2603.kiosk.cafePackage.model.CafePackage;

import java.util.List;

@Mapper

/*
 * 작성자 : 김민기
 * 기능 : 카페 패키지 데이터 접근 인터페이스
 * 날짜 : 2026-03-27
 */
public interface CafePackageMapper {

    /**
     * 활성 데이터 목록 조회합니다.
     *
     * @return 처리 결과
     */
    List<CafePackage> findAllActive();

    /**
     * ID로 단건 조회합니다.
     *
     * @param id 전달받은 id 값
     * @return 처리 결과
     */
    CafePackage findById(int id);
}
