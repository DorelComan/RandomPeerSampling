package de.tum.group34.serialization;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class FileParser {

  private INIConfiguration ini;

  public FileParser(String file) throws IOException, ConfigurationException {

    ini = new INIConfiguration();
    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file),
        StandardCharsets.UTF_8)) {
      ini.read(reader);
    }
  }

  public String getHostkey() {

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
