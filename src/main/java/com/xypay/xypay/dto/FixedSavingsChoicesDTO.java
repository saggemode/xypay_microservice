package com.xypay.xypay.dto;

import lombok.Data;

import java.util.List;

@Data
public class FixedSavingsChoicesDTO {
    
    private List<ChoiceDTO> purposes;
    private List<ChoiceDTO> sources;
    
    @Data
    public static class ChoiceDTO {
        private String value;
        private String label;
        
        public ChoiceDTO(String value, String label) {
            this.value = value;
            this.label = label;
        }
    }
}