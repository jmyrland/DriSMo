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

package com.drismo.logic;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.drismo.gui.monitor.MonitorActivityTemplate;
import android.hardware.SensorManager;
import com.drismo.logic.sms.MyPhoneStateListener;
import com.drismo.logic.sms.SMSReceiver;
import com.drismo.model.Config;
//import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;

/**
 * A singleton controller responsible of maintaining and controlling everything related to a monitor activity.
 * @see MonitorActivityTemplate
 * @see AccelerationHandler
 * @see LocationHandler
 * @see QualityRater
 * @see Calibration
 */
public class MonitorController implements CalibrationListener {

    private Context c;

    private static volatile MonitorController CONTROLLER = null;

    private PackageManager PM;
    private AudioManager AM;

    public static final int STATE_IDLE = 1;
    public static final int STATE_CALIBRATING = 2;
    public static final int STATE_MONITORING = 4;

    private int CURRENT_STATE;

    private int ringerVibrate = 0, notifyVibrate = 0;

    private Calibration calibrator;

    private AccelerationHandler accelerationHandler;
    private LocationHandler locationHandler;
    private QualityRater qualityRater;

    private MonitorActivityTemplate monitorActivity;
    private TripLogger tripLogger;

    private QualityToSpeech qtsEngine;

    /**
     * Constructor initiating all the necessary objects to control the actual monitor activity.
     * @see MonitorController#requestMonitorController(com.drismo.gui.monitor.MonitorActivityTemplate)
     * @param lm LocationManager provided by the monitor activity.
     * @param sm SensorManager provided by the monitor activity.
     * @param c Application context provided by the monitor activity.
     */
    private MonitorController(LocationManager lm, SensorManager sm, Context c){
//    private MonitorController(LocationManager lm, SensorManagerSimulator sm, Context c){

        this.c = c;

        CURRENT_STATE = STATE_IDLE;

//        sm.connectSimulator();
        accelerationHandler = new AccelerationHandler(sm);

        locationHandler = new LocationHandler(lm);

    }

    /**
     * Returns the monitor controller, and set the given monitor activity as the current.
     * @param a The monitor activity.
     * @return The monitor controller.
     */
    public static synchronized MonitorController requestMonitorController(MonitorActivityTemplate a){
        if(CONTROLLER == null){                     // If not created, create it
            CONTROLLER = new MonitorController(
                    (LocationManager) a.getSystemService(Context.LOCATION_SERVICE),
                    (SensorManager)   a.getSystemService(Context.SENSOR_SERVICE),
//                    SensorManagerSimulator.getSystemService(a, Context.SENSOR_SERVICE),
                    a.getApplicationContext()
            );
        }

        CONTROLLER.setMonitorActivity(a);          // Set the new monitor activity

        return CONTROLLER;                          // Return the controller
    }

    /**
     * Set the new monitor activity by purging the old and initiating the new.
     * @param a The give monitor activity.
     */
    private void setMonitorActivity(MonitorActivityTemplate a){
        purgeActivityListener();            // Purge the old activity listener.
        monitorActivity = a;                // Replace the new activity with the old and
        initiateMonitorActivity();         //   initiate it.
    }

    /**
     * Initiates the monitor activity by registering it as a listener in the required handlers.
     */
    private void initiateMonitorActivity(){
        if(CURRENT_STATE == STATE_MONITORING)
            qualityRater.registerQualityListener(monitorActivity);

        if(monitorActivity instanceof FilteredAccelerationListener)
            accelerationHandler.registerFilteredAccelerationListener((FilteredAccelerationListener) monitorActivity);

        if(monitorActivity instanceof NewLocationListener)
            locationHandler.registerNewLocationListener((NewLocationListener) monitorActivity);
    }

    /**
     * Purges the monitor activity by removing it in all the required handlers.
     */
    private void purgeActivityListener(){
        if(monitorActivity != null){

            if(CURRENT_STATE == STATE_MONITORING)
                qualityRater.unregisterQualityListener(monitorActivity);                 // Unregister old listener:

            if(monitorActivity instanceof FilteredAccelerationListener)
                accelerationHandler.unregisterFilteredAccelerationListener((FilteredAccelerationListener) monitorActivity);

            if(monitorActivity instanceof NewLocationListener)
                locationHandler.unregisterNewLocationListener((NewLocationListener) monitorActivity);
        }
    }

    /**
     * Initiates the calibration.
     */
    public void initiateCalibration() {
        if(CURRENT_STATE != STATE_CALIBRATING && CURRENT_STATE != STATE_MONITORING){
            CURRENT_STATE = STATE_CALIBRATING;

            if(Config.useTts()){
                qtsEngine = new QualityToSpeech( monitorActivity.getApplicationContext() );
            }

            calibrator = new Calibration(accelerationHandler, this);
        }
    }

    /**
     * Cancels the calibration, if calibrating.
     */
    public void cancelCalibration(){
        if(CURRENT_STATE == STATE_CALIBRATING){
            calibrator.cancel();
            CURRENT_STATE = STATE_IDLE;

            if(Config.useTts()){
                qtsEngine.speak("Calibration canceled");
                qtsEngine.shutdown();
            }
        }
    }

    public void onOffsetCalculationComplete() {
        monitorActivity.onOffsetCalculationComplete();
        if(Config.useTts()){
            qtsEngine.speak("Please drive forward.");
        }
    }

    public void onCalibrationCompleted() {
        monitorActivity.onCalibrationCompleted();
    }


    /**
     * Start monitoring.
     */
    public synchronized void startMonitoring() {
        tripLogger   = new TripLogger( monitorActivity.getApplicationContext() );
        qualityRater = new QualityRater();

        CURRENT_STATE = STATE_MONITORING;

        accelerationHandler.registerFilteredAccelerationListener(qualityRater);

        AM = (AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
                                                                                // Disable phone vibrator.
        if(AM.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION) != 0) {
            notifyVibrate = AM.getVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION);
            AM.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, AudioManager.VIBRATE_SETTING_OFF);
        }
        if(AM.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER) != 0) {
            ringerVibrate = AM.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
            AM.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_OFF);
        }

        setUpMessenger();

        initiateMonitorActivity();

        qualityRater.registerQualityListener(tripLogger);
        locationHandler.registerNewLocationListener(tripLogger);
        tripLogger.start();

        if(Config.useTts()){
            qtsEngine.speak("Device calibrated!");
            qualityRater.registerQualityListener(qtsEngine);
        }
    }

    /**
     * Sets up the on call/sms response, based on the preferences.
     */
    public void setUpMessenger(){
        if(Config.autoReplyCall()){                               // Register PhoneStateListener
            MyPhoneStateListener phoneListener = new MyPhoneStateListener();
            TelephonyManager telephonyManager  = (TelephonyManager)c.getSystemService(c.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        if(Config.autoReplySms()){                               // Activate SMSReceiver
            ComponentName component = new ComponentName(c, SMSReceiver.class);
            PM = c.getPackageManager();
            PM.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                                     PackageManager.DONT_KILL_APP);
        }
    }

    /**
     * Stop monitoring.
     */
    public synchronized void stopMonitoring() {
        if(CURRENT_STATE == STATE_MONITORING){
            tripLogger.interrupt();             // Stop logging and purge the monitor:
            purgeActivityListener();

            if(Config.useTts()){
                qualityRater.unregisterQualityListener(qtsEngine);
                qtsEngine.speak("Quality monitoring done.");
                qtsEngine.shutdown();
            }

            accelerationHandler.unregisterFilteredAccelerationListener(qualityRater);

            qualityRater.unregisterQualityListener(tripLogger);
            locationHandler.unregisterNewLocationListener(tripLogger);

            tripLogger = null;
            qualityRater = null;

            if(Config.autoReplySms()){                   // Deactivate SMSReceiver
                ComponentName component = new ComponentName(c, SMSReceiver.class);
                PM.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                                           PackageManager.DONT_KILL_APP);
            }

                                                         // Restore vibration settings
            if(notifyVibrate != 0) AM.setVibrateSetting(AudioManager.VIBRATE_TYPE_NOTIFICATION, notifyVibrate);
            if(ringerVibrate != 0) AM.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, ringerVibrate);

            CURRENT_STATE = STATE_IDLE;
        }
    }

    /**
     * Check to see if we are monitoring at the moment.
     * @return True if current state is monitoring.
     */
    public boolean isMonitoring(){
        return CURRENT_STATE == STATE_MONITORING;
    }

    /**
     * Call this function to get the filename of the file currently beeing logged.
     * @return file name.
     */
    public String getLogFileName() {
        return tripLogger.getFileName();
    }
}
