package com.rcslabs.a3.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by sx on 06.03.14.
 */
public class DefaultsConfigChainHandler extends AbstractConfigChainHandler {

    protected final static Logger log = LoggerFactory.getLogger(DefaultsConfigChainHandler.class);

    public DefaultsConfigChainHandler(){
        super();
        try {
            data.load(this.getClass().getClassLoader()
                    .getResourceAsStream("default.properties")
            );
        } catch (IOException e) {
            log.error("Error while config defaults loading: " + e.getMessage());
        }
    }
}
