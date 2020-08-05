package com.stm.ivi.pojo.ivi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IviBuyInfoWrapper {
    @JsonProperty("price_ranges")
    private IviBuyRange priceRanges;
    @JsonProperty("purchase_options")
    private List<IviBuyInfo> iviBuyInfoList = new ArrayList<>();
}
