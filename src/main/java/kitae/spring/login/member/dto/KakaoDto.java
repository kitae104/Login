package kitae.spring.login.member.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KakaoDto {
    private Long id;
    private String email;
    private String nickname;
}
