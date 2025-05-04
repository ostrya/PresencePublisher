package org.ostrya.presencepublisher.device;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DevicePreferenceTest {
    @Test
    public void cryptoWorks() {
        SharedPreferences delegate = new InMemoryPreferences();
        DevicePreferences uut = new DevicePreferences(delegate);

        uut.putString("key", "value");
        assertThat(uut.getString("key", "default")).isEqualTo("value");
        assertThat(delegate.getString("key", "default"))
                .isNotEqualTo("default")
                .isNotEqualTo("value");
    }

    private static class InMemoryPreferences implements SharedPreferences {
        private final Map<String, Object> entries = new HashMap<>();

        @Override
        public Map<String, ?> getAll() {
            return entries;
        }

        @Nullable
        @Override
        public String getString(String key, @Nullable String defValue) {
            return Optional.ofNullable(entries.getOrDefault(key, null))
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .orElse(defValue);
        }

        @Nullable
        @Override
        public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
            return Optional.ofNullable(entries.getOrDefault(key, null))
                    .filter(Set.class::isInstance)
                    .map(Set.class::cast)
                    .orElse(defValues);
        }

        @Override
        public int getInt(String key, int defValue) {
            return Optional.ofNullable(entries.getOrDefault(key, null))
                    .filter(Integer.class::isInstance)
                    .map(Integer.class::cast)
                    .orElse(defValue);
        }

        @Override
        public long getLong(String key, long defValue) {
            return Optional.ofNullable(entries.getOrDefault(key, null))
                    .filter(Long.class::isInstance)
                    .map(Long.class::cast)
                    .orElse(defValue);
        }

        @Override
        public float getFloat(String key, float defValue) {
            return Optional.ofNullable(entries.getOrDefault(key, null))
                    .filter(Float.class::isInstance)
                    .map(Float.class::cast)
                    .orElse(defValue);
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            return Optional.ofNullable(entries.getOrDefault(key, null))
                    .filter(Boolean.class::isInstance)
                    .map(Boolean.class::cast)
                    .orElse(defValue);
        }

        @Override
        public boolean contains(String key) {
            return entries.containsKey(key);
        }

        @Override
        public Editor edit() {
            return new Editor() {
                @Override
                public Editor putString(String key, @Nullable String value) {
                    entries.put(key, value);
                    return this;
                }

                @Override
                public Editor putStringSet(String key, @Nullable Set<String> values) {
                    entries.put(key, values);
                    return this;
                }

                @Override
                public Editor putInt(String key, int value) {
                    entries.put(key, value);
                    return this;
                }

                @Override
                public Editor putLong(String key, long value) {
                    entries.put(key, value);
                    return this;
                }

                @Override
                public Editor putFloat(String key, float value) {
                    entries.put(key, value);
                    return this;
                }

                @Override
                public Editor putBoolean(String key, boolean value) {
                    entries.put(key, value);
                    return this;
                }

                @Override
                public Editor remove(String key) {
                    entries.remove(key);
                    return this;
                }

                @Override
                public Editor clear() {
                    entries.clear();
                    return this;
                }

                @Override
                public boolean commit() {
                    return true;
                }

                @Override
                public void apply() {}
            };
        }

        @Override
        public void registerOnSharedPreferenceChangeListener(
                OnSharedPreferenceChangeListener listener) {}

        @Override
        public void unregisterOnSharedPreferenceChangeListener(
                OnSharedPreferenceChangeListener listener) {}
    }
}
