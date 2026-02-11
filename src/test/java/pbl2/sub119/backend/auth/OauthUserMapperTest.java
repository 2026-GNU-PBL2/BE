package pbl2.sub119.backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pbl2.sub119.backend.auth.entity.OauthInfo;
import pbl2.sub119.backend.auth.entity.OauthUserEntity;
import pbl2.sub119.backend.auth.enumerated.SocialProvider;
import pbl2.sub119.backend.auth.mapper.OauthUserMapper;
import pbl2.sub119.backend.common.enumerated.UserRole;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class OauthUserMapperTest {

    @Autowired
    private OauthUserMapper oauthUserMapper;

    @Test
    void insert_and_find() {
        OauthInfo info = OauthInfo.of(
                "test@sample.com",
                "테스트",
                "kakao-12345",
                SocialProvider.KAKAO
        );

        OauthUserEntity user = OauthUserEntity.createFromOAuth(info, UserRole.CUSTOMER);

        oauthUserMapper.insert(user);

        assertNotNull(user.getId());

        OauthUserEntity found = oauthUserMapper.findByProviderAndSocialId(SocialProvider.KAKAO, "kakao-12345");

        assertNotNull(found);
    }
}
