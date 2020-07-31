package com.stm.megogo.pojo;

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
    private List<String> studioList = new ArrayList<>();
    private String country;
    private List<String> genreList = new ArrayList<>();
    private String ratingKinopoisk;
    private String ratingIMDB;
    private String feesInWorld;
    private String feesInRussia;
    private String director;
    private List<String> actorList = new ArrayList<>();
    private List<String> awardList = new ArrayList<>();
    private String releaseDateInWorld;
    private String releaseDateInRussia;
    private String releaseDataInDigital;
    String rent;
    String buy;
    String rentWithSubscription;
    String buyWithSubscription;
    String buySubscription;
    private String url;
}
