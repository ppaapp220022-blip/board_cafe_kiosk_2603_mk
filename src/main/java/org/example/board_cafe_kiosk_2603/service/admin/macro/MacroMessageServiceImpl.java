package org.example.board_cafe_kiosk_2603.service.admin.macro;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.admin.macro.MacroMessage;
import org.example.board_cafe_kiosk_2603.dto.admin.macro.MacroMessageResponseDTO;
import org.example.board_cafe_kiosk_2603.repository.admin.macro.MacroMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MacroMessageServiceImpl implements MacroMessageService {
    final private MacroMessageRepository macroMessageRepository;

    @Override
    public List<MacroMessageResponseDTO> getAllActiveMessages() {
        List<MacroMessageResponseDTO> activeMessages = new ArrayList<>();
        List<MacroMessage> result = macroMessageRepository.findAllActive();
        result.forEach(item -> {
            MacroMessageResponseDTO macroMessageResponseDTO = MacroMessageResponseDTO.builder()
                    .messageText(item.getMessageText())
                    .direction(item.getDirection()).build();
            activeMessages.add(macroMessageResponseDTO);
        });
        return activeMessages;
    }
}
