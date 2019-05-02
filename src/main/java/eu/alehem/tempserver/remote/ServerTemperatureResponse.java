package eu.alehem.tempserver.remote;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Bean for server response to temperature save request
 *
 * If connection was successful then the server will respond like this:
 *
 * {
 *  "status":1,
 *  "saved_data":
 *      [
 *          {"id":83147,"measurement_time":"2019-05-02 09:00:02","temp":6}
 *      ],
 *      "msg":"TEMP_SAVE_OK"
 * }
 *
 * saved data is a list containing all the measurements that were saved.
 *
 */
public class ServerTemperatureResponse {

    @SerializedName("status") private int serverStatus;
    @SerializedName("msg") private String serverMessage;
    @SerializedName("saved_data") List<TemperatureMeasurement> savedTemperatures;

    ServerTemperatureResponse(int serverStatus, String serverMessage, List<TemperatureMeasurement> savedTemperatures) {
        this.serverStatus = serverStatus;
        this.serverMessage = serverMessage;
        this.savedTemperatures = savedTemperatures;
    }

    public int getServerStatus() {
        return serverStatus;
    }

    public String getServerMessage() {
        return serverMessage;
    }

    public List<TemperatureMeasurement> getSavedTemperatures() {
        return savedTemperatures;
    }

    @Override
    public String toString() {
        return "ServerTemperatureResponse{" +
                "serverStatus=" + serverStatus +
                ", serverMessage='" + serverMessage + '\'' +
                ", savedTemperatures=" + savedTemperatures +
                '}';
    }
}
