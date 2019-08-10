package org.ostrya.presencepublisher.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class LicenseFragment extends Fragment {

    public LicenseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        WebView webView = new WebView(requireActivity());
        webView.loadUrl("file:///android_asset/open_source_licenses.html");
        return webView;
    }
}
