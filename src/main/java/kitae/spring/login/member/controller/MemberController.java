package kitae.spring.login.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kitae.spring.login.member.entity.Member;
import kitae.spring.login.member.repository.MemberRepository;
import kitae.spring.login.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/myPage")
    public String myPage(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {
        request.setCharacterEncoding("UTF-8");  // 한글 깨짐 방지
        response.setContentType("text/html; charset=UTF-8");  // 한글 깨짐 방지
        PrintWriter out = response.getWriter();

        // 세션에서 사용자 정보 검색
        HttpSession session = request.getSession();
        Member member = (Member) session.getAttribute("member");

        String userName = "";
        String userEmail = "";

        if(member != null) {
            userName = member.getUsername();
            userEmail = member.getEmail();

            session.setAttribute("isLogon", true);
            session.setAttribute("user.name", userName);
            session.setAttribute("user.email", userEmail);
        }

        model.addAttribute("userName", userName);
        model.addAttribute("userEmail", userEmail);
        return "mypage";

    }
}
