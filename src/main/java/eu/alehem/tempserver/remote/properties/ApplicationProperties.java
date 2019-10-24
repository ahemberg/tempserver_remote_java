package eu.alehem.tempserver.remote.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationProperties {

  private final Properties properties;

  private ApplicationProperties() {
    try {
      properties = getProperties();
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public static ApplicationProperties getInstance() {
    return SingletonHelper.INSTANCE;
  }

  private Properties getProperties() throws IOException {
    InputStream input =
        ApplicationProperties.class.getClassLoader().getResourceAsStream("config.properties");
    Properties prop = new Properties();

    prop.load(input);
    return prop;
  }

  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  private static class SingletonHelper {
    private static final ApplicationProperties INSTANCE = new ApplicationProperties();
  }
}
