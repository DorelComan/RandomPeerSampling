package de.tum.group34.serialization;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class FileParser {


    private INIConfiguration ini;

    public FileParser(String file) throws IOException, ConfigurationException {

        ini = new INIConfiguration();
        ini.read(new FileReader(file));
    }

    public String getHostkey(){

        return ini.getString("HOSTKEY");
    }

    public int getGossipPort() throws URISyntaxException {

        URI uri = new URI(ini.getSection("GOSSIP").getString("api_address"));

        return uri.getPort();
    }

    public int getNSEPort() throws URISyntaxException {

        URI uri = new URI(ini.getSection("NSE").getString("api_address"));

        return uri.getPort();
    }




}
