package com.stm.megogo.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CatalogInfo {
    String filmUrl;
    Boolean isAvailable;
    Boolean isSubscription;
    BuyInfo buyInfo;
}
