# Privacy policy

The following document describes how Presence Publisher collects and uses your personal
data.

## Which personal data is collected?

Presence Publisher regularly collects information about your location in the background,
even if it is not running. The following information is collected:

* Whether the currently connected Wi-Fi network name matches a list of network
  names that you have configured.
* Whether any Bluetooth beacon that you have configured is in range of your
  Android device.
  
Presence Publisher does not process or store your GPS location. It also does not store
any information about Wi-Fi networks and Bluetooth beacons which are not explicitly
configured by you.

Once configured, Presence Publisher collects this information repeatedly with a
configurable time period, but at least every 60 minutes. This collection happens
even when the app is not running.

## How is your data used?

Presence Publisher uses the location information to send notification messages.
These notifications are sent to an MQTT broker that you have specified.

The following notifications based on your location can be configured:

* If the currently connected Wi-Fi network name matches a network that you have
  configured, the message configured for this network will be sent.
* If a Bluetooth beacon that you have configured is in range, the message
  configured for this beacon will be sent.
* If neither of the above conditions match, and you have requested a notification
  for this situation, the message configured for this situation will be sent.

In addition, Presence Publisher stores an internal log. This log includes:

* your configured Bluetooth beacons and Wi-Fi network names
* times when you are connected to a configured Wi-Fi network, together with its name
* times when you are in range of a configured Bluetooth beacon, together with its name

This log information can be viewed from within Presence Publisher, but is not accessible
from the outside. Presence Publisher will keep logs for 7 days. It will automatically delete
logs older than 7 days.

You can choose to export the log information into a file. This will cause all
log entries currently retained in Presence Publisher to be written to the external storage.
Once written, this file is no longer modified. In particular, no new entries will
be added. The exported log file is accessible by other apps and will not be
cleaned up automatically.

## How is your data shared?

Presence Publisher does not share any personal information with any third party.
Especially, it will never upload information about your location to any server other
than the MQTT broker that you have configured.
