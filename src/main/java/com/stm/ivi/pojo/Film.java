package com.stm.ivi.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Film {
    private String id;
    private String nameRus;
    private String category = "Фильмы";
    private String nameOrigin;
    private String year;
    private String country;
    private List<String> genreList = new ArrayList<>();
    private String ratingKinopoisk;
    private String ratingIMDB;
    private String ratingIvi;
    private String feesInWorld;
    private String feesInRussia;
    private String director;
    private List<String> actorList = new ArrayList<>();
    private List<String> awardList = new ArrayList<>();
    private String releaseDateInWorld;
    private String releaseDateInRussia;
    private String releaseDataInDigital;
    private BuyInfo buyInfo;
    private String url;
}
