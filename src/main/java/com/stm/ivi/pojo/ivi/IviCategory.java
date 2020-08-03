package com.stm.ivi.pojo.ivi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class IviCategory {
    private Long id;
    private String title;
    private List<IviCategory> genres;
}
