package ru.mts.megogo.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import ru.mts.megogo.exception.ProcessingException;
import ru.mts.megogo.service.ProcessingService;

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
