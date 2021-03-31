package com.sabadellzurich.olimpo.renewal.enumstatus;

public enum CheckerStatusEnum {
    NO_CHECKER("Sin validar"),
    PRE_CHECKED("Prevalidado"),
    OK_CHECKER("Validación correcta"),
    NOK_CHECKER("Validación incorrecta"),
    ERROR_CHECKER("Error en la validación");

    private final String str;

    CheckerStatusEnum(String str) {
        this.str = str;
    }

    public String str() {
        return this.str;
    }
}

