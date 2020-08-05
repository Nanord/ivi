package com.stm.ivi.pojo.ivi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IviBuyRange {
    private Price price;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Price {
        String min;
    }
}
