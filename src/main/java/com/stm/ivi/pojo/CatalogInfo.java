package com.stm.ivi.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CatalogInfo {
    String filmUrl;
    Boolean isAvailable = false;
    Boolean isSubscription = false;
    BuyInfo buyInfo;
}
