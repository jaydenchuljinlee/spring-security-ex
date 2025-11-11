package com.example.security.view.web.main;

import com.example.security.comn.enums.request.RequestCookie;
import com.example.security.comn.enums.request.RequestHeaderType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Controller
public class MainController {

    @GetMapping("/main")
    public String main(Model model, HttpServletRequest request) {
        String accessToken = request.getHeader(RequestHeaderType.X_AUTH_ACCESS_TOKEN.value());
        Cookie[] cookies = request.getCookies();

        String value = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (RequestCookie.AUTH_ID.getValue().equals(cookie.getName())) {
                    value = cookie.getValue();
                }
            }
        }

        // 로그
        log.info("AccessToken: {}", accessToken);
        log.info("Cookie Value: {}", value);

        // 템플릿에서 접근할 데이터 추가
        model.addAttribute("authId", value);
        model.addAttribute("accessToken", accessToken);

        return "main"; // templates/main.html
    }
}
