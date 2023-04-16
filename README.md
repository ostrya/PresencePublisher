# Presence Publisher

[![Android CI](https://github.com/ostrya/presencepublisher/workflows/Android%20CI/badge.svg?branch=main)](https://github.com/ostrya/PresencePublisher/actions?query=branch%3Amain)
[![Github release date](https://img.shields.io/github/release-date/ostrya/presencepublisher.svg?logo=github) ![Github release](https://img.shields.io/github/release/ostrya/presencepublisher.svg?logo=github)](https://github.com/ostrya/PresencePublisher/releases)
[![F-Droid release](https://img.shields.io/f-droid/v/org.ostrya.presencepublisher.svg)](https://f-droid.org/packages/org.ostrya.presencepublisher)

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="75">](https://f-droid.org/packages/org.ostrya.presencepublisher)
[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="75" alt="Get it on Google Play">](https://play.google.com/store/apps/details?id=org.ostrya.presencepublisher&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1)

Presence Publisher is a simple app that regularly publishes to a configurable MQTT topic whenever connected to a
given Wi-Fi network or in proximity to a Bluetooth beacon. This can be used to integrate the presence of your phone
in home automation.

Several networks and beacons can be configured at once, and the message to be sent can be configured
for each of them.

If your MQTT server is available on the internet, you can also choose to send an 'offline' message
whenever you are not connected to any of the configured Wi-Fi networks and not in range of any
configured beacon.

As an additional feature, you can send the battery level of your device whenever a condition is met,
so that you can recharge it before it turns off.

The app uses Android's Work Manager library, so notifications are sent even if the phone is in
stand-by.

For details on which data this app processes and how it does so, please have a look at the
[privacy policy](https://ostrya.github.io/PresencePublisher/en/PRIVACY_POLICY.html).

## TLS with self-signed certificates

The app uses the default Android CA trust store for checking the server certificate validity. You can simply add your
certificate via:

* Android 4 - 7:
  * `Security` → `Install from SD card`
* Android 8 - 9:
  * `Security & location` → `Encryption & credentials` → `Install from SD card`
* Android 10:
  * `Security` → `Encryption & credentials` → `Install from SD card`
* Android 11 - 12:
  * `Security` → `Encryption & credentials` → `Install a certificate` → `CA Certificate`
* Android 13+:
  * `Security & privacy` → `More security settings` → `Encryption and credentials` → `Install a certificate` → `CA certificate`

Please note you need to have your certificate in DER format to be able to import it correctly. You can
check this using:

```bash
openssl x509 -inform der -in server.crt -text
```

This should show you the correct certificate information. If you instead see something like `unable to load certificate`,
the certificate is most likely formatted in PEM format and needs to be converted, e.g like this:

```bash
openssl x509 -inform pem -outform der -in server.crt -out server_der.crt
```

Alternatively, you can use the [KeyStore Explorer](https://keystore-explorer.org) to do the conversion:

* `Create a new KeyStore` → choose `PKCS #12`
* `Tools` → `Import Trusted Certificates` → open your certificate file and give it some alias
* Right-click the entry → `Export` → `Export Certificate` → select export format `X.509` and uncheck `PEM`
* afterwards, you can close the keystore without saving it

### Client certificates

The Android keychain will only allow you to import a PKCS#12 keystore. If you have created a client certificate along
the lines of [https://mosquitto.org/man/mosquitto-tls-7.html](https://mosquitto.org/man/mosquitto-tls-7.html),
you will need to combine the certificate and key file together like this:

```bash
openssl pkcs12 -inkey client.key -in client.crt -export -out client.pfx
```

If you do not need your client certificate to be signed by a root certificate, because you plan to add it directly to
the trusted certificates of your MQTT broker, you can also use the [KeyStore Explorer](https://keystore-explorer.org)
to generate your client certificate:

* `Create a new KeyStore` → choose `PKCS #12`
* `Tools` → `Generate Key Pair` → choose one of Android's
  [supported algorithms](https://developer.android.com/training/articles/keystore#SupportedKeyPairGenerators)
  → configure properties of the public certificate → set a password for the private key
* `File` → `Save` → use the same password as above for the keystore

Make sure your PKCS#12 keystore file has the `.pfx` extension, otherwise Android will not recognize it.

To be able to select this client certificate in Presence Publisher, you first need to add it to
the Android Keystore. This works similar to the process for the server certificate:

* Android 4 - 7:
  * `Security` → `Install from SD card`
* Android 8 - 9:
  * `Security & location` → `Encryption & credentials` → `Install from SD card`
* Android 10:
  * `Security` → `Encryption & credentials` → `Install from SD card`
* Android 11 - 12:
  * `Security` → `Encryption & credentials` → `Install a certificate` → `VPN & app user certificate`
* Android 13+:
  * `Security & privacy` → `More security settings` → `Encryption and credentials` → `Install a certificate` → `VPN and app user certificate`

After you have imported your client certificate, you will be able to choose it from the app.

## Permissions

* ACCESS_BACKGROUND_LOCATION: on Android 10+, necessary to retrieve SSID of connected Wi-Fi while running in background
* ACCESS_FINE_LOCATION: necessary to discover beacons; on Android 9+, necessary to retrieve SSID of connected Wi-Fi
* ACCESS_NETWORK_STATE: necessary to register network change listener
* ACCESS_WIFI_STATE: necessary to retrieve SSID of connected Wi-Fi
* BLUETOOTH: necessary up to Android 11 to communicate with beacons
* BLUETOOTH_ADMIN: necessary up to Android 11 to discover beacons
* BLUETOOTH_SCAN: on Android 12+, necessary to discover beacons
* INTERNET: only necessary if your MQTT server is not running locally
* RECEIVE_BOOT_COMPLETED: necessary to start service on start-up
* REQUEST_IGNORE_BATTERY_OPTIMIZATIONS: on Android 6+, necessary to request disabling battery optimization
* WRITE_EXTERNAL_STORAGE: only necessary if you want to export log files in Android 4.0 - 4.3
