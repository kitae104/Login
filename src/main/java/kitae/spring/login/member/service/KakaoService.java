package kitae.spring.login.member.service;

import com.fasterxml.jackson.databind.util.JSONPObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kitae.spring.login.member.dto.KakaoDto;
import kitae.spring.login.member.entity.Member;
import kitae.spring.login.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Service
@RequiredArgsConstructor
public class KakaoService {

    private final MemberRepository memberRepository;

    @Value("${kakao.client.id}")
    private String KAKAO_CLIENT_ID;

    @Value("${kakao.client.secret}")
    private String KAKAO_CLIENT_SECRET;

    @Value("${kakao.redirect.url}")
    private String KAKAO_REDIRECT_URL;

    private final static String KAKAO_AUTH_URI = "https://kauth.kakao.com"; // 카카오 계정 인증 URI

    private final static String KAKAO_API_URI = "https://kapi.kakao.com"; // 카카오 API URI

    public String getKakaoLogin() {
        return KAKAO_AUTH_URI + "/oauth/authorize"
                + "?client_id=" + KAKAO_CLIENT_ID
                + "&redirect_uri=" + KAKAO_REDIRECT_URL
                + "&response_type=code";
    }

    public KakaoDto getKakaoTokenInfo(String code) throws Exception {
        if(code == null){
            throw new Exception("인증된 코드 값이 없습니다.");  // 인증 코드가 없을 경우 예외 처리
        }

        String accessToken = "";

        try{
            // httpHeaders 객체 생성(웹 폼 데이터 형식)
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            // Kakao API로 요청할 파라미터
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", KAKAO_CLIENT_ID);
            params.add("client_secret", KAKAO_CLIENT_SECRET);
            params.add("code", code);
            params.add("redirect_uri", KAKAO_REDIRECT_URL);

            // HTTP 요청에 필요한 헤더와 바디 생성
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.exchange(
            KAKAO_AUTH_URI + "/oauth/token",
                 HttpMethod.POST,
                 httpEntity,
                 String.class
            );

            // JSON 형식의 문자열을 Java 객체로 변환
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());

            accessToken = (String) jsonObject.get("access_token");

        } catch(Exception e){
            throw new Exception("카카오 API 요청 중 오류가 발생했습니다.");
        }
        return getUserDtoWithToken(accessToken);
    }

    public KakaoDto getUserDtoWithToken(String accessToken) throws Exception {
        // httpHeaders 객체 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpHeader 담기
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                KAKAO_API_URI + "/v2/user/me",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());
        JSONObject kakaoAccount = (JSONObject) jsonObject.get("kakao_account");
        JSONObject profile = (JSONObject) kakaoAccount.get("profile");

        long id = (long) jsonObject.get("id");  // 사용자 고유 ID
        String email = (String) kakaoAccount.get("email");  // 사용자 이메일
        String nickname = (String) profile.get("nickname");  // 사용자 닉네임

        Member member = Member.builder()
                .email(email)
                .username(nickname)
                .role("ROLE_USER")
                .build();

        // 회원 정보 저장 - 기존에 존재하는 확인 필요!!
        memberRepository.save(member);

        // 현재 스레드에 바인딩된 요청 속성을 반환
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        HttpSession session = request.getSession();
        session.setAttribute("member", member);

        return KakaoDto.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .build();
    }
}
