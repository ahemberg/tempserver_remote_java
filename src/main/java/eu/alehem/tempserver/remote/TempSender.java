package eu.alehem.tempserver.remote;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TempSender implements Runnable {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private final int BATCH_SIZE = 100;
  private final String mySerial = "000000001938eb39"; // TODO FIX THIS
  private final int myID = 6; // TODO FIX THIS;
  private final String serverAddress = "https://alehem.eu/api/save_temp"; // TODO FIX THIS
  private TempQueue queue = TempQueue.getInstance();
  private Random idGenerator = new Random();

  @Override
  public void run() {
    if (queue.getQueueLen() < 1) {
      return;
    }

    queue.setRemoveLock(true);
    Set<Temperature> temperatures = queue.getN(BATCH_SIZE);

    List<TemperatureMeasurement> temperatureMeasurements =
        temperatures.stream()
            .map(
                t ->
                    new TemperatureMeasurement(
                        idGenerator.nextInt(10000),
                        t.getMeasurementTimeServerFormat(),
                        t.getTemperature()))
            .collect(Collectors.toList());

    TemperaturePost postData = new TemperaturePost(myID, mySerial, temperatureMeasurements);

    try {
      ServerTemperatureResponse response = sendToServer(new Gson().toJson(postData));
      // LOGGER.info("SENDER: Got from server:");
      // LOGGER.info(new Gson().toJson(response));
      if (response.getServerStatus() == 1) {
        LOGGER.info("SENDER: Server responded with OK");
        List<TemperatureMeasurement> temperaturesSavedOnServer = response.getSavedTemperatures();

        // TODO: This is hack due to data lost with probeSerial not being available. Must update
        // server code!
        if (temperaturesSavedOnServer.equals(temperatureMeasurements)) {
          LOGGER.info("SENDER: Removing temperatures from queue");
          queue.setRemoveLock(false);
          queue.removeTemperatures(temperatures);
        }
      } else {
        LOGGER.warning("Server rejected transaction");
        LOGGER.warning("Server status: " + response.getServerStatus());
        LOGGER.warning("Server message: " + response.getServerMessage());
      }
    } catch (ServerCommsFailedException e) {
      LOGGER.info(e.getMessage());
    }
    queue.setRemoveLock(false);
  }

  private ServerTemperatureResponse sendToServer(String jsonData)
      throws ServerCommsFailedException {
    try {
      LOGGER.info("SENDER: Sending to server");
      // LOGGER.info(jsonData);
      HttpClient client = HttpClientBuilder.create().build();
      HttpPost httpPost = new HttpPost(serverAddress);

      StringEntity entity =
          new StringEntity("data=" + jsonData, ContentType.APPLICATION_FORM_URLENCODED);
      httpPost.setEntity(entity);
      HttpResponse response = client.execute(httpPost);

      Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
      ServerTemperatureResponse result =
          new Gson().fromJson(reader, ServerTemperatureResponse.class);
      if (result == null) {
        throw new ServerCommsFailedException();
      }
      return result;
    } catch (Throwable t) {
      LOGGER.warning("Failed to save temperature to server.");
      // t.printStackTrace();
      throw new ServerCommsFailedException();
    }
  }
}

class ServerCommsFailedException extends Exception {
  ServerCommsFailedException() {
    super("ERROR: Server communication failed");
  }
}
