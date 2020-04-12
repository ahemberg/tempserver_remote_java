package eu.alehem.tempserver.remote.core.workers;

import eu.alehem.tempserver.remote.core.TempQueue;
import eu.alehem.tempserver.remote.core.Temperature;
import eu.alehem.tempserver.remote.core.exceptions.ServerCommsFailedException;
import eu.alehem.tempserver.remote.core.properties.SenderProperties;
import eu.alehem.tempserver.schema.proto.Tempserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class Sender implements Runnable {

  private final String mySerial;
  private final SenderProperties properties;
  private final TempQueue queue = TempQueue.getInstance();

  public Sender(final String mySerial, final SenderProperties properties) {
    this.mySerial = mySerial;
    this.properties = properties;
  }

  private static Set<Tempserver.Measurement> makeProtoMessages(
      final Set<Temperature> temperatures) {
    return temperatures.stream()
        .map(
            t ->
                Tempserver.Measurement.newBuilder()
                    .setType(Tempserver.MeasurementType.TEMPERATURE)
                    .setId(t.getId().toString())
                    .setValue(t.getTemperature())
                    .setTimestamp(t.getMeasurementTime().toEpochMilli())
                    .setProbeserial(t.getProbeSerial())
                    .build())
        .collect(Collectors.toSet());
  }

  /**
   * Checks whether a string is a valid UUID or not
   *
   * @param candidate String to check
   * @return Boolean true if valid false otherwise.
   */
  private static boolean isValidUUID(final String candidate) {
    try {
      UUID.fromString(candidate);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public void run() {
    try {
      queue.setRemoveLock(true);
      final Set<Temperature> temperatures = queue.getN(properties.getBatchSize());

      if (temperatures.isEmpty()) {
        queue.setRemoveLock(false);
        return;
      }

      final Tempserver.MeasurementSaveRequest serverMessage =
          Tempserver.MeasurementSaveRequest.newBuilder()
              .setRemoteId(properties.getRemoteId().toString())
              .setRemoteSerial(mySerial)
              .addAllMeasurements(makeProtoMessages(temperatures))
              .build();

      final Tempserver.MeasurementSaveResponse response;

      try {
        response = sendToServer(serverMessage.toByteArray());
      } catch (ServerCommsFailedException e) {
        log.warn("Server communication failed", e);
        queue.setRemoveLock(false);
        return;
      }

      if (response.getSaveSuccess()) {
        if (properties.isVerbose())
          log.info("Server response: " + response.getResponseCode().toString());
        if (properties.isVerbose()) log.info("Removing temperatures from queue");
        queue.setRemoveLock(false);
        queue.removeTemperaturesById(
            response.getMeasurementIdsList().stream()
                .filter(Sender::isValidUUID)
                .map(UUID::fromString)
                .collect(Collectors.toSet()));
        if (properties.isVerbose()) log.info("Done sending batch");
      } else {
        log.warn("Server rejected transaction");
        log.warn("Server response: " + response.getResponseCode());
      }
    } catch (Exception e) {
      log.warn("Sender crashed with exception!", e);
    }
    queue.setRemoveLock(false);
  }

  private Tempserver.MeasurementSaveResponse sendToServer(final byte[] data)
      throws ServerCommsFailedException {
    try {
      if (properties.isVerbose()) log.info("Sending to server");
      HttpClient client = HttpClientBuilder.create().build();
      HttpPost httpPost = new HttpPost(properties.getServerAddress());

      ByteArrayEntity entity =
          new ByteArrayEntity(data, ContentType.create("application/x-protobuf"));
      httpPost.setEntity(entity);
      HttpResponse response = client.execute(httpPost);

      if (response.getStatusLine().getStatusCode() != 200) {
        log.warn("Got status code: " + response.getStatusLine().getStatusCode());
        throw new ServerCommsFailedException();
      }

      return Tempserver.MeasurementSaveResponse.parseFrom(response.getEntity().getContent());
    } catch (ServerCommsFailedException | IOException e) {
      log.warn("Failed to save temperature to server.", e);
      throw new ServerCommsFailedException();
    }
  }
}
