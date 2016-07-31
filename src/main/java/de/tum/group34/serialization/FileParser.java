package de.tum.group34.serialization;

import org.ini4j.Ini;
import java.io.FileReader;
import java.io.IOException;

public class FileParser {

    Ini ini;

    public FileParser(String file) throws IOException {

        ini = new Ini();
        ini.load(new FileReader(file));
    }

}
