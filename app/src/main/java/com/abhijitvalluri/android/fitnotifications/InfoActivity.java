package com.abhijitvalluri.android.fitnotifications;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

public class InfoActivity extends AppCompatActivity {

    private static final String EXTRA_ACTIVITY_TITLE =
            "com.abhijitvalluri.android.fitnotifications.infoActivityTitle";
    private static final String EXTRA_WEBVIEW_HTML =
            "com.abhijitvalluri.android.fitnotifications.webViewText";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        String activityTitle = getIntent().getStringExtra(EXTRA_ACTIVITY_TITLE);
        String webViewHtml = getIntent().getStringExtra(EXTRA_WEBVIEW_HTML);
        WebView webView = (WebView) findViewById(R.id.infoActivityWV);

        setTitle(activityTitle);
        webView.loadDataWithBaseURL(null, webViewHtml, "text/html", "utf-8", null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.transition.right_in, R.transition.right_out);
    }

    public static Intent newIntent(Context packageContext, String activityTitle, String webViewHtml) {
        Intent intent = new Intent(packageContext, InfoActivity.class);
        intent.putExtra(EXTRA_ACTIVITY_TITLE, activityTitle);
        intent.putExtra(EXTRA_WEBVIEW_HTML, webViewHtml);
        return intent;
    }
}
