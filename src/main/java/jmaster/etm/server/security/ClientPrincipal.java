package jmaster.etm.server.security;

import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;

public record ClientPrincipal(Long deviceId, String deviceUuid, String deviceName, Long userId, String userName) implements Principal, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return userName + (StringUtils.hasText(deviceName) ? "/" + deviceName : "");
    }
}
