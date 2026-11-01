package org.ostrya.presencepublisher.preference.backup;

import static org.ostrya.presencepublisher.mqtt.context.condition.beacon.RegionMonitorNotifier.FOUND_BEACON_LIST;
import static org.ostrya.presencepublisher.preference.schedule.LastSuccessTimestampPreference.LAST_SUCCESS;
import static org.ostrya.presencepublisher.preference.schedule.NextScheduleTimestampPreference.NEXT_SCHEDULE;
import static org.ostrya.presencepublisher.schedule.Scheduler.NEXT_WORKER_ID;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ostrya.presencepublisher.BuildConfig;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class BackupService {
    private enum RootField {
        VERSION("version"),
        PREFERENCES("preferences");

        private final String name;

        RootField(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private enum EntryField {
        TYPE("type"),
        KEY("key"),
        VALUE("value");

        private final String name;

        EntryField(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private enum EntryType {
        BOOL("bool"),
        INT("int"),
        LONG("long"),
        FLOAT("float"),
        STRING("string"),
        STRING_SET("stringSet");

        private final String name;

        EntryType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final SharedPreferences preferences;

    public BackupService(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public JSONObject serialize() throws JSONException {
        JSONObject result = new JSONObject();
        result.put(RootField.VERSION.getName(), BuildConfig.VERSION_CODE);
        Map<String, ?> allPrefs = new HashMap<>(preferences.getAll());
        // don't save next worker ID, no user setting and will be regenerated
        allPrefs.remove(NEXT_WORKER_ID);
        // don't save last / next schedule, no user setting and will be re-generated anyway
        allPrefs.remove(LAST_SUCCESS);
        allPrefs.remove(NEXT_SCHEDULE);
        // don't save seen beacons, no user setting, out of date, and will be re-generated anyway
        allPrefs.remove(FOUND_BEACON_LIST);
        result.put(RootField.PREFERENCES.getName(), preferencesToArray(allPrefs));
        return result;
    }

    private JSONArray preferencesToArray(Map<String, ?> allPrefs) throws JSONException {
        JSONArray prefsArray = new JSONArray();
        for (Map.Entry<String, ?> it : allPrefs.entrySet()) {
            JSONObject entry = new JSONObject();
            entry.put(EntryField.KEY.getName(), it.getKey());
            if (it.getValue() instanceof Boolean) {
                entry.put(EntryField.TYPE.getName(), EntryType.BOOL.getName());
                entry.put(EntryField.VALUE.getName(), (boolean) it.getValue());
            } else if (it.getValue() instanceof Integer) {
                entry.put(EntryField.TYPE.getName(), EntryType.INT.getName());
                entry.put(EntryField.VALUE.getName(), (int) it.getValue());
            } else if (it.getValue() instanceof Long) {
                entry.put(EntryField.TYPE.getName(), EntryType.LONG.getName());
                entry.put(EntryField.VALUE.getName(), (long) it.getValue());
            } else if (it.getValue() instanceof Float) {
                entry.put(EntryField.TYPE.getName(), EntryType.FLOAT.getName());
                entry.put(EntryField.VALUE.getName(), (float) it.getValue());
            } else if (it.getValue() instanceof String) {
                entry.put(EntryField.TYPE.getName(), EntryType.STRING.getName());
                entry.put(EntryField.VALUE.getName(), (String) it.getValue());
            } else if (it.getValue() instanceof Set) {
                entry.put(EntryField.TYPE.getName(), EntryType.STRING_SET.getName());
                entry.put(EntryField.VALUE.getName(), new JSONArray((Set<?>) it.getValue()));
            } else {
                throw new JSONException(
                        "Unexpected type "
                                + it.getValue().getClass().getSimpleName()
                                + " encountered!");
            }
            prefsArray.put(entry);
        }
        return prefsArray;
    }

    public void deserialize(JSONObject backup) throws JSONException {
        // for now, no version check logic in place
        backup.getInt(RootField.VERSION.getName());
        JSONArray allPrefs = backup.getJSONArray(RootField.PREFERENCES.getName());
        SharedPreferences.Editor editor = preferences.edit();
        for (int i = 0; i < allPrefs.length(); ++i) {
            JSONObject entry = allPrefs.getJSONObject(i);
            String key = entry.getString(EntryField.KEY.getName());
            String type = entry.getString(EntryField.TYPE.getName());
            if (EntryType.BOOL.getName().equals(type)) {
                editor.putBoolean(key, entry.getBoolean(EntryField.VALUE.getName()));
            } else if (EntryType.INT.getName().equals(type)) {
                editor.putInt(key, entry.getInt(EntryField.VALUE.getName()));
            } else if (EntryType.LONG.getName().equals(type)) {
                editor.putLong(key, entry.getLong(EntryField.VALUE.getName()));
            } else if (EntryType.FLOAT.getName().equals(type)) {
                editor.putFloat(key, (float) entry.getDouble(EntryField.VALUE.getName()));
            } else if (EntryType.STRING.getName().equals(type)) {
                editor.putString(key, entry.getString(EntryField.VALUE.getName()));
            } else if (EntryType.STRING_SET.getName().equals(type)) {
                JSONArray list = entry.getJSONArray(EntryField.VALUE.getName());
                Set<String> set = new LinkedHashSet<>();
                for (int j = 0; j < list.length(); ++j) {
                    set.add(list.getString(j));
                }
                editor.putStringSet(key, set);
            }
        }
        editor.commit();
    }
}
