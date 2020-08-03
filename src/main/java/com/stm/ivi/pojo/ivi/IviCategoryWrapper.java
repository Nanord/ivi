package com.stm.ivi.pojo.ivi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class IviCategoryWrapper {
    private List<IviCategory> result;
}
