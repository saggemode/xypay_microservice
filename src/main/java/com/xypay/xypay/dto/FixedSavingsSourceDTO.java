package com.xypay.xypay.dto;

public class FixedSavingsSourceDTO {
    private String value;
    private String label;

    public FixedSavingsSourceDTO() {}

    public FixedSavingsSourceDTO(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}