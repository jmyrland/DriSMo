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

import android.view.animation.*;
import com.drismo.R;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import com.drismo.model.Quality;

/**
 * A primitive monitor displaying the quality in the form of a short text on a colored background.
 */
public class PrimitiveMonitor extends MonitorActivityTemplate {

    private LinearLayout layout;
    private TextView statusText;

    private int currentStringId = -1;

    private Animation fadeAnimation;

    /**
     * Handler required to communicate between the thread firing <code>onQualityUpdate()</code> and
     * the current thread.
     */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg){
            int newScore = msg.what;
            //layout.setBackgroundColor( MonitorController.getColorFromScore(newScore) );
            if(layout!= null){
                layout.setBackgroundColor(Quality.getDynamicColorFromScore(newScore));
                int newStringId = Quality.getQualityStringId(newScore);
                if(currentStringId != newStringId){
                    currentStringId = newStringId;
                    statusText.setText(getString(currentStringId));
                    statusText.startAnimation(fadeAnimation);
                }
            }
        }
    };

    /**
     * Setting up the layout of the primitive monitor.
     */
    @Override
    protected void setUpLayout() {

        layout = new LinearLayout(this);
        layout.setBackgroundColor(Color.BLUE);
        setContentView(layout);

        statusText = new TextView(this);
        statusText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        statusText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 70);
        statusText.setGravity(Gravity.CENTER);
        statusText.setText(" ");  // N/A

        fadeAnimation = AnimationUtils.loadAnimation(this, R.anim.shrink_and_fade_out);
        fadeAnimation.setFillEnabled(true);
        fadeAnimation.setFillAfter(true);

        layout.addView(statusText,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT)
        );
    }

    /**
     * Send the new quality to the current thread updating the current layout.
     * @param newScore The new quality score.
     */
    @Override
    public synchronized void onQualityUpdate(int newScore) {
        handler.sendEmptyMessage(newScore);
    }

}
