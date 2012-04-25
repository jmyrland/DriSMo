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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.*;
import android.preference.Preference.OnPreferenceClickListener;
import com.drismo.R;
import com.drismo.model.Config;
import com.drismo.logic.sms.MessageHandler;

/**
 * Set up the preferences, and handle changes.
 */
public class Preferences extends PreferenceActivity
                         implements SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * Load the current preferences, and disable some preference items accordingly.
     * @param savedInstanceState Not beeing used, as <code>onSaveInstanceState(Bundle)</code> is not implemented.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Config.setConfigLocale(getBaseContext(), Config.getLanguageCode());
        } catch(Exception e){}
        this.setTitle(getString(R.string.prefs));
/*
        getListView().setBackgroundColor(Color.TRANSPARENT);        // Transparent background
        getListView().setCacheColorHint(Color.TRANSPARENT);          */

        addPreferencesFromResource(R.xml.preferences);              // Loading preferences setup

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

        CheckBoxPreference replySMS = (CheckBoxPreference) findPreference("replySMS");
        CheckBoxPreference replyCall = (CheckBoxPreference) findPreference("replyCall");
        EditTextPreference replyMsg = (EditTextPreference) findPreference("customReplyMsg");

        if(!replySMS.isChecked() && !replyCall.isChecked()) deactivatePref(replyMsg);
        else activatePref(replyMsg);

        CheckBoxPreference logGps = (CheckBoxPreference) findPreference("logGps");
        if(!Config.deviceHasGps()) {
            logGps.setChecked(false);               // No need to let the user set this,
            deactivatePref(logGps);                 //  if GPS is not present on the device.
        }
        else {
            activatePref(logGps);
        }

//        findPreference("languageSelection").setSummary(Config.getLangSummaryText());

        // Popup modal window to display information about this feature.
        Preference autoReplyInfo = findPreference("autoReplyInfo");
        autoReplyInfo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent myIntent = new Intent(Preferences.this, InfoPopup.class);
                myIntent.putExtra("infoPopupContent", getString(R.string.autoReplyInfoTxt));
                myIntent.putExtra("infoPopupHeader", getString(R.string.whatsThis));
                startActivity(myIntent);
                return true;
            }
        });

        // Popup modal window to display information about the application.
        Preference prefAbout = findPreference("prefAbout");
        prefAbout.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent myIntent = new Intent(Preferences.this, AboutPopup.class);
                startActivity(myIntent);
                return true;
            }
        });
    }

    /**
     * When preferences are changed, store these in the config.
     * @param sp The preferences object.
     * @param key The preference ID, assigned in xml/preferences.xml.
     * @see com.drismo.model.Config
     */
    public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        Preference pref;

        if (key.equals("customReplyMsg")) {
            String value = sp.getString(key, null);
            Config.setAutoReplyMsg(value);
            MessageHandler.clearNumbers();                // Clear list of stored phone numbers, because
                                                          //  none of them has already received this new message.
            if(value.length() > 160) {
                EditTextPreference textbox = (EditTextPreference) findPreference("customReplyMsg");
                int oldLength = value.length();
                textbox.setText(value.substring(0, 160)); // If message is > 160 characters, crop the message
                                                          //  and alert the user that this is done.
                String alertMsg = getString(R.string.maxLength)+" 160\n"
                                + getString(R.string.yourLength)+" "+oldLength+"\n\n"
                                + getString(R.string.croppedMsg)+"\n"+'"'+textbox.getText()+'"';
                alertBox(alertMsg, getString(R.string.tooLong));
            }
        }
        if (key.equals("replySMS")) {
            Config.setAutoReplySms(sp.getBoolean(key, false));
        }
        if (key.equals("replyCall")) {
            Config.setAutoReplyCall(sp.getBoolean(key, false));
        }
        if (key.equals("logGps")) {
            Config.setLogGps(sp.getBoolean(key, true));
        }
        if (key.equals("monitorChoice")) {
            Config.setPrefMonitor(Integer.parseInt(sp.getString(key, "2").trim()));
        }
        if (key.equals("languageSelection")) {
            setLangConfig(sp.getString(key, "en"));
            finish();
        }
        if (key.equals("speedUnitSelection")) {
            Config.setSpeedConv(sp.getString(key, "km/h"));
            Config.setSpeedUnit(sp.getString(key, "km/h"));
        }
        if (key.equals("powerSaver")) {
            Config.setPowerSaver(sp.getBoolean(key, false));
        }
        if (key.equals("dimScreen")) {
            Config.setDimScreen(sp.getBoolean(key, false));
        }
        if (key.equals("useTts")) {
            Config.setUseTts(sp.getBoolean(key, true));
        }

        pref = findPreference("customReplyMsg");
        if (!Config.autoReplyCall() && !Config.autoReplySms()) {
               deactivatePref(pref);
        } else activatePref(pref);

    }

    /**
     * Deactivate preference
     * @param p The preference element to deactivate
     */
    public void deactivatePref(Preference p) {
         p.setEnabled(false);
    }

    /**
     * Activate preference
     * @param p The preference element to activate
     */
    public void activatePref(Preference p) {
         p.setEnabled(true);
    }

    /**
     * Changing default <code>Locale</code>, to activate selected language in the application.
     * @param lang Abbreviation for the language. (i.e. 'en' for english or 'no' for norwegian.)
     */
    public void setLangConfig(String lang){
        Config.setConfigLocale(getBaseContext(), lang);
    }

    /**
     * Create <code>AlertDialog</code> for user information.
     * @param msg Message to display to the user.
     * @param title Heading for the alertbox.
     */
    public void alertBox(String msg, String title){
       new AlertDialog.Builder(this)
          .setMessage(msg)
          .setTitle(title)
          .setCancelable(true)
          .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton){}})
          .show();
    }
}