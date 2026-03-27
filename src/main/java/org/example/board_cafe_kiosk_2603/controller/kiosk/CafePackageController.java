package org.example.board_cafe_kiosk_2603.controller.kiosk;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.kiosk.CafePackageDTO;
import org.example.board_cafe_kiosk_2603.service.kiosk.CafePackageService;
import org.springframework.web.bind.annotation.*;

/**
 * 키오스크 카페 패키지 REST API 컨트롤러.
 * URL: /kiosk/package/*
 */
@Log4j2
@RestController
@RequestMapping("/kiosk/package")
@RequiredArgsConstructor
public class CafePackageController {

    private final CafePackageService cafePackageService;

    @PostMapping("/select")
    public CafePackageDTO selectPackage(
            @RequestBody @Valid CafePackageDTO request,
            HttpSession session) {

        CafePackageDTO pkg = cafePackageService.getById(request.getPackageId());
        if (pkg == null) {
            return CafePackageDTO.fail("패키지를 찾을 수 없습니다.");
        }

        session.setAttribute("selectedPackageId",    pkg.getId());
        session.setAttribute("selectedPackageName",  pkg.getName());
        session.setAttribute("selectedPackagePrice", pkg.getBasePrice());
        session.setAttribute("sessionStartTime",     System.currentTimeMillis());

        log.info("패키지 선택 - 테이블: {}, 패키지: {} ({}원)",
                session.getAttribute("tableNumber"), pkg.getName(), pkg.getBasePrice());

        return CafePackageDTO.selected(pkg, (Integer) session.getAttribute("tableNumber"));
    }
}
