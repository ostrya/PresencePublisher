# Presence Publisher
A simple app that regularly publishes to a configurable MQTT topic whenever connected to a given WiFi network.
This can be used to integrate the presence of your phone in home automation.

The app uses the built-in Android alarm manager, so notifications are sent even if the phone is in stand-by.

## Permissions
* ACCESS_COARSE_LOCATION: necessary to retrieve SSID of connected WiFi
* ACCESS_NETWORK_STATE: necessary to register network change listener
* ACCESS_WIFI_STATE: necessary to retrieve SSID of connected WiFi
* INTERNET: only necessary if your MQTT server is not running locally
* FOREGROUND_SERVICE: necessary to send notifications
* RECEIVE_BOOT_COMPLETED: necessary to start service on start-up
