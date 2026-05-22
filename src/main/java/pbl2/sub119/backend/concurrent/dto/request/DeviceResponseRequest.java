package pbl2.sub119.backend.concurrent.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeviceResponseRequest {

    @NotNull
    private Boolean isMyDevice;
}
