# Presence Publisher

[<img src="https://img.shields.io/travis/ostrya/presencepublisher?logo=travis" alt="Travis CI">](https://travis-ci.com/ostrya/PresencePublisher)
[<img src="https://img.shields.io/github/release/ostrya/presencepublisher.svg?logo=github" alt="Github release">](https://github.com/ostrya/PresencePublisher/releases)
[<img src="https://img.shields.io/f-droid/v/org.ostrya.presencepublisher.svg" alt="F-Droid release">](https://f-droid.org/packages/org.ostrya.presencepublisher)

[<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="75">](https://f-droid.org/packages/org.ostrya.presencepublisher)
[<img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="75" alt="Get it on Google Play">](https://play.google.com/store/apps/details?id=org.ostrya.presencepublisher&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1)

A simple app that regularly publishes to a configurable MQTT topic whenever connected to a given WiFi network.
This can be used to integrate the presence of your phone in home automation. Several networks can be configured
at once and the message to be sent can be configured for each network.

Additionally, if your MQTT server is available on the internet, you can also choose to send messages whenever
you are not connected to any of the configured WiFi networks.

The app uses the built-in Android alarm manager, so notifications are sent even if the phone is in stand-by.

## TLS with self-signed certificates

The app uses the default Android CA trust store for checking the server certificate validity. You can simply add your
certificate via:

* Android 4.0 - 7.1:
  * `Settings` → `Security` → `Install from SD card`
* Android 8.0+:
  * `Settings` → `Security & location` → `Encryption & credentials` → `Install from SD card`

## Permissions

* ACCESS_COARSE_LOCATION: on Android 9+, necessary to retrieve SSID of connected WiFi (you do not need to grant
 the permission in Android 6.0 - 8.1 for the app to work)
* ACCESS_NETWORK_STATE: necessary to register network change listener
* ACCESS_WIFI_STATE: necessary to retrieve SSID of connected WiFi
* INTERNET: only necessary if your MQTT server is not running locally
* RECEIVE_BOOT_COMPLETED: necessary to start service on start-up
* REQUEST_IGNORE_BATTERY_OPTIMIZATIONS: on Android 6+, necessary to request disabling battery optimization
* WRITE_EXTERNAL_STORAGE: only necessary if you want to export log files in Android 4.0 - 4.3

## Future ideas

* more conditions when to send notification
  * time ranges
  * presence of Bluetooth beacons
  * actual location
  * ...
* ...
