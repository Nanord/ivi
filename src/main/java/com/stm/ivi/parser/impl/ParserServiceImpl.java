package com.stm.ivi.parser.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stm.ivi.parser.ParserService;
import com.stm.ivi.pojo.Film;
import com.stm.ivi.pojo.ivi.*;
import com.stm.ivi.service.JsoupApiHelper;
import com.stm.ivi.service.PageService;
import com.stm.ivi.service.SaveFile;
import com.stm.ivi.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ParserServiceImpl implements ParserService {
    @Autowired
    private PageService pageService;
    @Autowired
    private SaveFile saveFile;
    @Autowired
    private JsoupApiHelper helper;
    private String session;

    @Override
    public void parse() {
        session = helper.getIviSession();
        if (session.isEmpty()) {
            log.warn("Cannot get session");
            return;
        }
        int currentCount = 0;
        while (true) {
            IviResultWrapper data = helper.getForObject(String.format(Constants.API_MOVIES_URL, currentCount, session),
                    new TypeReference<IviResultWrapper>(){}, "Cannot get data").orElse(null);
            if (Objects.nonNull(data)) {
                List<Film> collect = data.getResult().stream()
                        .map(this::mapToFilm)
                        .collect(Collectors.toList());
                currentCount += 100;
                if (currentCount > data.getCount()) break;
            }
        }
    }

    private Film mapToFilm(IviFilm iviFilm) {
        return new Film()
                .setId(String.valueOf(iviFilm.getId()))
                .setNameRus(iviFilm.getTitle())
                .setNameOrigin(iviFilm.getOriginalTitle())
                .setYear(String.valueOf(iviFilm.getYear()))
                .setRatingKinopoisk(iviFilm.getKinopoiskRating())
                .setRatingIMDB(iviFilm.getImdbRating())
                .setFeesInWorld(String.valueOf(iviFilm.getGrossWorld()))
                .setFeesInRussia(String.valueOf(iviFilm.getGrossRussia()))
                .setReleaseDateInWorld(iviFilm.getReleaseDate())
                .setGenreList(getGenres(iviFilm.getGenres()))
                .setCountry(getCountry(iviFilm.getCountry()))
                .setUrl(iviFilm.getShareLink());
    }

    private List<String> getGenres(List<Long> iviGenres) {
        IviCategoryWrapper categoryWrapper = helper.getForObject(String.format(Constants.API_CATEGORIES_AND_GENRES_URL, session),
                new TypeReference<IviCategoryWrapper>(){}, "Cannot get category").orElse(null);
        if (Objects.nonNull(categoryWrapper)) {
            return categoryWrapper.getResult().stream()
                    .filter(category -> category.getId() == 14) // id категории фильмов 14
                    .map(IviCategory::getGenres)
                    .flatMap(Collection::stream)
                    .filter(genre -> iviGenres.contains(genre.getId()))
                    .map(IviCategory::getTitle)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String getCountry(Integer countryId) {
        IviCountryWrapper countryWrapper = helper.getForObject(String.format(Constants.API_COUNTRY_URL, session),
                new TypeReference<IviCountryWrapper>(){}, "Cannot get country").orElse(null);
        if (Objects.nonNull(countryWrapper)) {
            IviCountry country = countryWrapper.getResult().entrySet().stream()
                    .filter(entry -> entry.getKey().equals(String.valueOf(countryId)))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            return Objects.isNull(country) ? StringUtils.EMPTY : country.getTitle();
        }
        return StringUtils.EMPTY;
    }

}
