/*
 * The basis of DriSMo was developed as a bachelor project in 2011,
 * by three students at Gjøvik University College (Fredrik Kvitvik,
 * Fredrik Hørtvedt and Jørn André Myrland). For documentation on DriSMo,
 * view the JavaDoc provided with the source code.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.drismo.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.drismo.R;
import com.drismo.gui.monitor.*;
import com.drismo.model.Config;
import com.drismo.logic.sms.MyPhoneStateListener;

import java.util.List;
import java.util.Locale;

/**
 * This is the application main activity.
 * In this class we set up the main navigation, loading preferences and register listeners.
 */
public class MainActivity extends Activity {
    SharedPreferences prefs;

    /**
     * Load <code>SharedPreferences</code>
     */
    public void getSetSharedPrefs() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
                                                                // Check if GPS is present in the device hardware.
        List<String> allProviders = ((LocationManager)getSystemService(Context.LOCATION_SERVICE)).getAllProviders();
        for(String provider : allProviders) if(provider.equals("gps")) Config.setGpsExists(true);

        Config.initializePreferences(getBaseContext(), prefs);
    }

    /**
     * Constructing main layout with onClick navigation, and font from assets.
     */
    private void initializeMain() {
        setContentView(R.layout.main);
        (findViewById(R.id.focusDummy)).requestFocus();          // Dummy to remove focus from menu item

        final Animation selectAnimation = AnimationUtils.loadAnimation(this, R.anim.button_select);

                                                                 // Creating font for main navigation TextViews.
        Typeface font = Typeface.createFromAsset(getAssets(), "eras-bold.ttf");

        if(getResources().getConfiguration().orientation == 2) { // Horizontal
            setContentView(R.layout.main_landscape);
        }

                                                                // MONITOR
        final FrameLayout startMonitor = (FrameLayout)this.findViewById(R.id.monitorFrame);
        final TextView monitorTxt = ((TextView)findViewById(R.id.monitorTxt));

        View.OnClickListener monitorClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                startMonitor.requestFocus();
                startMonitor.startAnimation(selectAnimation);
                Intent monitorIntent = MonitorFactory.getMonitorActivityIntent(MainActivity.this, Config.getPrefMonitor());
                startActivity(monitorIntent);
            }
        };
        View.OnFocusChangeListener monitorFocusListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    monitorTxt.setTextColor(Color.LTGRAY);
                }
                else monitorTxt.setTextColor(Color.WHITE);
            }
        };
        View.OnTouchListener monitorTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    startMonitor.requestFocus();
                    startMonitor.startAnimation(selectAnimation);
                    Intent monitorIntent = MonitorFactory.getMonitorActivityIntent(MainActivity.this, Config.getPrefMonitor());
                    startActivity(monitorIntent);
                }
                return true;
            }
        };

        monitorTxt.setTypeface(font);
        monitorTxt.setOnFocusChangeListener(monitorFocusListener);
        monitorTxt.setOnClickListener(monitorClickListener);
        startMonitor.setOnTouchListener(monitorTouchListener);

                                                                // ARCHIVE
        final FrameLayout viewArchive = (FrameLayout)this.findViewById(R.id.archiveFrame);
        final TextView archiveTxt = ((TextView)findViewById(R.id.archiveTxt));

        View.OnClickListener archiveClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                viewArchive.requestFocus();
                viewArchive.startAnimation(selectAnimation) ;
                Intent myIntent = new Intent(MainActivity.this, ViewArchive.class);
                startActivity(myIntent);
            }
        };
        View.OnFocusChangeListener archiveFocusListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    archiveTxt.setTextColor(Color.LTGRAY);
                }
                else archiveTxt.setTextColor(Color.WHITE);
            }
        };
        View.OnTouchListener archiveTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    viewArchive.requestFocus();
                    viewArchive.startAnimation(selectAnimation) ;
                    Intent myIntent = new Intent(MainActivity.this, ViewArchive.class);
                    startActivity(myIntent);
                }
                return true;
            }
        };

        archiveTxt.setTypeface(font);
        archiveTxt.setOnFocusChangeListener(archiveFocusListener);
        archiveTxt.setOnClickListener(archiveClickListener);
        viewArchive.setOnTouchListener(archiveTouchListener);
                                                                // SETTINGS
        final FrameLayout settings = (FrameLayout)this.findViewById(R.id.settingsFrame);
        final TextView settingsTxt = ((TextView) findViewById(R.id.settingsTxt));

        View.OnClickListener settingsClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                settings.requestFocus();
                settings.startAnimation(selectAnimation);
                Intent myIntent = new Intent(MainActivity.this, Preferences.class);
                startActivity(myIntent);
            }
        };
        View.OnFocusChangeListener settingsFocusListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    settingsTxt.setTextColor(Color.LTGRAY);
                }
                else settingsTxt.setTextColor(Color.WHITE);
            }
        };
        View.OnTouchListener settingsTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    settings.requestFocus();
                    settings.startAnimation(selectAnimation);
                    Intent myIntent = new Intent(MainActivity.this, Preferences.class);
                    startActivity(myIntent);
                }
                return true;
            }
        };

        settingsTxt.setTypeface(font);
        settingsTxt.setOnClickListener(settingsClickListener);
        settingsTxt.setOnFocusChangeListener(settingsFocusListener);
        settings.setOnTouchListener(settingsTouchListener);

                                                                // TUTORIAL
        final FrameLayout info = (FrameLayout)this.findViewById(R.id.infoFrame);
        final TextView infoTxt = ((TextView)findViewById(R.id.infoTxt));

        View.OnClickListener infoClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                info.requestFocus();
                info.startAnimation(selectAnimation);
                Intent myIntent = new Intent(MainActivity.this, Tutorial.class);
                startActivity(myIntent);
            }
        };
        View.OnFocusChangeListener infoFocusListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    infoTxt.setTextColor(Color.LTGRAY);
                }
                else infoTxt.setTextColor(Color.WHITE);
            }
        };
        View.OnTouchListener infoTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    info.requestFocus();
                    info.startAnimation(selectAnimation);
                    Intent myIntent = new Intent(MainActivity.this, Tutorial.class);
                    startActivity(myIntent);
                }
                return true;
            }
        };

        infoTxt.setTypeface(font);
        infoTxt.setOnClickListener(infoClickListener);
        infoTxt.setOnFocusChangeListener(infoFocusListener);
        info.setOnTouchListener(infoTouchListener);
    }

    /**
     * Set up the main navigation, load preferences and register listeners.
     * @param savedInstanceState Not beeing used, as <code>onSaveInstanceState(Bundle)</code> is not implemented.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSetSharedPrefs();     // Initializing preferences
                                 // Set custom locale from preferences
        try {
            Config.setConfigLocale(getBaseContext(), Config.getLanguageCode());
        } catch(Exception e){}

        firstRunPreferences();

        if(getFirstRun()) {      // Force tutorial on first run
            startActivity(new Intent(MainActivity.this, Tutorial.class));
            setRunned();
        }

        initializeMain();        // Constructing main layout with onClickListeners
    }

    /**
     * If the language has been changed while the activity was on pause,
     * the layout will be reconstructed on resume.
     */
    @Override
    public void onResume() {
        super.onResume();

        if(!Config.getLanguageCode().equals(Locale.getDefault().getLanguage())) {
            Config.setLangCode(Locale.getDefault().getLanguage());
            initializeMain();
        }
    }

    /**
     * Get if this is the first run
     * @return True if this is the first run. False otherwise.
     */
    public boolean getFirstRun() {
        return prefs.getBoolean("firstRun", true);
    }

    /**
    * store the first run
    */
    public void setRunned() {
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("firstRun", false);
        edit.commit();
    }

    /**
    * setting up preferences storage
    */
    public void firstRunPreferences() {
        Context mContext = this.getApplicationContext();
        prefs = mContext.getSharedPreferences("myAppPrefs", 0); //0 = mode private. only this app can read these preferences
    }

}


