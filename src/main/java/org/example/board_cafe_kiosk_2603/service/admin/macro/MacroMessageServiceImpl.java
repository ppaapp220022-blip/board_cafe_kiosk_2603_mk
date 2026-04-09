package org.example.board_cafe_kiosk_2603.service.admin.macro;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.macro.MacroMessage;
import org.example.board_cafe_kiosk_2603.dto.admin.macro.MacroMessageResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageResponseDTO;
import org.example.board_cafe_kiosk_2603.mapper.admin.macro.MacroMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MacroMessageServiceImpl implements MacroMessageService {
    final private MacroMessageMapper macroMessageMapper;

    @Override
    public List<MacroMessageResponseDTO> getAllActiveMessages() {
        log.info("--- MacroMessageServiceImpl getAllActiveMessages ---");

        List<MacroMessageResponseDTO> activeMessages = new ArrayList<>();
        List<MacroMessage> result = macroMessageMapper.findAllActive();
        result.forEach(item -> {
            MacroMessageResponseDTO macroMessageResponseDTO = MacroMessageResponseDTO.builder()
                    .messageText(item.getMessageText())
                    .direction(item.getDirection()).build();
            activeMessages.add(macroMessageResponseDTO);
        });
        return activeMessages;
    }

    // direction별 페이징 조회
    @Override
    public PageResponseDTO<MacroMessageResponseDTO> getPagedMessage(String direction, PageRequestDTO pageRequestDTO) {
        List<MacroMessageResponseDTO> dtoList = macroMessageMapper
                .selectList(direction, pageRequestDTO.getSkip(), pageRequestDTO.getSize())
                .stream()
                .map(item -> MacroMessageResponseDTO.builder()
                        .messageText(item.getMessageText())
                        .direction(item.getDirection())
                        .build())
                .collect(Collectors.toList());

        int total = macroMessageMapper.selectCount(direction);

        return PageResponseDTO.<MacroMessageResponseDTO>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .total(total)
                .build();
    }
}
