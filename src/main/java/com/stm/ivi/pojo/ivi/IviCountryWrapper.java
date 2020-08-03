package com.stm.ivi.pojo.ivi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class IviCountryWrapper {
    private Map<String, IviCountry> result;
}
