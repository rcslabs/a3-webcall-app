package com.rcslabs.a3.config;

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
            if(s3.length < 2){ continue; }
            data.setProperty(s3[0].substring(2), s3[1]);
        }
    }
}
