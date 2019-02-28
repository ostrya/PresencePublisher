package org.ostrya.presencepublisher.mqtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.Charset;

import static org.ostrya.presencepublisher.ui.ConnectionFragment.*;

public class MqttService {
    private static final String TAG = MqttService.class.getSimpleName();

    private final AndroidSslSocketFactoryFactory factory;
    private final SharedPreferences sharedPreferences;

    public MqttService(Context context, SharedPreferences sharedPreferences) {
        this.factory = new AndroidSslSocketFactoryFactory(context);
        this.sharedPreferences = sharedPreferences;
    }

    public void sendPing(String status) throws MqttException {
        Log.d(TAG, "Try pinging server");
        String topic = sharedPreferences.getString(TOPIC, "topic");
        boolean tls = sharedPreferences.getBoolean(TLS, false);
        String clientCertAlias = sharedPreferences.getString(CLIENT_CERT, null);
        String login = sharedPreferences.getString(LOGIN, "");
        String password = sharedPreferences.getString(PASSWORD, "");

        MqttClient mqttClient = new MqttClient(getMqttUrl(tls), Settings.Secure.ANDROID_ID, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setConnectionTimeout(5);
        if(!login.isEmpty() && !password.isEmpty()) {
            options.setUserName(login);
            options.setPassword(password.toCharArray());
        }
        if (tls) {
            options.setSocketFactory(factory.getSslSocketFactory(clientCertAlias));
        }
        mqttClient.connect(options);
        mqttClient.publish(topic, status.getBytes(Charset.forName("UTF-8")), 0, false);
        mqttClient.disconnect(5);
        mqttClient.close(true);
        Log.d(TAG, "Ping successful");
    }

    private String getMqttUrl(boolean tls) {
        String host = sharedPreferences.getString(HOST, "localhost");
        String port = sharedPreferences.getString(PORT, null);
        String protocolPrefix = tls ? "ssl://" : "tcp://";
        String portAppendix = port == null ? "" : ":" + port;
        return protocolPrefix + host + portAppendix;
    }
}
