package com.stm.megogo.parser;

import com.stm.megogo.pojo.Film;
import org.jsoup.nodes.Document;

public interface MegogoParserService {
    Film parse(Document document);
}
