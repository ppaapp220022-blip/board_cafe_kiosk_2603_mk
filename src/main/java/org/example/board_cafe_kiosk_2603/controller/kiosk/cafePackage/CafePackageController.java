package org.example.board_cafe_kiosk_2603.controller.kiosk.cafePackage;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.service.kiosk.cafePackage.CafePackageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 패키지 선택 페이지 + REST API 컨트롤러.
 *
 * [페이지] GET  /kiosk/package_selection  → package_selection.html
 * [API]   POST /kiosk/package/select      → 패키지 선택 처리 (JSON)
 */
@Log4j2
@Controller
@RequestMapping("/kiosk")
@RequiredArgsConstructor
public class CafePackageController {

    private final CafePackageService cafePackageService;

    // ===========================================================
    // 페이지
    // ===========================================================

    @GetMapping("/package_selection")
    public String packageSelectionPage(
            @RequestParam(required = false, defaultValue = "1") Integer tableNumber,
            @RequestParam(required = false, defaultValue = "1") Integer size,
            HttpSession session, Model model) {

        session.setAttribute("tableNumber", tableNumber);
        session.setAttribute("partySize",   size);

        model.addAttribute("tableNumber",  tableNumber);
        model.addAttribute("partySize",    size);
        model.addAttribute("packageList",  cafePackageService.getActivePackages());

        log.info("패키지 선택 화면 - 테이블: {}, 인원: {}", tableNumber, size);
        return "kiosk/package_selection";
    }

    // ===========================================================
    // REST API
    // ===========================================================

    @PostMapping("/package/select")
    @ResponseBody
    public Map<String, Object> selectPackage(
            @RequestBody Map<String, Object> req,
            HttpSession session) {

        int packageId = ((Number) req.get("packageId")).intValue();

        var pkg = cafePackageService.getById(packageId);

        Map<String, Object> res = new LinkedHashMap<>();
        if (pkg == null) {
            res.put("success", false);
            res.put("message", "패키지를 찾을 수 없습니다.");
            return res;
        }

        session.setAttribute("selectedPackageId",    pkg.getId());
        session.setAttribute("selectedPackageName",  pkg.getName());
        session.setAttribute("selectedPackagePrice", pkg.getBasePrice());
        session.setAttribute("sessionStartTime",     System.currentTimeMillis());

        log.info("패키지 선택 완료 - 테이블: {}, 패키지: {} ({}원)",
                session.getAttribute("tableNumber"), pkg.getName(), pkg.getBasePrice());

        res.put("success",     true);
        res.put("packageId",   pkg.getId());
        res.put("packageName", pkg.getName());
        res.put("basePrice",   pkg.getBasePrice());
        return res;
    }
}
