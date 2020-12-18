/*
   Copyright 2017 Abhijit Kiran Valluri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.abhijitvalluri.android.fitnotifications.home;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.abhijitvalluri.android.fitnotifications.R;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

/**
 * Fragment that holds the information for the various pages navigated to from the side drawer
 */
public class InfoFragment extends Fragment {

    private static final String WEBVIEW_HTML =
            "com.abhijitvalluri.android.fitnotifications.webViewText";
    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_info, container, false);

        mWebView = (WebView) v.findViewById(R.id.infoActivityWV);

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(mWebView.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                }
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(mWebView.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                }
                break;
        }

        if (getArguments() != null) {
            String webViewHtml = getArguments().getString(WEBVIEW_HTML);
            if (webViewHtml != null) {
                mWebView.loadDataWithBaseURL(null, getHtmlWithThemeStyling(webViewHtml), "text/html", "utf-8", null);
            }
        }

        return v;
    }

    public void updateWebViewContent(@NonNull String webViewHtml) {
        mWebView.loadDataWithBaseURL(null, getHtmlWithThemeStyling(webViewHtml), "text/html", "utf-8", null);
    }

    private String getHtmlWithThemeStyling(@NonNull String webViewHtml) {
        String textColor = "#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.normal_text)).substring(2);
        String backgroundColor = "#" + Integer.toHexString(ContextCompat.getColor(getContext(), R.color.normal_background)).substring(2);

        return "<html><head>"
                + "<style type=\"text/css\">body { color: " + textColor + "; background-color: " + backgroundColor + "; } "
                + "a:link { color: #42A5F5; }"
                + "</style></head>"
                + "<body>" + webViewHtml + "</body></html>";
    }

    public static Fragment newInstance(@NonNull String webViewHtml) {
        Fragment frag = new InfoFragment();
        Bundle b = new Bundle();
        b.putString(WEBVIEW_HTML, webViewHtml);
        frag.setArguments(b);
        return frag;
    }
}
