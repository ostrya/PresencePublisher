# Datenschutzerklärung

Im Folgenden wird beschrieben, wie Presence Publisher deine persönlichen Daten erhebt
und verarbeitet.

## Welche persönlichen Daten werden erhoben?

Presence Publisher erfasst regelmäßig im Hintergrund Informationen über deinen Standort,
selbst wenn es nicht ausgeführt wird. Folgende Informationen werden erhoben:

* Ob der Name des aktuell verbundenen WLAN-Netzwerks in einer Liste der von dir
  konfigurierten Netzwerknamen vorkommt.
* Ob eines der Bluetooth-Beacons, die du konfiguriert hast, sich in der Reichweite
  deines Android-Gerätes befindet.

Presence Publisher erfasst und verarbeitet niemals deinen GPS-Standort. Auch speichert es
keine Information über WLAN-Netzwerke und Bluetooth-Beacons die nicht vorher von dir
konfiguriert worden sind.

Sobald Presence Publisher eingerichtet ist, erhebt es die genannten Informationen
regelmäßig in einem konfigurierbaren Intervall, jedoch mindestens alle 60 Minuten.
Diese Erhebung erfolgt auch, wenn die App nicht ausgeführt wird.

## Wie werden deine Daten verarbeitet?

Presence Publisher nutzt die Standortinformationen, um Nachrichten zu versenden.
Diese Nachrichten werden an einen von dir vorgegebenen MQTT-Broker gesendet.

Folgende Nachrichten auf Basis deines Standorts können konfiguriert werden:

* Wenn der Name des aktuell verbundenen WLAN-Netzwerks einem von dir konfigurierten
  Namen entspricht, wird die für dieses Netzwerk konfigurierte Nachricht gesendet.
* Wenn ein von dir konfiguriertes Bluetooth-Beacon in Reichweite ist, wird die
  für dieses Beacon konfigurierte Nachricht gesendet.
* Wenn keine der obigen Bedingungen zutrifft und du für diesen Fall eine Benachrichtigung
  angefordert hast, wird die für diesen Fall konfigurierte Nachricht gesendet.

Darüber hinaus speichert Presence Publisher ein internes Log. Diese Log enthält:

* die von dir konfigurierten Bluetooth-Beacons und Netzwerknamen
* Zeiten, in denen du mit einem konfigurierten WLAN-Netzwerk verbunden bist, zusammen mit dessen Namen
* Zeiten, in denen du in Reichweite zu einem konfigurierten Bluetooth-Beacon bist, zusammen mit seinem Namen

Diese Logdaten können in Presence Publisher eingesehen werden. Sie sind jedoch nicht von
außen zugreifbar. Presence Publisher speichert die Logdaten für 7 Tage. Logeinträge, die älter
als 7 Tage sind, werden automatisch gelöscht.

Du hast die Möglichkeit, diese Logdaten in eine Datei zu exportieren. Dies führt dazu, dass alle
internen Logeinträge von Presence Publisher in den externen Speicher geschrieben werden.
Die so erzeugte Datei wird nicht mehr von Presence Publisher verändert, sobald sie geschrieben wurde.
Insbesondere werden keine neuen Logeinträge hinzugefügt. Die exportierte Logdatei kann von
anderen Apps gelesen werden und wird nicht automatisch gelöscht.

## Wie werden deine Daten weitergegeben?

Presence Publisher gibt keinerlei persönliche Daten an irgend eine dritte Partei weiter.
Insbesondere wird es niemals Informationen über deinen Standort an irgendeinen anderen
Server laden als den von dir konfigurierten MQTT-Broker.
