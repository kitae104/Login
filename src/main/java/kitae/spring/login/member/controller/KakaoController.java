package kitae.spring.login.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import kitae.spring.login.member.dto.KakaoDto;
import kitae.spring.login.member.service.KakaoService;
import kitae.spring.login.utils.entity.MsgEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/kakao")
public class KakaoController {

    private final KakaoService kakaoService;

    @GetMapping("/callback")
    public ResponseEntity<MsgEntity> kakaoCallback(HttpServletRequest request) throws Exception {
        KakaoDto kakaoDto = kakaoService.getKakaoTokenInfo(request.getParameter("code"));
        return ResponseEntity.ok().body(new MsgEntity("Success", kakaoDto));
    }
}
