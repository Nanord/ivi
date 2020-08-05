package com.stm.ivi.pojo.ivi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class IviBuyInfoResultWrapper {
    private IviBuyInfoWrapper result;
}
