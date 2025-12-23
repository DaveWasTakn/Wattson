package com.dave.Ocpp;

public enum AuthorizationStatus {

    ACCEPTED("Accepted"),
    BLOCKED("Blocked"),
    EXPIRED("Expired"),
    INVALID("Invalid"),
    CONCURRENT_Tx("ConcurrentTx");

    private final String displayName;

    AuthorizationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

}
