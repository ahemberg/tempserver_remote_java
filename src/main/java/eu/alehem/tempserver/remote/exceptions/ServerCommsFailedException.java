package eu.alehem.tempserver.remote.exceptions;

public class ServerCommsFailedException extends Exception {
  public ServerCommsFailedException() {
    super("ERROR: Server communication failed");
  }
}
