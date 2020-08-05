package com.stm.ivi.parser.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stm.ivi.parser.IviParser;
import com.stm.ivi.parser.ParserService;
import com.stm.ivi.pojo.Film;
import com.stm.ivi.pojo.ivi.*;
import com.stm.ivi.service.JsoupApiHelper;
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
    private SaveFile saveFile;
    @Autowired
    private JsoupApiHelper helper;
    @Autowired
    private IviParser iviParser;

    @Override
    public void parse() {
        int currentCount = 0;
        while (true) {
            log.info("Get films from {} to {}", currentCount, currentCount + 100);
            IviResultWrapper data = helper.getForObject(String.format(Constants.API_MOVIES_URL, currentCount, helper.getIviSession()),
                    new TypeReference<IviResultWrapper>(){}, "Cannot get data").orElse(null);
            helper.refreshSession();
            if (Objects.nonNull(data)) {
                data.getResult().stream()
                        .map(iviParser::mapIviFilmToFilm)
                        .filter(Objects::nonNull)
                        .forEach(film -> saveFile.save(film));
                currentCount += 100;
                if (currentCount > data.getCount()) {
                    break;
                }
            }
        }
    }

}
