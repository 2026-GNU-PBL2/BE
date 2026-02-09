package pbl2.sub119.backend.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import pbl2.sub119.backend.auth.entity.OauthUserEntity;
import pbl2.sub119.backend.auth.enumerated.SocialProvider;

@Mapper
public interface OauthUserMapper {

    OauthUserEntity findByProviderAndSocialId(
            @Param("socialProvider") SocialProvider socialProvider,
            @Param("socialId") String socialId
    );

    boolean existsByProviderAndSocialId(
            @Param("socialProvider") SocialProvider socialProvider,
            @Param("socialId") String socialId
    );

    boolean existsById(@Param("id") Long id);

    int insert(OauthUserEntity user);
}
