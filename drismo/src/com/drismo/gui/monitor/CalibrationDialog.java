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
import android.content.Context;
import android.content.DialogInterface;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import com.drismo.R;
import com.drismo.model.Config;

/**
 * A dialog to guide the user through the calibration phase.
 */
public class CalibrationDialog extends Dialog implements  DialogInterface.OnCancelListener {

    private TextView text;
    private ImageView image;

    private MonitorActivityTemplate context;

    /**
     * Creates the dialog to guide the user through the calibration phase.
     * @param c Context initiating the calibration.
     */
    public CalibrationDialog(Context c) {
        super(c);

        try {
            Config.setConfigLocale(c, Config.getLanguageCode());
        } catch(Exception e){}

        context = (MonitorActivityTemplate) c;

        setOnCancelListener(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.calibration_dialog);

        text = (TextView) findViewById(R.id.calibrationText );
        image = (ImageView) findViewById(R.id.calibrationImage);
    }

    /**
     * Set the state as calibration, notifying the user that the vehicle must remain still.
     */
    public synchronized void setStateCalibrating(){
        text.setText(context.getString(R.string.keepVehicleStill));
        image.setImageResource(R.drawable.ic_dialog_calibration_stop);
    }

    /**
     * Change the dialog state, letting the user know its time to drive foreward.
     */
    public synchronized void setStateReadyToDrive(){
        text.setText(context.getString(R.string.driveVehicleForward));
        image.setImageResource(R.drawable.ic_dialog_calibration_go) ;
    }

    /**
     * When the dialog is canceled, we must notify the monitor of this action as well.
     * @param dialogInterface The current dialog interface.
     */
    public void onCancel(DialogInterface dialogInterface) {
        context.resetCalibration();
    }
}
