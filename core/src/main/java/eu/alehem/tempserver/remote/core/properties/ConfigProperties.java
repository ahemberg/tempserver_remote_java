package eu.alehem.tempserver.remote.core.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {

  private final Properties properties;

  private ConfigProperties() {
    try {
      properties = getProperties();
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public static ConfigProperties getInstance() {
    return SingletonHelper.INSTANCE;
  }

  private Properties getProperties() throws IOException {
    InputStream input =
        ConfigProperties.class.getClassLoader().getResourceAsStream("config.properties");
    Properties prop = new Properties();

    prop.load(input);
    return prop;
  }

  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  private static class SingletonHelper {
    private static final ConfigProperties INSTANCE = new ConfigProperties();
  }
}
