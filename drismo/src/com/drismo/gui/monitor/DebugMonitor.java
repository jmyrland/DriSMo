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

import android.content.Context;
import android.graphics.*;
import android.view.View;
import com.drismo.logic.FilteredAccelerationListener;

import java.util.ArrayList;

/**
 * Used to debug the calibration. Should not be used in the final release.
 * Displays a real time graph representing the acceleration values with 3 coloured lines.
 */
public class DebugMonitor extends MonitorActivityTemplate implements FilteredAccelerationListener {
    private static final String TAG = "DebugMonitor";

    private final ArrayList<float[]> accValues = new ArrayList<float[]>();

    private AccelerationGraph graph = null;
    private int score;


    /**
     * Sets up the layout of the debug monitor.
     */
    @Override
    protected void setUpLayout() {
        graph = new AccelerationGraph(this);

        setContentView(graph);
    }

    /**
     * Not used in this monitor.
     * @param newScore The new quality score.
     */
    @Override
    public void onQualityUpdate(int newScore) {
        score = newScore;
    }

    /**
     * On new acceleration update, update the graph.
     * @param filteredAccelerationValues Noise reduced acceleration vectors.
     * @param RotatedWmaValues
     */
    public void onFilteredAccelerationChange(float[] filteredAccelerationValues, float[] RotatedWmaValues) {
        if(graph != null){
            accValues.add(RotatedWmaValues.clone());

            int N = 50;

            int len = ( accValues.size() > N) ? N : accValues.size();
            int start = ( accValues.size() > N) ? accValues.size() - N : 0;

            float[][] a = new float[ len ][];

            for(int i = 0; i < len ; i++)
                a[i] = accValues.get(start + i);


            graph.updateView(a);
        }
    }

    /**
     * The graph representing the 50 last acceleration values.
     */
    private class AccelerationGraph extends View {

        private Paint paint;
        private float[][] values;

        public AccelerationGraph(Context context) {
            super(context);

            values = new float[0][];

            paint = new Paint();
        }

        /**
         * Draws the graph, with the current values.
         * @param canvas The canvas.
         */
        @Override
        protected void onDraw(Canvas canvas) {
            final float border = 20;
            final float horstart = border * 2;
            final float height = getHeight();
            final float width = getWidth() - 1;
            final float max = 10;
            final float min = -10;
            final float diff = max - min;
            final float graphheight = height - (2 * border);
            final float graphwidth = width - (2 * border);

            synchronized (this){

                paint = new Paint();

                paint.setTextAlign(Paint.Align.CENTER);

                paint.setColor(Color.LTGRAY);
                paint.setStrokeWidth(2);

                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setAntiAlias(true);

                float datalength = values.length;
                float colwidth = (width - (2 * border)) / datalength;
                float halfcol = colwidth / 2;
                float lasth = 0;

                for(int j = 0; j < 3; j++){

                    if(j == 0)
                        paint.setColor(Color.RED);
                    else if(j == 1)
                        paint.setColor(Color.GREEN);
                    else
                        paint.setColor(Color.BLUE);


                    for (int i = 0; i < values.length; i++) {
                        float val = values[i][j] - min;
                        float rat = val / diff;
                        float h = graphheight * rat;

                        if (i > 0){
                            canvas.drawLine(((i - 1) * colwidth) + (horstart + 1) + halfcol, (border - lasth) + graphheight, (i * colwidth) + (horstart + 1) + halfcol, (border - h) + graphheight, paint);
                        }
                        lasth = h;
                    }
                }

                paint.setColor(Color.LTGRAY);                       // Draw the Zero-line:
                paint.setStrokeWidth(1);
                paint.setTextSize(32);
                canvas.drawText(score+"", 100, 350, paint);

                for (int i = 0; i < 50; i++) {
                    float val = 0 - min;
                    float rat = val / diff;
                    float h = graphheight * rat;

                    if (i > 0){
                        canvas.drawLine(((i - 1) * colwidth) + halfcol, (border - lasth) + graphheight, (i * colwidth) + (horstart + 1) + halfcol, (border - h) + graphheight, paint);
                    }
                    lasth = h;
                }

            }
        }

        public void updateView(float[][] values){
            this.values = values;
            this.invalidate();
        }

    }

}
