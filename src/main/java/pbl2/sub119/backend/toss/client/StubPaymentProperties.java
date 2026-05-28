package pbl2.sub119.backend.toss.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "payment.stub")
public class StubPaymentProperties {
    private int delayMs = 120;
    private double failRate = 0.0;
}