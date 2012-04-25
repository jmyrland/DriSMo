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

package com.drismo.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import com.drismo.R;

import java.util.Locale;

/**
 * This class keeps track of all the preferences for the application.
 */
public class Config {
    private static boolean replySMS;              // Set static preference-vars,
    private static boolean replyCall;             //  for easy access from the Preference class.
    private static boolean logGps;
    private static boolean powerSaver;
    private static boolean dimScreen;
    private static boolean gpsExists;
    private static boolean useTts;
    private static int prefMonitor;
    private static String replyMsg;
    private static String languageCode;
    private static String currentLangTitle;
    private static String speedUnit;
    private static float speedConv;
    private static int ttsFrequency;

    /**
     * Check whether auto reply on is activated for incoming messages or not.
     * @return True if the setting activated. False otherwise.
     */
    public static boolean autoReplySms(){
        return replySMS;
    }

    /**
     * Check whether auto reply on is activated for incoming calls or not.
     * @return True if the setting activated. False otherwise.
     */
    public static boolean autoReplyCall(){
        return replyCall;
    }

    /**
     * Check whether GPS-logging is activated or not.
     * @return True if GPS-logging is enabled. False otherwise.
     */
    public static boolean logGps(){
        return logGps;
    }

    /**
     * Check whether the power saver is activated or not.
     * @return True if power saving is activated. False otherwise.
     */
    public static boolean powerSaverOn(){
        return powerSaver;
    }

    /**
     * Gets the message that will be sent as auto-reply to incoming requests.
     * @return The message string
     */
    public static String getAutoReplyMsg(){
        return replyMsg;
    }

    /**
     * Get the description text for the language preference.
     * @return The description
     */
    public static String getLangSummaryText() {
        return currentLangTitle;
    }

    /**
     * Get the language code to set as locale.
     * @return The language code.
     */
    public static String getLanguageCode(){
        return languageCode;
    }

    /**
     * Get the preferred monitor style.
     * <br /> 1 = Primitive
     * <br /> 2 = Wheel
     * <br /> 3 = Map
     * @return  Integer value for preferred monitor style.
     */
    public static int getPrefMonitor() {
        return prefMonitor;
    }

    /**
     * Get the frequency for the TTS feature.
     * @return The frequency in minutes.
     */
    public static int getTtsMinuteFrequency(){
        return ttsFrequency;
    }

    /**
     * Check whether the dim screen setting is enabled or not.
     * @return True if the setting is enabled. False otherwise.
     */
    public static boolean getDimScreen(){
        return dimScreen;
    }

    /**
     * Check if the TTS feature is enabled.
     * @return True if it is enabled. False otherwise.
     */
    public static boolean useTts() {
        return useTts;
    }

    /**
     * Check if a GPS is present in the device.
     * @return True if the device has GPS. False otherwise.
     */
    public static boolean deviceHasGps(){
        return gpsExists;
    }

    /**
     * Gets the speed conversion constant. (km/h or mph)
     * @return if km/h return 3.6f, else mph return 2.24f
     */
    public static float getSpeedConv() {
        return speedConv;
    }

    /**
     * Get the preferred speed unit.
     * @return String with the short name for the speed unit. (km/t, mph or m/s)
     */
    public static String getSpeedUnit() {
        return speedUnit;
    }

    /**
     * Sets the boolean that decides whether or not to send auto-reply message on incoming messages.
     * @param reply True if the auto-reply setting is activated for messages. False otherwise.
     */
    public static void setAutoReplySms(boolean reply){
        replySMS = reply;
    }

    /**
     * Sets the boolean that decides whether or not to send auto-reply message on incoming calls.
     * @param reply True if the auto-reply setting is activated for calls. False otherwise.
     */
    public static void setAutoReplyCall(boolean reply){
        replyCall = reply;
    }

    /**
     * Set language code.
     * @param lang The language code
     */
    public static void setLangCode(String lang){
        languageCode = lang;
    }

    /**
     * Set the description text for the language preference.
     * @param lang The description
     */
    public static void setLangSummaryText(String lang) {
        currentLangTitle = lang;
    }

    /**
     * Sets the message that will be sent as auto-reply to incoming requests.
     * @param msg The message
     */
    public static void setAutoReplyMsg(String msg){
        replyMsg = msg;
    }

    /**
     * Enable/Disable GPS-logging.
     * @param log True if GPS should be logged. False otherwise.
     */
    public static void setLogGps(boolean log){
        logGps = log;
    }

    /**
     * Enable/Disable power saver.
     * @param ps True if power saver should be enabled. False otherwise.
     */
    public static void setPowerSaver(boolean ps){
        powerSaver = ps;
    }

    /**
     * Setting what monitor is default.
     * <br /> 1 = Primitive
     * <br /> 2 = Wheel
     * <br /> 3 = Map
     * @param monitor Integer value of monitor to set as default.
     */
    public static void setPrefMonitor(int monitor){
        prefMonitor = monitor;
    }

    /**
     * Set the boolean for the dim screen setting.
     * @param dim True if dim screen is allowed. False otherwise.
     */
    public static void setDimScreen(boolean dim) {
        dimScreen = dim;
    }

    /**
     * Set the boolean for the TTS setting.
     * @param tts True if the setting has been enabled. False otherwise.
     */
    public static void setUseTts(boolean tts) {
        useTts = tts;
    }

    /**
     * Set how often to give feedback via TTS.
     * @param freq Frequency given in minutes.
     */
    public static void setTtsFrequency(int freq) {
        ttsFrequency = freq;
    }

    /**
     * Store information about whether or not the device has GPS.
     * @param gps True if the device has GPS. False otherwise.
     */
    public static void setGpsExists(boolean gps) {
        gpsExists = gps;
    }

    /**
     * Sets the speed conversion constant
     * @param unit if km/h set 3.6f, else mph set 2.24f
     */
    public static void setSpeedConv(String unit) {
        if(unit.equals("km/h"))
            speedConv = 3.6f;
        else if(unit.equals("mph"))
            speedConv = 2.2369f;
        else
            speedConv = 1;
    }

    /**
     * Set preferred speed measurement unit.
     * @param unit The short name for the preferred speed unit. (km/t, mph or m/s)
     */
    public static void setSpeedUnit(String unit) {
        speedUnit = unit;
    }

    /**
     * Set all preferences. Call this function to set up preferences on application startup.
     * @param c The context
     * @param prefs The SharedPreferences object.
     */
    public static void initializePreferences(Context c, SharedPreferences prefs) {
        setLogGps(prefs.getBoolean("logGps", true));
        setAutoReplySms(prefs.getBoolean("replySMS", false));
        setAutoReplyCall(prefs.getBoolean("replyCall", false));
        setPowerSaver(prefs.getBoolean("powerSaver", false));
        setDimScreen(prefs.getBoolean("dimScreen", false));
        setUseTts(prefs.getBoolean("useTts", true));
        setTtsFrequency(Integer.parseInt(prefs.getString("TTSFrequency", "5")));
        setLangCode(prefs.getString("languageSelection", "en"));
        setAutoReplyMsg(prefs.getString("customReplyMsg", c.getString(R.string.autoReplyMessage)));
        setPrefMonitor(Integer.parseInt(prefs.getString("monitorChoice", "2").trim()));
        setSpeedConv(prefs.getString("speedUnitSelection", "km/h"));
        setSpeedUnit(prefs.getString("speedUnitSelection", "km/h"));
    }

    /**
     * Changing default <code>Locale</code>, to activate selected language in the application.
     * @param c the context
     * @param lang Abbreviation for the language. (i.e. 'en' for english or 'no' for norwegian.)
     */
    public static void setConfigLocale(Context c, String lang){
        Configuration config = new Configuration();
        Locale loc = new Locale(lang, Locale.getDefault().getCountry());  // Using country code from phone settings.
        Locale.setDefault(loc); config.locale = loc;                      // Setting new (custom) locale.
        c.getResources().updateConfiguration(config, null);

        // setLangSummaryText(loc.getDisplayLanguage());
    }
}
