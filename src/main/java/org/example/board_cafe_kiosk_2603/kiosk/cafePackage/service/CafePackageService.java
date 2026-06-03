package org.example.board_cafe_kiosk_2603.kiosk.cafePackage.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.kiosk.cafePackage.model.CafePackage;
import org.example.board_cafe_kiosk_2603.kiosk.cafePackage.dto.CafePackageDTO;
import org.example.board_cafe_kiosk_2603.kiosk.cafePackage.mapper.CafePackageMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/*
 * 작성자 : 김민기
 * 기능 : CafePackage 서비스 인터페이스
 * 날짜 : 2026-03-27
 */

@Log4j2
@Service
@RequiredArgsConstructor
public class CafePackageService {

    private final CafePackageMapper cafePackageMapper;
    private final ModelMapper       modelMapper;
    /**
     * 활성화된 패키지 목록 조회합니다.
     *
     * @return 처리 결과
     */

    public List<CafePackageDTO> getActivePackages() {
        return cafePackageMapper.findAllActive()
                .stream()
                .map(p -> modelMapper.map(p, CafePackageDTO.class))
                .collect(Collectors.toList());
    }
    /**
     * ID로 단건 조회합니다.
     *
     * @param id 전달받은 id 값
     * @return 처리 결과
     */

    public CafePackageDTO getById(int id) {
        CafePackage pkg = cafePackageMapper.findById(id);
        return pkg != null ? modelMapper.map(pkg, CafePackageDTO.class) : null;
    }
}
