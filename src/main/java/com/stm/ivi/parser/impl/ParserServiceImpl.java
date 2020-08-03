package com.stm.ivi.parser.impl;

import com.stm.ivi.parser.ParserService;
import com.stm.ivi.service.PageService;
import com.stm.ivi.service.SaveFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ParserServiceImpl implements ParserService {
    @Autowired
    private PageService pageService;
    @Autowired
    private SaveFile saveFile;

    @Override
    public void parse() {

    }

}
