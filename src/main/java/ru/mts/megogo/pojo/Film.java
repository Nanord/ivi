package ru.mts.megogo.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static ru.mts.megogo.utils.Constants.CELL_SEPARATOR;
import static ru.mts.megogo.utils.Constants.LINE_SEPARATOR;

@Data
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Film {
    private String id;
    private String nameRus;
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
    private String provisionFor;
    private String purchasePrice;
    private Boolean subscriptionAvailability = false;
    private String url;
    private String kinopoiskUrl;
}
