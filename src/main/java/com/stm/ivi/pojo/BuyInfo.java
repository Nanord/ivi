package com.stm.ivi.pojo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class BuyInfo {
    String buySubscription = "-";
    String buyHD = "-";
    String buySD = "-";
    String buyUHD = "-";
    String rentHD = "-";
    String rentSD = "-";
    String rendUHD = "-";
}
