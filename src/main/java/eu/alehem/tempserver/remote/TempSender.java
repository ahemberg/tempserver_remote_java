package eu.alehem.tempserver.remote;

import com.google.gson.Gson;
import eu.alehem.tempserver.remote.exceptions.ServerCommsFailedException;
import eu.alehem.tempserver.remote.properties.ApplicationProperties;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.java.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

@Log
public class TempSender implements Runnable {

  private final int BATCH_SIZE;
  private final String mySerial;
  private final UUID myID;
  private final String serverAddress;
  private TempQueue queue = TempQueue.getInstance();

  TempSender(String mySerial) {
    ApplicationProperties properties = ApplicationProperties.getInstance();
    this.mySerial = mySerial;
    this.BATCH_SIZE = Integer.valueOf(properties.getProperty("sender.batch_size"));
    this.myID = UUID.fromString(properties.getProperty("sender.pi_uuid"));
    this.serverAddress = properties.getProperty("sender.server_address");
  }

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
                        t.getId(), t.getMeasurementTime(), t.getTemperature()))
            .collect(Collectors.toList());

    TemperaturePost postData = new TemperaturePost(myID, mySerial, temperatureMeasurements);

    try {
      ServerTemperatureResponse response = sendToServer(new Gson().toJson(postData));
      if (response.isSaveSuccessful()) {
        log.info("Server response: " + response.getMessage());
        Set<Temperature> temperaturesSavedOnServer =
            temperatureMeasurements.stream()
                .filter(m -> response.getSavedMeasurements().contains(m.getMeasurementId()))
                .map(
                    tm ->
                        temperatures.stream()
                            .filter(t -> t.getId() == tm.getMeasurementId())
                            .collect(Collectors.toList())
                            .get(0))
                .collect(Collectors.toSet());

        log.info("Removing temperatures from queue");
        queue.setRemoveLock(false);
        queue.removeTemperatures(temperaturesSavedOnServer);
      } else {
        log.warning("Server rejected transaction");
        log.warning("Server status: " + response.getResponseCode());
        log.warning("Server message: " + response.getMessage());
      }
    } catch (ServerCommsFailedException e) {
      log.info(e.getMessage());
    }
    queue.setRemoveLock(false);
  }

  private ServerTemperatureResponse sendToServer(String jsonData)
      throws ServerCommsFailedException {
    try {
      log.info("Sending to server");
      HttpClient client = HttpClientBuilder.create().build();
      HttpPost httpPost = new HttpPost(serverAddress);

      StringEntity entity = new StringEntity(jsonData, ContentType.APPLICATION_JSON);
      httpPost.setEntity(entity);
      HttpResponse response = client.execute(httpPost);

      if (response.getStatusLine().getStatusCode() != 200) {
        log.warning("Got status code: " + response.getStatusLine().getStatusCode());
        throw new ServerCommsFailedException();
      }

      Reader reader =
          new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
      ServerTemperatureResponse result =
          new Gson().fromJson(reader, ServerTemperatureResponse.class);
      if (result == null) {
        throw new ServerCommsFailedException();
      }
      return result;
    } catch (ServerCommsFailedException | IOException e) {
      log.warning("Failed to save temperature to server.");
      log.warning(e.getMessage());
      throw new ServerCommsFailedException();
    }
  }
}
