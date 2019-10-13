package org.ostrya.presencepublisher.mqtt;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import androidx.preference.PreferenceManager;
import com.hypertrack.hyperlog.HyperLog;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.ostrya.presencepublisher.message.Message;

import java.nio.charset.Charset;
import java.util.List;

import static org.ostrya.presencepublisher.ui.ConnectionFragment.*;

public class MqttService {
    private static final String TAG = "MqttService";

    private final AndroidSslSocketFactoryFactory factory;
    private final SharedPreferences sharedPreferences;

    public MqttService(Context context) {
        Context applicationContext = context.getApplicationContext();
        factory = new AndroidSslSocketFactoryFactory(applicationContext);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
    }

    public void sendMessages(List<Message> messages) throws MqttException {
        HyperLog.i(TAG, "Sending messages to server");
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
        for (Message message : messages) {
            mqttClient.publish(message.getTopic(), message.getContent().getBytes(Charset.forName("UTF-8")), 0, false);
        }
        mqttClient.disconnect(5);
        mqttClient.close(true);
        HyperLog.i(TAG, "Sending messages was successful");
    }

    private String getMqttUrl(boolean tls) {
        String host = sharedPreferences.getString(HOST, "localhost");
        String port = sharedPreferences.getString(PORT, null);
        String protocolPrefix = tls ? "ssl://" : "tcp://";
        String portAppendix = port == null ? "" : ":" + port;
        return protocolPrefix + host + portAppendix;
    }
}
