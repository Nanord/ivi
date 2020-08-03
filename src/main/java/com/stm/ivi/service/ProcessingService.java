package com.stm.ivi.service;

import com.stm.ivi.exception.ProcessingException;

import javax.naming.ConfigurationException;

public interface ProcessingService {

    void start() throws ConfigurationException, ProcessingException;
}
