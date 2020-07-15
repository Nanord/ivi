package ru.mts.megogo.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
    private List<String> studioList;
    private String country;
    private List<String> genreList = new ArrayList<>();
    private String ratingKinopoisk;
    private String ratingIMDB;
    private String feesInWorld;
    private String feesInRussia;
    private String director;
    private List<String> actors = new ArrayList<>();
    private List<String> awards = new ArrayList<>();
    private String releaseDateInWorld;
    private String releaseDateInRussia;
    private String releaseDataInDigital;
    private String provisionFor;
    private String purchasePrice;
    private Boolean subscriptionAvailability = false;
    private String url;
    private String kinopoiskUrl;

    public String toCSV() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String studio : this.getStudioList()) {
            for (String actor : this.getActors()) {
                for (String genre : this.getGenreList()) {
                    stringBuilder.append(id);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(nameRus);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(nameOrigin);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(year);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(studio);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(country);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(genre);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(ratingKinopoisk);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(ratingIMDB);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(feesInWorld);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(feesInRussia);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(director);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(actor);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(String.join(" | ", this.getAwards()));
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(releaseDateInRussia);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(releaseDataInDigital);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(provisionFor != null ? provisionFor : "Бесплатно");
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(purchasePrice != null ? purchasePrice : "Бесплатно");
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(subscriptionAvailability ? "+" : "-");
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(url);
                    stringBuilder.append(CELL_SEPARATOR);
                    stringBuilder.append(LINE_SEPARATOR);
                }
            }
        }
        return stringBuilder.toString();
    }

}
