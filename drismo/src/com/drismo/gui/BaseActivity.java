package com.drismo.gui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.drismo.model.Config;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Config.setConfigLocale(getBaseContext(), Config.getLanguageCode());
    }

    public TextView getTextView(int id) {
        return (TextView) findViewById(id);
    }
}
