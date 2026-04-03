package org.example.board_cafe_kiosk_2603.service.admin.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.policy.Policy;
import org.example.board_cafe_kiosk_2603.dto.admin.policy.PolicyDTO;
import org.example.board_cafe_kiosk_2603.mapper.admin.policy.PolicyMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyMapper policyMapper;

    @Override
    public List<PolicyDTO> getAllPolicies(int page, int size, String filter) {
        int offset = (page - 1) * size;
        return policyMapper.findAll(offset, size, filter).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int countAll(String filter) {
        return policyMapper.countAll(filter);
    }

    @Override
    public PolicyDTO getById(int id) {
        Policy policy = policyMapper.findById(id);
        return policy != null ? toDTO(policy) : null;
    }

    @Override
    public void insert(PolicyDTO policyDTO) {
        policyMapper.insert(toEntity(policyDTO));
        log.info("패키지 등록 완료 - name: {}, type: {}, price: {}",
                policyDTO.getName(), policyDTO.getType(), policyDTO.getBasePrice());
    }

    @Override
    public void update(PolicyDTO policyDTO) {
        policyMapper.update(toEntity(policyDTO));
        log.info("패키지 수정 완료 - id: {}, name: {}", policyDTO.getId(), policyDTO.getName());
    }

    @Override
    public void updateStatus(int id, boolean active) {
        policyMapper.updateStatus(id, active);
        log.info("패키지 상태 변경 - id: {}, active: {}", id, active);
    }

    // ===================================================
    // 헬퍼
    // ===================================================

    private PolicyDTO toDTO(Policy policy) {
        return PolicyDTO.builder()
                .id(policy.getId())
                .name(policy.getName())
                .type(policy.getType())
                .durationMinutes(policy.getDurationMinutes())
                .basePrice(policy.getBasePrice())
                .extraPricePerMin(policy.getExtraPricePerMin())
                .active(policy.isActive())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }

    private Policy toEntity(PolicyDTO dto) {
        return Policy.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .durationMinutes(dto.getDurationMinutes())
                .basePrice(dto.getBasePrice())
                .extraPricePerMin(dto.getExtraPricePerMin())
                .active(dto.isActive())
                .build();
    }
}