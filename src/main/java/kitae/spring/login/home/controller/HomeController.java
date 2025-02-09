package kitae.spring.login.home.controller;

import kitae.spring.login.member.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final KakaoService kakaoService;

    @GetMapping("/")
    public String login(Model model) {
        model.addAttribute("kakaoUrl", kakaoService.getKakaoLogin());
        return "login";
    }
}
