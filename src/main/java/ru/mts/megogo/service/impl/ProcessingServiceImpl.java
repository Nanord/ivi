package ru.mts.megogo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mts.megogo.exception.ProcessingException;
import ru.mts.megogo.parser.ParserService;
import ru.mts.megogo.service.ProcessingService;
import ru.mts.megogo.utils.DateUtils;

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
