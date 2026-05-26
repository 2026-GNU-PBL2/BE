package pbl2.sub119.backend.concurrent.service;

import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class IpLocationService {

    private final WebClient webClient;

    public IpLocationService(
            @Value("${geoip.ip-api.base-url:http://ip-api.com}") final String baseUrl) {
        this.webClient = WebClient.create(baseUrl);
    }

    /**
     * IP 주소를 지리적 위치 문자열(예: "서울특별시, 대한민국")로 변환.
     * 사설/루프백 IP이거나 조회 실패 시 raw IP를 반환.
     */
    public String resolve(final String ip) {
        if (ip == null || ip.isBlank() || isPrivateOrLoopback(ip)) {
            return ip;
        }
        try {
            final Map<String, String> result = webClient.get()
                    .uri("/json/{ip}?fields=status,city,regionName,country&lang=ko", ip)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                    .timeout(Duration.ofSeconds(2))
                    .block();

            if (result == null || !"success".equals(result.get("status"))) {
                return ip;
            }

            final String city = result.getOrDefault("city", "");
            final String country = result.getOrDefault("country", "");

            if (city.isBlank() && country.isBlank()) {
                return ip;
            }
            if (city.isBlank()) {
                return country;
            }
            if (country.isBlank()) {
                return city;
            }
            return city + ", " + country;
        } catch (Exception e) {
            log.debug("IP 위치 조회 실패. ip={}, error={}", ip, e.getMessage());
            return ip;
        }
    }

    private boolean isPrivateOrLoopback(final String ip) {
        // IPv6 loopback
        if ("::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return true;
        }
        // IPv4 loopback
        if (ip.startsWith("127.")) {
            return true;
        }
        // 사설 대역: 10.x, 192.168.x, 172.16~31.x
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (ip.startsWith("172.")) {
            try {
                final int second = Integer.parseInt(ip.split("\\.")[1]);
                return second >= 16 && second <= 31;
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }
}
