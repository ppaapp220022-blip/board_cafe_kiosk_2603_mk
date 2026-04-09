package org.example.board_cafe_kiosk_2603.service.admin.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.manager.Manager;
import org.example.board_cafe_kiosk_2603.dto.admin.manager.ManagerRequest;
import org.example.board_cafe_kiosk_2603.dto.admin.manager.ManagerResponse;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageResponseDTO;
import org.example.board_cafe_kiosk_2603.mapper.admin.manager.ManagerMapper;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ManagerServiceImpl implements ManagerService {

    private final ManagerMapper managerMapper;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    // 직원 등록 - Request → VO 변환 후 insert
    @Override
    public void createManager(ManagerRequest request) {

        // 등록 직전 최종 중복 검사 (방어적 코드)
        if (isLoginIdDuplicate(request.getLoginId())) {
            log.error("중복된 아이디 등록 시도: {}", request.getLoginId());
            throw new RuntimeException("이미 사용 중인 아이디입니다.");
        }

        Manager manager = Manager.builder()
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword())) // BCrypt 암호화
                .name(request.getName())
                .role(request.getRole())
                .isActive(true) // 등록 시 기본값 활성
                .build();

        managerMapper.insert(manager);
    }

    // 활성/비활성 토글
    @Override
    public void updateActive(int id, boolean isActive) {
        managerMapper.updateActive(id, isActive);
    }

    // 아이디 중복 확인
    @Override
    public boolean isLoginIdDuplicate(String loginId) {
        // 매퍼를 통해 해당 아이디로 등록된 관리자가 있는지 확인합니다.
        // 존재하면(isPresent) true(중복됨), 없으면 false(사용 가능)를 반환합니다.
        return managerMapper.findByLoginId(loginId).isPresent();
    }

    // 아이디로 직원 조회 (프로필 조회용) - VO를 Response DTO로 변환하여 반환
    @Override
    public Optional<ManagerResponse> findByLoginId(String loginId) {
        return managerMapper.findByLoginId(loginId)
                .map(vo -> modelMapper.map(vo, ManagerResponse.class));
    }

    // 내 정보 수정 처리
    @Override
    public void updateProfile(String loginId, ManagerRequest request) {
        // 1. 기존 사용자 정보 조회 (없으면 예외 발생)
        Manager manager = managerMapper.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("수정할 사용자 정보를 찾을 수 없습니다."));

        // 2. 수정할 데이터 준비 (이름은 필수)
        String newName = request.getName();

        // 3. 비밀번호 처리 로직
        // HTML에서 새 비밀번호를 입력하지 않았다면(빈 문자열) 기존 비밀번호 유지
        String finalPassword = manager.getPassword();
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            log.info("비밀번호 변경 감지 - 암호화 진행");
            finalPassword = passwordEncoder.encode(request.getPassword());
        }

        // 4. 매퍼 호출하여 DB 업데이트
        // (참고: ManagerMapper 인터페이스에 updateProfileInfo 메서드가 정의되어 있어야 합니다)
        managerMapper.updateProfileInfo(loginId, newName, finalPassword);

        log.info("사용자 프로필 업데이트 완료: {}", loginId);
    }

    /*================페이징============== */
    @Override
    public PageResponseDTO<ManagerResponse> getPagedManagers(PageRequestDTO pageRequestDTO) {
        List<ManagerResponse> dtoList = managerMapper.selectList(pageRequestDTO).stream()
                .map(vo -> modelMapper.map(vo, ManagerResponse.class))
                .collect(Collectors.toList());

        int total = managerMapper.selectCount(pageRequestDTO);

        return PageResponseDTO.<ManagerResponse>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }

    @Override
    public int getCount(PageRequestDTO pageRequestDTO) {
        return managerMapper.selectCount(pageRequestDTO);
    }
}
