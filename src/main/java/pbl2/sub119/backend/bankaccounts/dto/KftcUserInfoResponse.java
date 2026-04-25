package pbl2.sub119.backend.bankaccounts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KftcUserInfoResponse {
    @JsonProperty("user_name")
    private String userName;
    @JsonProperty("res_cnt")
    private String resCnt;
    @JsonProperty("res_list")
    private List<KftcAccountDto> resList; // 등록된 계좌 리스트

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KftcAccountDto {
        @JsonProperty("fintech_use_num")
        private String fintechUseNum; // 핵심 식별자
        @JsonProperty("bank_tran_id")
        private String bankTranId;
        @JsonProperty("bank_name")
        private String bankName;
        @JsonProperty("account_alias")
        private String accountAlias;
        @JsonProperty("account_num_masked")
        private String accountNumMasked;
    }
}
