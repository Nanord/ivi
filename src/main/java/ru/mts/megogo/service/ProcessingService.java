package ru.mts.megogo.service;

import ru.mts.megogo.exception.ProcessingException;

import javax.naming.ConfigurationException;

public interface ProcessingService {

    void start() throws ConfigurationException, ProcessingException;
}
