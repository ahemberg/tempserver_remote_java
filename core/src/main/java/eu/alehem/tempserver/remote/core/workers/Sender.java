package eu.alehem.tempserver.remote.core.workers;

import eu.alehem.tempserver.remote.core.exceptions.ServerCommsFailedException;
import eu.alehem.tempserver.schema.proto.Tempserver;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

@Log4j2
@AllArgsConstructor
public class Sender implements Function<Set<Tempserver.Measurement>, Set<String>> {

  private final String mySerial;
  private final String remoteId;
  private final String serverAddress;

  @Override
  public Set<String> apply(Set<Tempserver.Measurement> measurements) {
    try {
      final Tempserver.MeasurementSaveRequest serverMessage =
          Tempserver.MeasurementSaveRequest.newBuilder()
              .setRemoteId(remoteId)
              .setRemoteSerial(mySerial)
              .addAllMeasurements(measurements)
              .build();

      try {
        final Tempserver.MeasurementSaveResponse response =
            sendToServer(serverMessage.toByteArray());
        if (response.getSaveSuccess()) {
          log.debug("Server response: " + response.getResponseCode().toString());
          log.debug("Done sending batch");

        } else {
          log.warn("Server rejected transaction");
          log.warn("Server response: " + response.getResponseCode());
        }
        return new HashSet<>(response.getMeasurementIdsList());
      } catch (ServerCommsFailedException e) {
        log.warn("Server communication failed");
        log.debug("Exception", e);
        return new HashSet<>();
      }
    } catch (Exception e) {
      log.warn("Sender crashed with exception!", e);
      return new HashSet<>();
    }
  }

  // TODO Think about rewriting this
  private Tempserver.MeasurementSaveResponse sendToServer(final byte[] data)
      throws ServerCommsFailedException {
    try {
      log.debug("Sending to server");
      HttpClient client = HttpClientBuilder.create().build();
      HttpPost httpPost = new HttpPost(serverAddress);

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
