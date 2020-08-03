package com.stm.ivi.pojo.ivi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class IviFilm {
    private Long id;
    private String description;
    private Long budget;
    private Integer country;
    @JsonProperty("gross_russia")
    private Long grossRussia;
    @JsonProperty("gross_usa")
    private Long grossUsa;
    @JsonProperty("gross_world")
    private Long grossWorld;
    @JsonProperty("imdb_rating")
    private String imdbRating;
    @JsonProperty("ivi_pseudo_release_date")
    private String iviPseudoReleaseDate;
    @JsonProperty("ivi_rating")
    private String iviRating;
    @JsonProperty("kp_rating")
    private String kinopoiskRating;
    @JsonProperty("orig_title")
    private String originalTitle;
    @JsonProperty("release_date")
    private String releaseDate;
    private String synopsis;
    private String title;
    private Integer year;
    @JsonProperty("share_link")
    private String shareLink;
    private List<String> artists = new ArrayList<>();
    private List<Long> genres = new ArrayList<>();
}
