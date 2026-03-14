package pbl2.sub119.backend.bankaccounts.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "kftc")
public class KftcProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String baseUrl = "https://testapi.openbanking.or.kr";

    private String useOrgCode;
}
