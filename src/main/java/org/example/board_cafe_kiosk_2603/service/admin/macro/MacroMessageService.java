package org.example.board_cafe_kiosk_2603.service.admin.macro;

import org.example.board_cafe_kiosk_2603.dto.admin.macro.MacroMessageResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageResponseDTO;

import java.util.List;

public interface MacroMessageService {
    // 활성화된 모든 매크로 메시지 조회
    List<MacroMessageResponseDTO> getAllActiveMessages();

    // direction별 페이징 조회
    PageResponseDTO<MacroMessageResponseDTO> getPagedMessage(String direction, PageRequestDTO pageRequestDTO);
}
