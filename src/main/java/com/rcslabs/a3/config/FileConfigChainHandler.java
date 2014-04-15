package com.rcslabs.a3.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;

public class FileConfigChainHandler extends AbstractConfigChainHandler {

    protected final static Logger log = LoggerFactory.getLogger(FileConfigChainHandler.class);

    public void readFile(String filename){
        try {
            data.load(new FileReader(filename));
        } catch (IOException e) {
            log.error("Error while config loading from file "
                    + filename + " : " + e.getMessage());
        }
    }
}
