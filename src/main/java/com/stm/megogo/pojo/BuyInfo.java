package com.stm.megogo.pojo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class BuyInfo {
    String rent;
    String buy;
    String rentWithSubscription;
    String buyWithSubscription;
}
