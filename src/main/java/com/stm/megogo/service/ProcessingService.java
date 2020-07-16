package com.stm.megogo.service;

import com.stm.megogo.exception.ProcessingException;

import javax.naming.ConfigurationException;

public interface ProcessingService {

    void start() throws ConfigurationException, ProcessingException;
}
