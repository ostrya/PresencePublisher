# Privacy policy

*Effective December 31, 2021*

The following document describes how Presence Publisher collects and uses your personal
data.

## Which data is collected?

Presence Publisher regularly collects information about your device location in the background,
even if it is not running. The following information is collected:

* Whether the currently connected Wi-Fi network name matches a list of network
  names that you have configured.
* Whether any Bluetooth beacon that you have configured is in range of your
  Android device.
* If you explicitly choose to send this information, your last known location
  and currently connected Wi-Fi network name is collected.

Presence Publisher does not store your GPS location. It also does not store
any information about Wi-Fi networks and Bluetooth beacons which are not explicitly
configured by you.

Once configured, Presence Publisher collects this information repeatedly with a
configurable time period, but at least every 60 minutes. This collection happens
even when the app is not running.

Presence Publisher does not collect any data that would allow correlating the processed data
as mentioned above to you as the user of the app.

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

Presence Publisher only uses this information to send corresponding messages to your
MQTT broker.

In addition to these messages which are sent depending on your location, you can also choose
to send direct location information such as your current Wi-Fi name and your GPS location.
Note that this information is only collected to be sent in these MQTT messages. It is not
otherwise processed, stored or passed on.

For debugging purposes, Presence Publisher stores an internal log. This log includes:

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

Presence Publisher will never share any personal information with any third party.
Especially, it will never upload information about your device location to any server other
than the MQTT broker that you have configured.

## Changes to the privacy policy

This privacy policy may be updated from time to time, e.g. when new features are added to
the app. The new policy will become effective immediately after its publication.

## Your rights and choices

Subject to applicable law, you have the right to request and receive all personal data
processed by this app. In addition, you have the right to update and correct as well as
delete this data. Furthermore, you have the right to withdraw your consent for processing
the data either by disabling the corresponding checkbox in the app settings or by uninstalling
the app. Please note that the app will not be able to offer its full functionality in this case.

To exercise your rights and choices, please use the contact method below.

## Any more questions?

If you have any questions on how your data is processed or if you want to exercise your rights
and choices regarding the processed data, please contact me (the author of the app) at
`ostrya@mailbox.org`.

Messages and contact data of such requests will only be stored for the purpose of processing them
and will be deleted 30 days after the communication ends or at latest after 6 months.
