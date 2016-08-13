package de.tum.group34.serialization;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

  public byte[] getHostkey() throws IOException {

    String pathStr = ini.getString("HOSTKEY");
    Path path = Paths.get(pathStr);

    return Files.readAllBytes(path);
  }

  public InetSocketAddress getGossipAddress() throws URISyntaxException {

    URI uri = new URI(ini.getSection("GOSSIP").getString("api_address"));

    return new InetSocketAddress(uri.getHost(), uri.getPort());
  }

  public InetSocketAddress getNseAddress() throws URISyntaxException {

    URI uri = new URI(ini.getSection("NSE").getString("api_address"));

    return new InetSocketAddress(uri.getHost(), uri.getPort());
  }

  public int getQueryServerPort() throws URISyntaxException {

    URI uri = new URI(ini.getSection("RPS").getString("query_server_address"));

    return uri.getPort();
  }
}
