package com.dweenmd.womensafety.ui.profile;

import android.os.Bundle;
import android.widget.TextView;


import com.dweenmd.womensafety.R;
import com.dweenmd.womensafety.ui.BaseActivity;

public class LoginActivityHistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_history);

        setupToolbar(R.id.toolbar_login_history);

        TextView deviceName = findViewById(R.id.tv_current_device_name);
        deviceName.setText("Android Device (" + android.os.Build.MODEL + ")");
    }
}
