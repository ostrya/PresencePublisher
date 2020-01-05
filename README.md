# Presence Publisher

[![Android CI](https://github.com/ostrya/presencepublisher/workflows/Android%20CI/badge.svg?branch=master)](https://github.com/ostrya/PresencePublisher/actions?query=branch%3Amaster)
[![Travis CI](https://img.shields.io/travis/ostrya/presencepublisher/master?logo=travis)](https://travis-ci.com/ostrya/PresencePublisher)
[![Github release date](https://img.shields.io/github/release-date/ostrya/presencepublisher.svg?logo=github) ![Github release](https://img.shields.io/github/release/ostrya/presencepublisher.svg?logo=github)](https://github.com/ostrya/PresencePublisher/releases)
[![F-Droid release](https://img.shields.io/f-droid/v/org.ostrya.presencepublisher.svg)](https://f-droid.org/packages/org.ostrya.presencepublisher)

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="75">](https://f-droid.org/packages/org.ostrya.presencepublisher)
[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="75" alt="Get it on Google Play">](https://play.google.com/store/apps/details?id=org.ostrya.presencepublisher&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1)

A simple app that regularly publishes to a configurable MQTT topic whenever connected to a given WiFi network or in
proximity to a Bluetooth beacon. This can be used to integrate the presence of your phone in home automation.
 
Several networks and beacons can be configured at once and the message to be sent can be configured for each of them.

If your MQTT server is available on the internet, you can also choose to send an 'offline' message
whenever you are not connected to any of the configured WiFi networks and not in range of any configured beacon.

As an additional feature, you can send the battery level of your device whenever a condition is met, so that you can
recharge it before it turns off.

The app uses the built-in Android alarm manager, so notifications are sent even if the phone is in stand-by.

## TLS with self-signed certificates

The app uses the default Android CA trust store for checking the server certificate validity. You can simply add your
certificate via:

* Android 4.0 - 7.1:
  * `Settings` → `Security` → `Install from SD card`
* Android 8.0+:
  * `Settings` → `Security & location` → `Encryption & credentials` → `Install from SD card`

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

## Permissions

* ACCESS_BACKGROUND_LOCATION: on Android 10+, necessary to retrieve SSID of connected WiFi while running in background
* ACCESS_FINE_LOCATION: on Android 9+, necessary to retrieve SSID of connected WiFi (you do not need to grant
 the permission in Android 6.0 - 8.1 for the app to work)
* ACCESS_NETWORK_STATE: necessary to register network change listener
* ACCESS_WIFI_STATE: necessary to retrieve SSID of connected WiFi
* BLUETOOTH: necessary to communicate with beacons
* BLUETOOTH_ADMIN: necessary to discover beacons
* INTERNET: only necessary if your MQTT server is not running locally
* RECEIVE_BOOT_COMPLETED: necessary to start service on start-up
* REQUEST_IGNORE_BATTERY_OPTIMIZATIONS: on Android 6+, necessary to request disabling battery optimization
* WRITE_EXTERNAL_STORAGE: only necessary if you want to export log files in Android 4.0 - 4.3

## Future ideas

* more conditions when to send notification
  * time ranges
  * actual location
  * ...
* ...
