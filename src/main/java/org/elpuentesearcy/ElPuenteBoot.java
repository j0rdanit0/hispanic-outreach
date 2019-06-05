package org.elpuentesearcy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class ElPuenteBoot
{
    public static final String IMAGE_FOLDER = "/images/";
    public static String IMAGE_DIRECTORY;

    public static void main( String[] args ) throws IOException
    {
        IMAGE_DIRECTORY = new File( "." ).getCanonicalPath() + IMAGE_FOLDER;
        SpringApplication.run( ElPuenteBoot.class, args );
    }
}