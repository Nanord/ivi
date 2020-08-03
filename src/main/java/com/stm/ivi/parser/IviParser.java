package com.stm.ivi.parser;

import com.stm.ivi.pojo.BuyInfo;

public interface IviParser {
    BuyInfo parse(String id);
}
