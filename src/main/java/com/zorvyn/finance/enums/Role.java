package com.zorvyn.finance.enums;

public enum Role {

    ROLE_VIEWER,
    ROLE_ANALYST,
    ROLE_ADMIN;

    public String getAuthority() {
        return this.name();
    }
}