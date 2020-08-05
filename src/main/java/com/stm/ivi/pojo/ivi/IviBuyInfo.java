package com.stm.ivi.pojo.ivi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IviBuyInfo {
    private String price;
    @JsonProperty("currency_symbol")
    private String currencySymbol;
    private String quality;
    @JsonProperty("object_type")
    private String type;
    @JsonProperty("renewal_price")
    private String priceSubscription;
    @JsonProperty("ownership_type")
    private String durationType;
}
