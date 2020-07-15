package ru.mts.megogo.parser;

import org.jsoup.nodes.Document;
import ru.mts.megogo.pojo.Film;

public interface MegogoParserService {
    Film parse(Document document);
}
