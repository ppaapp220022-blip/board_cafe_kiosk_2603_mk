package org.example.board_cafe_kiosk_2603.service.admin.macro;

import org.example.board_cafe_kiosk_2603.dto.admin.MacroMessageResponseDTO;

import java.util.List;

public interface MacroMessageService {
    // 활성화된 모든 매크로 메시지 조회
    List<MacroMessageResponseDTO> getAllActiveMessages();
}
