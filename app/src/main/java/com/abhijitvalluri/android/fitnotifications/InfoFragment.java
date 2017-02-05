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

package com.abhijitvalluri.android.fitnotifications;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

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
