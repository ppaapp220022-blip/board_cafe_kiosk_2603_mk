package org.example.board_cafe_kiosk_2603.controller.admin.macro;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.macro.MacroMessageResponseDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageRequestDTO;
import org.example.board_cafe_kiosk_2603.dto.common.pagenation.PageResponseDTO;
import org.example.board_cafe_kiosk_2603.service.admin.macro.MacroMessageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Controller
@RequestMapping("/admin/macro")
@RequiredArgsConstructor
public class MacroMessageController {

    private final MacroMessageService macroMessageService;

    @GetMapping
    public String getAllMacro(Model model) {
        log.info("--- MacroMessageController getAllMacro get ---");
        List<MacroMessageResponseDTO> macroList = macroMessageService.getAllActiveMessages();

        // direction별로 그룹핑 (예: "STAFF_TO_TABLE" -> 리스트, "CUSTOMER_TO_STAFF" -> 리스트)
        Map<String, List<MacroMessageResponseDTO>> macroGroups = macroList.stream()
                .collect(Collectors.groupingBy(MacroMessageResponseDTO::getDirection));

        model.addAttribute("macroGroups", macroGroups);
        return "admin/macro";
    }

    // 탭별 페이징 AJAX
    @GetMapping("/list")
    @ResponseBody
    public PageResponseDTO<MacroMessageResponseDTO> getPagedList(
            @RequestParam String direction,
            PageRequestDTO pageRequestDTO) {
        return macroMessageService.getPagedMessage(direction, pageRequestDTO);
    }
}