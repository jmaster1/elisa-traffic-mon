package jmaster.etm.server.security;

import jmaster.system.user.UserRole;

public final class EtmUserRole {

    public static final UserRole admin = UserRole.of("admin");

    private EtmUserRole() {
    }

    public static UserRole[] values() {
        return new UserRole[] {
                admin
        };
    }
}
