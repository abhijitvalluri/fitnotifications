package com.abhijitvalluri.android.fitnotifications;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class InfoFragment extends Fragment {

    private static final String WEBVIEW_HTML =
            "com.abhijitvalluri.android.fitnotifications.webViewText";
    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_info, container, false);

        String webViewHtml = getArguments().getString(WEBVIEW_HTML);
        mWebView = (WebView) v.findViewById(R.id.infoActivityWV);

        mWebView.loadDataWithBaseURL(null, webViewHtml, "text/html", "utf-8", null);

        return v;
    }

    public void updateWebViewContent(String webViewHtml) {
        mWebView.loadDataWithBaseURL(null, webViewHtml, "text/html", "utf-8", null);
    }

    public static Fragment newInstance(String webViewHtml) {
        Fragment frag = new InfoFragment();
        Bundle b = new Bundle();
        b.putString(WEBVIEW_HTML, webViewHtml);
        frag.setArguments(b);
        return frag;
    }
}
