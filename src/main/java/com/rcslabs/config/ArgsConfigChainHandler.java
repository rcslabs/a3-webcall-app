package com.rcslabs.config;

/**
 * Created by sx on 06.03.14.
 */
public class ArgsConfigChainHandler extends AbstractConfigChainHandler {

    public ArgsConfigChainHandler()
    {
        super();
        String s = System.getProperty("sun.java.command");
        String[] s2 = s.split(" ");
        for(int i = 1; i < s2.length; ++i){
            s = s2[i].trim();
            String[] s3 = s.split("=");
            data.setProperty(s3[0].substring(2), s3[1]);
        }
    }
}
