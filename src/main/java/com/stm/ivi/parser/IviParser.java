package com.stm.ivi.parser;

import com.stm.ivi.pojo.Film;
import com.stm.ivi.pojo.ivi.IviFilm;

public interface IviParser {
    Film mapIviFilmToFilm(IviFilm iviFilm);
}
