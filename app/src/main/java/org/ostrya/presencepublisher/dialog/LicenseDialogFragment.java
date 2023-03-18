package org.ostrya.presencepublisher.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import org.ostrya.presencepublisher.R;

public class LicenseDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Activity parent = requireActivity();
        WebView webView = new WebView(parent);
        webView.loadUrl("file:///android_asset/open_source_licenses.html");
        AlertDialog.Builder builder = new AlertDialog.Builder(parent);
        builder.setTitle(R.string.bundled_licenses_title);
        builder.setView(webView);
        builder.setNeutralButton(R.string.dialog_close, null);
        return builder.create();
    }
}
