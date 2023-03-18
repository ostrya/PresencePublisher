package org.ostrya.presencepublisher.mqtt;

import static org.ostrya.presencepublisher.PresencePublisher.MQTT_CLIENT_ID;
import static org.ostrya.presencepublisher.preference.connection.ClientCertificatePreference.CLIENT_CERTIFICATE;
import static org.ostrya.presencepublisher.preference.connection.HostPreference.HOST;
import static org.ostrya.presencepublisher.preference.connection.PortPreference.PORT;
import static org.ostrya.presencepublisher.preference.connection.QoSPreference.DEFAULT_VALUE;
import static org.ostrya.presencepublisher.preference.connection.QoSPreference.QOS_VALUE;
import static org.ostrya.presencepublisher.preference.connection.RetainFlagPreference.RETAIN_FLAG;
import static org.ostrya.presencepublisher.preference.connection.UseTlsPreference.USE_TLS;
import static org.ostrya.presencepublisher.preference.connection.UsernamePreference.USERNAME;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.core.util.Supplier;
import androidx.preference.PreferenceManager;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.mqtt.message.Message;
import org.ostrya.presencepublisher.preference.connection.PasswordPreference;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class MqttService {
    private static final String TAG = "MqttService";

    private static final byte[] TEST_PAYLOAD = {0x74, 0x65, 0x73, 0x74};

    private final AndroidSslSocketFactoryFactory factory;
    private final SharedPreferences sharedPreferences;
    private final Supplier<String> passwordProvider;
    private final String clientId;

    public MqttService(Context context) {
        Context applicationContext = context.getApplicationContext();
        factory = new AndroidSslSocketFactoryFactory(applicationContext);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        passwordProvider = PasswordPreference.getPasswordProvider(applicationContext);
        clientId =
                sharedPreferences.getString(
                        MQTT_CLIENT_ID, "initialization error " + System.currentTimeMillis());
    }

    public void sendTestMessage() throws MqttException {
        DatabaseLogger.i(TAG, "Sending test message to server");
        boolean tls = sharedPreferences.getBoolean(USE_TLS, false);
        String clientCertAlias = sharedPreferences.getString(CLIENT_CERTIFICATE, null);
        String login = sharedPreferences.getString(USERNAME, "");
        String password = passwordProvider.get();
        String topic = "test";

        try (MqttClient mqttClient =
                new MqttClient(getMqttUrl(tls), clientId, new MemoryPersistence())) {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setConnectionTimeout(5);
            if (!login.isEmpty() && !password.isEmpty()) {
                options.setUserName(login);
                options.setPassword(password.toCharArray());
            }
            if (tls) {
                options.setSocketFactory(factory.getSslSocketFactory(clientCertAlias));
            }
            mqttClient.connect(options);
            mqttClient.publish(topic, TEST_PAYLOAD, 1, false);
            mqttClient.disconnect(5);
        }
        DatabaseLogger.i(TAG, "Sending messages was successful");
    }

    public void sendMessages(List<Message> messages) throws MqttException {
        DatabaseLogger.i(TAG, "Sending " + messages.size() + " messages to server");
        boolean tls = sharedPreferences.getBoolean(USE_TLS, false);
        String clientCertAlias = sharedPreferences.getString(CLIENT_CERTIFICATE, null);
        String login = sharedPreferences.getString(USERNAME, "");
        String password = passwordProvider.get();
        int qos = getQosFromString(sharedPreferences.getString(QOS_VALUE, null));
        boolean retain = sharedPreferences.getBoolean(RETAIN_FLAG, false);

        try (MqttClient mqttClient =
                new MqttClient(getMqttUrl(tls), clientId, new MemoryPersistence())) {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setConnectionTimeout(5);
            if (!login.isEmpty() && !password.isEmpty()) {
                options.setUserName(login);
                options.setPassword(password.toCharArray());
            }
            if (tls) {
                options.setSocketFactory(factory.getSslSocketFactory(clientCertAlias));
            }
            mqttClient.connect(options);
            for (Message message : messages) {
                mqttClient.publish(
                        message.getTopic(), message.getContent().getBytes("UTF-8"), qos, retain);
                DatabaseLogger.logMessage(message);
            }
            mqttClient.disconnect(5);
        } catch (UnsupportedEncodingException e) {
            DatabaseLogger.e(TAG, "Unable to find UTF-8 encoding", e);
        }
        DatabaseLogger.i(TAG, "Sending messages was successful");
    }

    private String getMqttUrl(boolean tls) {
        String host = sharedPreferences.getString(HOST, "localhost");
        String port = sharedPreferences.getString(PORT, null);
        String protocolPrefix = tls ? "ssl://" : "tcp://";
        String portAppendix = port == null ? "" : ":" + port;
        return protocolPrefix + host + portAppendix;
    }

    private int getQosFromString(@Nullable String stringValue) {
        if (stringValue != null) {
            try {
                return Integer.parseInt(stringValue);
            } catch (NumberFormatException e) {
                DatabaseLogger.w(TAG, "Unable to parse QoS value " + stringValue);
            }
        }
        return DEFAULT_VALUE;
    }
}
