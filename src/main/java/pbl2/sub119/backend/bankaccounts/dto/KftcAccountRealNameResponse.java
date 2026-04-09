package pbl2.sub119.backend.bankaccounts.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KftcAccountRealNameResponse {

    @JsonProperty("rsp_code")
    private String rspCode;

    @JsonProperty("rsp_message")
    private String rspMessage;

    @JsonProperty("account_holder_name")
    private String accountHolderName;

    public boolean isSuccess() {
        return "A0000".equals(rspCode) || "0000".equals(rspCode);
    }
}
