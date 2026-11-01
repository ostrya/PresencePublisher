package org.ostrya.presencepublisher.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import org.json.JSONObject;
import org.ostrya.presencepublisher.R;
import org.ostrya.presencepublisher.log.DatabaseLogger;
import org.ostrya.presencepublisher.preference.about.BackupPreferenceDummy;
import org.ostrya.presencepublisher.preference.about.BundledLicensesPreferenceDummy;
import org.ostrya.presencepublisher.preference.about.LocationConsentPreference;
import org.ostrya.presencepublisher.preference.about.NightModePreference;
import org.ostrya.presencepublisher.preference.about.PrivacyPreferenceDummy;
import org.ostrya.presencepublisher.preference.about.RestorePreferenceDummy;
import org.ostrya.presencepublisher.preference.about.SignaturePreferenceDummy;
import org.ostrya.presencepublisher.preference.about.SourceRepositoryPreferenceDummy;
import org.ostrya.presencepublisher.preference.about.VersionInfoPreferenceDummy;
import org.ostrya.presencepublisher.preference.backup.BackupService;
import org.ostrya.presencepublisher.preference.common.AbstractConfigurationFragment;
import org.ostrya.presencepublisher.preference.common.MyPreferenceCategory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AboutFragment extends AbstractConfigurationFragment {
    private static final String TAG = "AboutFragment";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ActivityResultLauncher<String> backupLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument("application/json"),
                    this::onBackupSelected);
    private final ActivityResultLauncher<String[]> restoreLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(), this::onRestoreSelected);

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        Context context = getPreferenceManager().getContext();

        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        PreferenceCategory legalCategory =
                new MyPreferenceCategory(context, R.string.category_legal);
        PreferenceCategory backupCategory =
                new MyPreferenceCategory(context, R.string.category_backup);
        PreferenceCategory uiCategory = new MyPreferenceCategory(context, R.string.category_ui);
        PreferenceCategory appCategory = new MyPreferenceCategory(context, R.string.category_build);

        screen.addPreference(legalCategory);
        screen.addPreference(backupCategory);
        screen.addPreference(uiCategory);
        screen.addPreference(appCategory);

        legalCategory.addPreference(new SourceRepositoryPreferenceDummy(context, this));
        legalCategory.addPreference(new BundledLicensesPreferenceDummy(context, this));
        legalCategory.addPreference(new PrivacyPreferenceDummy(context, this));
        legalCategory.addPreference(new LocationConsentPreference(context));

        backupCategory.addPreference(new BackupPreferenceDummy(context, backupLauncher, this));
        backupCategory.addPreference(new RestorePreferenceDummy(context, restoreLauncher, this));

        uiCategory.addPreference(new NightModePreference(context));

        appCategory.addPreference(new VersionInfoPreferenceDummy(context));
        appCategory.addPreference(new SignaturePreferenceDummy(context));

        setPreferenceScreen(screen);
    }

    @Override
    protected void onPreferencesChanged(SharedPreferences preferences, String name) {
        if (NightModePreference.NIGHT_MODE.equals(name)) {
            NightModePreference.updateCurrentNightMode(preferences);
        }
    }

    private void onBackupSelected(Uri targetUri) {
        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        Context parent = getActivity();
        if (isAdded() && parent != null) {
            executor.execute(
                    () -> {
                        BackupService backupService = new BackupService(preferences);
                        String message;
                        try (BufferedWriter out =
                                new BufferedWriter(
                                        new OutputStreamWriter(
                                                parent.getContentResolver()
                                                        .openOutputStream(targetUri, "wt"),
                                                StandardCharsets.UTF_8))) {
                            JSONObject jsonObject = backupService.serialize();
                            out.append(jsonObject.toString());
                            message = parent.getString(R.string.backup_success);
                        } catch (Exception e) {
                            DatabaseLogger.w(TAG, "Unable to export settings", e);
                            message =
                                    parent.getString(
                                            R.string.backup_failure, e.getLocalizedMessage());
                        }
                        final String result = message;
                        mainHandler.post(
                                () -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                                    builder.setTitle(R.string.backup_title);
                                    builder.setMessage(result);
                                    builder.setNeutralButton(R.string.dialog_close, null);
                                    builder.create().show();
                                });
                    });
        }
    }

    private void onRestoreSelected(Uri sourceUri) {
        SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
        Context parent = getActivity();
        if (isAdded() && parent != null) {
            executor.execute(
                    () -> {
                        BackupService backupService = new BackupService(preferences);
                        String message;
                        try (BufferedReader reader =
                                new BufferedReader(
                                        new InputStreamReader(
                                                parent.getContentResolver()
                                                        .openInputStream(sourceUri),
                                                StandardCharsets.UTF_8))) {
                            JSONObject jsonObject =
                                    new JSONObject(reader.lines().collect(Collectors.joining()));
                            backupService.deserialize(jsonObject);
                            message = parent.getString(R.string.restore_success);
                        } catch (Exception e) {
                            DatabaseLogger.w(TAG, "Unable to import settings", e);
                            message =
                                    parent.getString(
                                            R.string.restore_failure, e.getLocalizedMessage());
                        }
                        final String result = message;
                        mainHandler.post(
                                () -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                                    builder.setTitle(R.string.backup_title);
                                    builder.setMessage(result);
                                    builder.setNeutralButton(R.string.dialog_close, null);
                                    builder.create().show();
                                });
                    });
        }
    }
}
