package org.ostrya.presencepublisher.mqtt;

import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.Charset;

import static org.ostrya.presencepublisher.ui.ConnectionFragment.*;
import static org.ostrya.presencepublisher.ui.ScheduleFragment.LAST_PING;

public class MqttService {
    private static final String TAG = MqttService.class.getSimpleName();

    private final SharedPreferences sharedPreferences;

    public MqttService(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void sendPing() {
        try {
            doSendPing();
            sharedPreferences.edit().putLong(LAST_PING, System.currentTimeMillis()).apply();
        } catch (MqttException e) {
            Log.w(TAG, "Error while sending ping", e);
        }
    }

    public void doSendPing() throws MqttException {
        Log.d(TAG, "Try pinging server");
        String topic = sharedPreferences.getString(TOPIC, "topic");

        MqttClient mqttClient = new MqttClient(getMqttUrl(), Settings.Secure.ANDROID_ID, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(5);
        mqttClient.connect(options);
        mqttClient.publish(topic, "online".getBytes(Charset.forName("UTF-8")), 0, false);
        mqttClient.disconnect(5);
        mqttClient.close(true);
        Log.d(TAG, "Ping successful");
    }

    private String getMqttUrl() {
        String host = sharedPreferences.getString(HOST, "localhost");
        String port = sharedPreferences.getString(PORT, null);
        boolean tls = sharedPreferences.getBoolean(TLS, false);
        String protocolPrefix = tls ? "ssl://" : "tcp://";
        String portAppendix = port == null ? "" : ":" + port;
        return protocolPrefix + host + portAppendix;
    }
}
