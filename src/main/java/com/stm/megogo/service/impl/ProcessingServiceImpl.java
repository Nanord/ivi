package com.stm.megogo.service.impl;

import com.stm.megogo.exception.ProcessingException;
import com.stm.megogo.parser.ParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.stm.megogo.service.ProcessingService;
import com.stm.megogo.utils.DateUtils;

@Service
@Slf4j
public class ProcessingServiceImpl implements ProcessingService {

    @Autowired
    private ParserService parserService;

    @Override
    public void start() throws ProcessingException {
        long startTime = System.currentTimeMillis();
        try {
            parserService.parse();
        } catch (Exception e) {
            log.error("Exception in parser");
            throw new ProcessingException("Exception in parser", e);
        }
        DateUtils.printExecutionTime(startTime);

    }

}
