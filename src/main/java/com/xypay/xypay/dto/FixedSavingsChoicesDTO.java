package com.xypay.xypay.dto;

import java.util.List;

public class FixedSavingsChoicesDTO {
    private List<FixedSavingsPurposeDTO> purposes;
    private List<FixedSavingsSourceDTO> sources;

    // Getters and setters
    public List<FixedSavingsPurposeDTO> getPurposes() {
        return purposes;
    }

    public void setPurposes(List<FixedSavingsPurposeDTO> purposes) {
        this.purposes = purposes;
    }

    public List<FixedSavingsSourceDTO> getSources() {
        return sources;
    }

    public void setSources(List<FixedSavingsSourceDTO> sources) {
        this.sources = sources;
    }
}