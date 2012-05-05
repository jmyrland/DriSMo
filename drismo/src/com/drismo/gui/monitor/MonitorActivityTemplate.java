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

package com.drismo.gui.monitor;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.drismo.R;
import com.drismo.gui.BaseActivity;
import com.drismo.gui.ViewTrip;
import com.drismo.logic.CalibrationListener;
import com.drismo.logic.MonitorController;
import com.drismo.logic.QualityListener;
import com.drismo.model.Config;

/**
 * This class is a template for creating a custom quality monitor. The derived class must only specify the layout
 * in the abstract method <code>setUpLayout()</code> and control it as wished specified by the abstract method
 * <code>onQualityUpdate()</code>.
 *
 * The rest (i.e. initiation, calibration, etc) is controlled/handled in <code>MonitorActivityTemplate</code>.
 */
public abstract class MonitorActivityTemplate extends BaseActivity implements CalibrationListener, QualityListener {

    private static final String TAG = "MonitorTemplate";
    private static final int NOTIFICATION_MONITORING = 0;

    private MonitorController controller;

    private CalibrationDialog calibrationDialog;

    private static final int CALIBRATION_DIALOG = 0;

    private PowerManager.WakeLock wl;


    /**
     * Constructs the required layout (depending if we are monitoring or idle) and instantiates the necessary objects.
     *
     * @param savedInstanceState Never used. State is handled/controlled by the <code>MonitorController</code>.
     * @see MonitorController
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Config.logGps()) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER )) {
                Toast.makeText(getApplicationContext(), getString(R.string.turnOnGps),
                Toast.LENGTH_LONG).show();

                Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                startActivity(myIntent);
            }
        }

//        Log.d(TAG, "MonitorTemplate::onCreate fired");

        controller = MonitorController.requestMonitorController(this);

                                                    // Set the screen to not dim:
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if(Config.getDimScreen())
            wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, MonitorActivityTemplate.class.getSimpleName() );
        else
            wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, MonitorActivityTemplate.class.getSimpleName() );

        if(controller.isMonitoring()) {
            lockScreenRotation();                   // Lock the screen orientation if we are monitoring.
            setUpLayout();
            setUpNotificationIcon();
        } else
            setUpCalibrationLayout();
    }

    /**
     * Sets up the calibration preface.
     */
    private void setUpCalibrationLayout(){
        if(getResources().getConfiguration().orientation == 2)
             setContentView(R.layout.calibration_layout_land);          // Horizontal
        else setContentView(R.layout.calibration_layout);               // Vertical

        final LinearLayout calibrationLayout = (LinearLayout)this.findViewById(R.id.calibrationLayout);
        calibrationLayout.setFocusable(true);
        calibrationLayout.requestFocus();                               // Accessability for non touch phones

        View.OnTouchListener calibTouchListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent ev) {
                prepareQualityMonitor();
                calibrationLayout.setVisibility(View.GONE);
                return true;
            }
        };
        calibrationLayout.setOnTouchListener(calibTouchListener);
                                                                       // Accessability for non touch phones
        View.OnClickListener calibClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                prepareQualityMonitor();
                calibrationLayout.setVisibility(View.GONE);
            }
        };
        calibrationLayout.setOnClickListener(calibClickListener);
    }

    /**
     * Sets screen rotation as fixed to current rotation setting
     */
    private void lockScreenRotation() {

        switch (this.getResources().getConfiguration().orientation){
            case Configuration.ORIENTATION_PORTRAIT:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
        }
    }

    /**
     * Prepares the quality monitor by locking the screen, starting calibration and showing the calibration dialog.
     */
    protected void prepareQualityMonitor(){
        lockScreenRotation();
        showDialog(CALIBRATION_DIALOG);
        calibrationDialog.setStateCalibrating();
        controller.initiateCalibration();
    }

    /**
     * Cancels calibration and resets layout to calibration-layout.
     */
    public void resetCalibration() {
//        controller.cancelCalibration();
        controller.cancelCalibration();
        setUpCalibrationLayout();
    }

    /**
     * When roll, pitch and offset is calibrated, instruct the driver to go forward.
     */
    public void onOffsetCalculationComplete() {
        calibrationDialog.setStateReadyToDrive();
    }

    /**
     * Sets up the layout of the child class and hides the calibration dialog. It also starts the quality monitoring.
     * When the <code>Calibration</code> is done calibrating, this method is fired.
     */
    public void onCalibrationCompleted() {
        setUpLayout();
        calibrationDialog.hide();               // Hide the calibration dialog, since were done calibrating.
        Toast.makeText(getApplicationContext(), getString(R.string.deviceCalibrated), Toast.LENGTH_LONG).show();
        startQualityMonitoring();
    }

    /**
     * Starts all the processes involving the quality monitoring.
     */
    private void startQualityMonitoring(){
        lockScreenRotation();                       // Lock the screen orientation before starting the QR.
        controller.startMonitoring();

        setUpNotificationIcon();                // Set status bar icon.
    }

    /**
     * Sets up the notification icon in the status bar. Used when initiating quality monitoring.
     */
    private void setUpNotificationIcon(){
        Notification notification = new Notification(R.drawable.ic_stat_notify_drismo,
                                                     null,
                                                     System.currentTimeMillis() );
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        Context context = getApplicationContext();
        CharSequence contentTitle = getString(R.string.app_name);
        CharSequence contentText = getString(R.string.currentlyMonitoring);

        Intent notificationIntent = this.getIntent();
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, PendingIntent.FLAG_CANCEL_CURRENT, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_MONITORING , notification);
    }

    /**
     * Stops all the processes involving the quality monitoring.
     */
    private void stopQualityMonitoring(){
        controller.stopMonitoring();
        finish();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_MONITORING);
    }

    /**
     * Stops monitoring when back pressed.
     */
    @Override
    public void onBackPressed(){
        if(controller.isMonitoring()) {
            Intent intent = new Intent(getApplicationContext(), ViewTrip.class);
            intent.putExtra(ViewTrip.EXTRA_FILENAME, controller.getLogFileName());
            startActivity(intent);
        }
        stopQualityMonitoring();
    }

    /**
     * Works as intended, but acquires the wake lock.
     */
    @Override
    protected void onResume() {
        super.onResume();

        wl.acquire();
    }

    /**
     * Works as intended, but releases the wake lock.
     */
    @Override
    protected void onPause() {
        super.onPause();

        wl.release();
//        finish();
    }

    /**
     * Creates the calibration dialog.
     *
     * @param id CalibrationDialog id, see <code>CALIBRATION_DIALOG</code>.
     * @return The calibration dialog.
     */
    public Dialog onCreateDialog(int id) {
        if(id == CALIBRATION_DIALOG  && calibrationDialog == null)
            calibrationDialog = new CalibrationDialog(this);

        return calibrationDialog;
    }

    /**
     * Creates the menu to switch custom monitors.
     * @param menu Menu to insert into.
     * @return Always true.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.monitor_menu, menu);
        return true;
    }

    /**
     * Switches between the different custom monitors, based on the given input.
     *
     * @param item The selected monitor to view.
     * @return True if success.
     * @see MonitorFactory#getMonitorActivityIntent(android.content.Context, int)
     * @see PrimitiveMonitor
     * @see QualityMeterMonitor
     * @see MapMonitor
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent;
        // Handle menu item selection
        switch (item.getItemId()) {                             // Starts a new activity:
            case R.id.primitive:                                            // PrimitiveMonitor:
                myIntent = MonitorFactory.getMonitorActivityIntent(this, MonitorFactory.PRIMITIVE_MONITOR);
                break;

            case R.id.wheel:                                                // QualityMeterMonitor:
                myIntent = MonitorFactory.getMonitorActivityIntent(this, MonitorFactory.METER_MONITOR );
                break;

            case R.id.map:                                                  // MapMonitor:
                if(Config.logGps()){
                    myIntent = MonitorFactory.getMonitorActivityIntent(this, MonitorFactory.MAP_MONITOR);
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.noGpsActive), Toast.LENGTH_LONG).show();
                    return false;
                }

                break;

            case R.id.debug:                                                // Debug: Todo: remove after testing
                myIntent = MonitorFactory.getMonitorActivityIntent(this, MonitorFactory.DEBUG_MONITOR);
                break;
                                                                            // Unknown, so return:
            default:
                return super.onOptionsItemSelected(item);
        }

        startActivity(myIntent);

        finish();                                            // Finish this activity before returning..
        return true;
    }

    /**
     * This method must be specified in the child class, and should work the same way as the method
     * <code>onCreate()</code> in a normal activity.
     */
    protected abstract void setUpLayout();

    /**
     * This method must be specified in the child class, and should control the layout based on the input parameter.
     * @param newScore The new quality score.
     * @see com.drismo.model.Quality
     */
    public abstract void onQualityUpdate(int newScore);

}
