package com.test.accessibilityservice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ACTIVITY";
    private String packageName = "com.google.android.youtube";
    private static final int REQ_CODE = 777;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setup();
            }
        });

    }

    private void setup() {
        if (isAccessibilitySettingsOn(this)) goYoutube();
        else startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), REQ_CODE);
    }

    private void goYoutube() {
        if (!isAppInstalled(packageName))
            Toast.makeText(this, "No Youtube app installed!", Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.youtube.com/"));
            intent.setPackage("com.google.android.youtube");
            startActivity(intent);
        }
    }


    public boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        //your package / accesibility service path/class
        final String service = "com.test.accessibilityservice/com.test.accessibilityservice.TestAccessibilityService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext())
                    if (mStringColonSplitter.next().equalsIgnoreCase(service)) return true;
            }
        }
        return accessibilityFound;
    }

    private boolean isAppInstalled(String packageName) {
        return getPackageManager().getLaunchIntentForPackage(packageName) != null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE) setup();
    }
}
