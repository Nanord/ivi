package com.stm.megogo.parser;

import com.stm.megogo.pojo.Film;
import org.jsoup.nodes.Document;

public interface KinopoiskParserService {
    Film parse(Document document);
}
