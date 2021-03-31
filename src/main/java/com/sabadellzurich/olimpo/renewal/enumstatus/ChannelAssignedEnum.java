package com.sabadellzurich.olimpo.renewal.enumstatus;

public enum ChannelAssignedEnum {
    NO_ASSIGNED("Sin asignar"),
    SMS_ASSIGNED("SMS"),
    MAIL_ASSIGNED("Email"),
    POSTAL_ASSIGNED("Postal");

    private final String str;

    ChannelAssignedEnum(String str) {
        this.str = str;
    }

    public String str() {
        return str;
    }
}
