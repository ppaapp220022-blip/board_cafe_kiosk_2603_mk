package org.example.board_cafe_kiosk_2603.service.kiosk;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.domain.common.cafeTableSession.CafeTableSession;
import org.example.board_cafe_kiosk_2603.dto.admin.point.PointAdminDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.cafePackage.CafePackageDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.cart.CartDTO;
import org.example.board_cafe_kiosk_2603.service.admin.cafeTable.TableSessionAdminService;
import org.example.board_cafe_kiosk_2603.service.admin.point.PointService;
import org.example.board_cafe_kiosk_2603.service.kiosk.cafePackage.CafePackageService;
import org.example.board_cafe_kiosk_2603.service.kiosk.cart.CartService;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * 키오스크 페이지 렌더링에 필요한 Model 구성 로직을 담당.
 * Controller 는 서비스를 호출하고 뷰 이름만 반환합니다.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class KioskPageService {

    private final CartService cartService;
    private final PointService pointService;
    private final CafePackageService cafePackageService;
    private final TableSessionAdminService tableSessionAdminService;

    // ===================================================
    // 세션 헬퍼
    // ===================================================

    public void initSessionIfNeeded(HttpSession session, Integer tableNumber) {
        if (session.getAttribute("tableNumber") == null) {
            session.setAttribute("tableNumber", tableNumber);
            session.setAttribute("partySize", 2);
            session.setAttribute("sessionStartTime", System.currentTimeMillis());
            log.info("세션 초기화 - 테이블: {}", tableNumber);
        }
    }

    public Integer resolveTableNumber(Integer tableNumber, HttpSession session) {
        if (tableNumber == 1 && session.getAttribute("tableNumber") != null) {
            return (Integer) session.getAttribute("tableNumber");
        }
        return tableNumber;
    }

    public int getPartySize(HttpSession session) {
        Integer partySize = (Integer) session.getAttribute("partySize");
        return partySize != null ? partySize : 2;
    }

    public int getSessionDuration(HttpSession session) {
        Long startTime = readSessionStartMillis(session.getAttribute("sessionStartTime"));
        if (startTime == null) return 0;
        return (int) ((System.currentTimeMillis() - startTime) / 60000);
    }

    public int resolvePointBalance(String customerPhone) {
        if (customerPhone == null || customerPhone.isBlank()) return 0;
        PointAdminDTO point = pointService.getPointByPhone(customerPhone);
        return point != null ? point.getBalance() : 0;
    }

    // ===================================================
    // 페이지별 Model 구성
    // ===================================================

    public void buildHeadcountModel(Model model, Integer tableNumber) {
        model.addAttribute("tableNumber", tableNumber);
    }

    public void buildPhoneLoginModel(Model model, Integer tableNumber, Integer size,
                                     HttpSession session) {
        session.setAttribute("partySize", size);
        session.setAttribute("tableNumber", tableNumber);
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", size);
    }

    public void buildPackageSelectionModel(Model model, Integer tableNumber, Integer size,
                                           HttpSession session,
                                           List<?> packageList) {
        if (tableNumber != 1) session.setAttribute("tableNumber", tableNumber);
        if (size != 1) session.setAttribute("partySize", size);

        Integer sessionTable = (Integer) session.getAttribute("tableNumber");
        Integer sessionSize = (Integer) session.getAttribute("partySize");

        model.addAttribute("tableNumber", sessionTable != null ? sessionTable : tableNumber);
        model.addAttribute("partySize", sessionSize != null ? sessionSize : size);
        model.addAttribute("packageList", packageList);
    }

    public void buildMenuModel(Model model, int tableNumber, HttpSession session,
                               String menuType, List<Map<String, Object>> menuItems, String title) {
        CartDTO cartDTO = cartService.getCart(tableNumber);
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        model.addAttribute("currentMenu", menuType);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("cartCount", cartDTO.getCartCount());
        model.addAttribute("pageTitle", title);
    }

    public void buildCartModel(Model model, int tableNumber, HttpSession session) {
        CartDTO cartDTO = cartService.getCart(tableNumber);
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize", getPartySize(session));
        model.addAttribute("cartItems", cartDTO.getCartItems());
        model.addAttribute("totalPrice", cartDTO.getTotalPrice());
        model.addAttribute("cartCount", cartDTO.getCartCount());
    }

    public void buildCheckoutModel(Model model, int tableNumber, HttpSession session) {
        Integer tableId = (Integer) session.getAttribute("tableId");
        boolean adminCheckoutMode = Boolean.TRUE.equals(session.getAttribute("adminCheckoutMode"));
        CartDTO cartDTO = cartService.getCart(tableNumber);
        String customerPhone = (String) session.getAttribute("customerPhone");
        int pointBalance = resolvePointBalance(customerPhone);
        int partySize = getPartySize(session);

        // DB에서 활성 세션 먼저 조회
        Integer packageId = null;
        Long sessionStartMillis = null;
        if (tableId != null) {
            CafeTableSession activeSession = tableSessionAdminService.getActiveSession(tableId);
            if (activeSession != null) {
                packageId = activeSession.getPackageId();
                sessionStartMillis = toEpochMillis(activeSession.getCheckInTime());
            }
        }

        // 활성 세션이 없으면 기존 세션값을 사용하되, 값이 없으면 현재 시각으로 초기화해 과도한 초과시간 표시를 방지한다.
        if (sessionStartMillis == null && !adminCheckoutMode) {
            sessionStartMillis = readSessionStartMillis(session.getAttribute("sessionStartTime"));
        }
        if (sessionStartMillis == null) {
            sessionStartMillis = System.currentTimeMillis();
            session.setAttribute("sessionStartTime", sessionStartMillis);
        }
        model.addAttribute("sessionStartTime", sessionStartMillis);
        int sessionDuration = Math.max(0, (int) ((System.currentTimeMillis() - sessionStartMillis) / 60000));

        // 패키지 금액 계산 (packageId가 확정된 후)
        int packageTotal   = 0;
        String packageName = "";
        model.addAttribute("durationMinutes",  null);  // ← 기본값 먼저 설정
        model.addAttribute("extraPricePerMin", 0.0);   // ← 기본값 먼저 설정

        if (packageId != null) {
            CafePackageDTO pkg = cafePackageService.getById(packageId);
            if (pkg != null) {
                packageTotal = pkg.getBasePrice() * partySize;
                packageName  = pkg.getName();
                model.addAttribute("durationMinutes",  pkg.getDurationMinutes());
                model.addAttribute("extraPricePerMin", pkg.getExtraPricePerMin() != null ? pkg.getExtraPricePerMin() : 0.0);
            }
        }

        int totalPrice = cartDTO.getTotalPrice() + packageTotal;

        model.addAttribute("tableNumber",   tableNumber);
        model.addAttribute("partySize",     partySize);
        model.addAttribute("cartItems",     cartDTO.getCartItems());
        model.addAttribute("menuTotal",     cartDTO.getTotalPrice());
        model.addAttribute("packageName",   packageName);
        model.addAttribute("packageTotal",  packageTotal);
        model.addAttribute("totalPrice",    totalPrice);
        model.addAttribute("cartCount",     cartDTO.getCartCount());
        model.addAttribute("sessionHours",  sessionDuration / 60);
        model.addAttribute("sessionMinutes",sessionDuration % 60);
        model.addAttribute("pointBalance",  pointBalance);
        model.addAttribute("customerPhone", customerPhone != null ? customerPhone : "");

        log.info("정산 화면 - 테이블: {}, 메뉴: ₩{}, 패키지: {} ₩{}, 합계: ₩{}, 포인트: {}P",
                tableNumber, cartDTO.getTotalPrice(), packageName, packageTotal, totalPrice, pointBalance);
    }

    private Long readSessionStartMillis(Object rawStartTime) {
        if (rawStartTime instanceof Long) {
            return (Long) rawStartTime;
        }
        if (rawStartTime instanceof Integer) {
            return ((Integer) rawStartTime).longValue();
        }
        if (rawStartTime instanceof LocalDateTime) {
            return toEpochMillis((LocalDateTime) rawStartTime);
        }
        if (rawStartTime instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Long toEpochMillis(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public void buildScreensaverModel(Model model, HttpSession session) {
        Integer tableNumber = (Integer) session.getAttribute("tableNumber");
        model.addAttribute("tableNumber", tableNumber != null ? tableNumber : 1);
    }
}
