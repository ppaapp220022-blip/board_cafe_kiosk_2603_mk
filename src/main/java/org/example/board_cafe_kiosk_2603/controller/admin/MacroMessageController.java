//package org.example.board_cafe_kiosk_2603.controller.admin;
//
//import lombok.RequiredArgsConstructor;
//import org.example.board_cafe_kiosk_2603.dto.admin.MacroMessageResponseDTO;
//import org.example.board_cafe_kiosk_2603.service.admin.macro.MacroMessageService;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin") // 1. 슬래시 추가
//@RequiredArgsConstructor
//public class MacroMessageController {
//
//    private final MacroMessageService macroMessageService;
//
//    @GetMapping("/macro")
//    public String getAllMacro(Model model) {
//        log.info("--- AdminController getAllMacro get ---");
//        List<MacroMessageResponseDTO> macroList = macroMessageService.getAllActiveMessages();
//
//        // direction별로 그룹핑 (예: "STAFF_TO_TABLE" -> 리스트, "CUSTOMER_TO_STAFF" -> 리스트)
//        Map<String, List<MacroMessageResponseDTO>> macroGroups = macroList.stream()
//                .collect(Collectors.groupingBy(MacroMessageResponseDTO::getDirection));
//
//        model.addAttribute("macroGroups", macroGroups);
//        return "admin/macro";
//    }
//}