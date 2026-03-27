package org.example.board_cafe_kiosk_2603.service.kiosk;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.board_cafe_kiosk_2603.dto.admin.PointAdminDTO;
import org.example.board_cafe_kiosk_2603.dto.kiosk.CartDTO;
import org.example.board_cafe_kiosk_2603.service.admin.PointService;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

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

    private final CartService    cartService;
    private final PointService   pointService;
    private final MenuService    menuService;

    // ===================================================
    // 세션 헬퍼
    // ===================================================

    public void initSessionIfNeeded(HttpSession session, Integer tableNumber) {
        if (session.getAttribute("tableNumber") == null) {
            session.setAttribute("tableNumber",      tableNumber);
            session.setAttribute("partySize",        2);
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
        Long startTime = (Long) session.getAttribute("sessionStartTime");
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
        session.setAttribute("partySize",   size);
        session.setAttribute("tableNumber", tableNumber);
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize",   size);
    }

    public void buildPackageSelectionModel(Model model, Integer tableNumber, Integer size,
                                           HttpSession session,
                                           List<?> packageList) {
        if (tableNumber != 1) session.setAttribute("tableNumber", tableNumber);
        if (size        != 1) session.setAttribute("partySize",   size);

        Integer sessionTable = (Integer) session.getAttribute("tableNumber");
        Integer sessionSize  = (Integer) session.getAttribute("partySize");

        model.addAttribute("tableNumber", sessionTable != null ? sessionTable : tableNumber);
        model.addAttribute("partySize",   sessionSize  != null ? sessionSize  : size);
        model.addAttribute("packageList", packageList);
    }

    public void buildMenuModel(Model model, int tableNumber, HttpSession session,
                               String menuType, List<Map<String, Object>> menuItems, String title) {
        CartDTO cartDTO = cartService.getCart(tableNumber);
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize",   getPartySize(session));
        model.addAttribute("currentMenu", menuType);
        model.addAttribute("menuItems",   menuItems);
        model.addAttribute("cartCount",   cartDTO.getCartCount());
        model.addAttribute("pageTitle",   title);
    }

    public void buildCartModel(Model model, int tableNumber, HttpSession session) {
        CartDTO cartDTO = cartService.getCart(tableNumber);
        model.addAttribute("tableNumber", tableNumber);
        model.addAttribute("partySize",   getPartySize(session));
        model.addAttribute("cartItems",   cartDTO.getCartItems());
        model.addAttribute("totalPrice",  cartDTO.getTotalPrice());
        model.addAttribute("cartCount",   cartDTO.getCartCount());
    }

    public void buildCheckoutModel(Model model, int tableNumber, HttpSession session) {
        CartDTO cartDTO       = cartService.getCart(tableNumber);
        int     sessionDuration = getSessionDuration(session);
        String  customerPhone   = (String) session.getAttribute("customerPhone");
        int     pointBalance    = resolvePointBalance(customerPhone);

        model.addAttribute("tableNumber",    tableNumber);
        model.addAttribute("partySize",      getPartySize(session));
        model.addAttribute("cartItems",      cartDTO.getCartItems());
        model.addAttribute("totalPrice",     cartDTO.getTotalPrice());
        model.addAttribute("cartCount",      cartDTO.getCartCount());
        model.addAttribute("sessionHours",   sessionDuration / 60);
        model.addAttribute("sessionMinutes", sessionDuration % 60);
        model.addAttribute("pointBalance",   pointBalance);
        model.addAttribute("customerPhone",  customerPhone != null ? customerPhone : "");
    }

    public void buildScreensaverModel(Model model, HttpSession session) {
        Integer tableNumber = (Integer) session.getAttribute("tableNumber");
        model.addAttribute("tableNumber", tableNumber != null ? tableNumber : 1);
    }

    // ===================================================
    // 메뉴 데이터 위임
    // ===================================================

    public List<Map<String, Object>> getDrinkItems()  { return menuService.getDrinkItems(); }
    public List<Map<String, Object>> getFoodItems()   { return menuService.getFoodItems();  }
    public List<Map<String, Object>> getGameItems()   { return menuService.getGameItems();  }
}
