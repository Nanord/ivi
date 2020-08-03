package com.stm.ivi.command;

import com.stm.ivi.exception.ProcessingException;
import com.stm.ivi.service.ProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import javax.naming.ConfigurationException;

@Service
@Slf4j
public class StartParserCommand implements CommandLineRunner {
    @Autowired
    private ProcessingService processingService;

    @Override
    public void run(String... args) {
        try {
            processingService.start();
        } catch (ProcessingException | ConfigurationException e) {
            log.error("Error ", e);
        }
    }
}
