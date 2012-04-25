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

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.view.View;
import com.drismo.R;
import com.drismo.model.Quality;

/**
 * The class is taken from http://android.arnodenhond.com/components/graphview
 * and modified by us.
 * Used to display the qualitygraph of a trip.
 * @see ViewTrip
 */
public class GraphView extends View {

	private Paint paint;
	private Paint lastPaint;
	private int[] values;
	private String[] verlabels;
	private String title;
    private int[] color;
    private Context c;

    /**
     * Used to set up the graph
     * @param context the context
     * @param values the quality values
     * @param title the title of the trip
     * @param colors the colors, matching the values
     */
	public GraphView(Context context, int[] values, String title, int[] colors) {
        super(context);
        c = context;
        color = colors;

		if (values == null)
			values = new int[0];
		else
			this.values = values;
		if (title == null)
			title = "";
		else
			this.title = title;
                                    //labels for the graph
        verlabels = new String[] { c.getString(R.string.excellent),
                                   c.getString(R.string.ok),
                                   c.getString(R.string.bad)};


		paint = new Paint();
        lastPaint = new Paint();
        lastPaint.setColor(Color.GREEN);
	}

    /**
     * Draws the graph
     * @param canvas the canvas to draw on.
     */
	@Override
	protected void onDraw(Canvas canvas) {
		final float border = 20;
		final float horstart = border * 2;
		final float height = getHeight();
		final float width = getWidth() - 1;
        final float max = Quality.MAX_SCORE;
		final float min = Quality.MIN_SCORE;
		final float diff = max - min;
		final float graphheight = height - (2 * border);
		final float graphwidth = width - (2 * border);

        synchronized (this){

            paint = new Paint();
            paint.setTextAlign(Align.LEFT);
            int vers = verlabels.length - 1;
            for (int i = 0; i < verlabels.length; i++) {
                paint.setColor(Color.DKGRAY);
                float y = ((graphheight / vers) * i) + border;
                canvas.drawLine(horstart, y, width, y, paint);
                paint.setColor(Color.WHITE);
                canvas.drawText(verlabels[i], 0, y, paint);
            }

            paint.setTextAlign(Align.CENTER);
            canvas.drawText(title, (graphwidth / 2) + horstart, border - 4, paint);

            if(ViewTrip.showThresholds() == 1){             //draw the quality thresholds
                paint.setColor(Quality.BAD_COLOR);
                canvas.drawLine(border, (border - ((Quality.OK_SCORE -min) / diff)*graphheight  + graphheight),
                                width, (border - ((Quality.OK_SCORE -min) / diff)*graphheight  + graphheight), paint);

                paint.setColor(Quality.GOOD_COLOR);
                canvas.drawLine(border, (border - ((Quality.GOOD_SCORE -min) / diff)*graphheight  + graphheight),
                                width, (border - ((Quality.GOOD_SCORE -min) / diff)*graphheight  + graphheight), paint);

                paint.setColor(Quality.EXCELLENT_COLOR);
                canvas.drawLine(border, (border - ((Quality.EXCELLENT_SCORE -min) / diff)*graphheight  + graphheight),
                                width, (border - ((Quality.EXCELLENT_SCORE -min) / diff)*graphheight  + graphheight), paint);
            }
            paint.setColor(Color.LTGRAY);
            paint.setStrokeWidth(3);

            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);

            float datalength = values.length;
            float colwidth = (width - (2 * border)) / datalength;
            float halfcol = colwidth / 2;
            float lasth = 0;
            for (int i = 0; i < values.length; i++) {           //calculate all the graphpoints and draw the graph
                float val = values[i] - min;
                float rat = val / diff;
                float h = graphheight * rat;

                paint.setColor(color[i]);                       //set the shader to fade from the last quality to the current color.
                paint.setShader(new LinearGradient(((i - 1) * colwidth) + (horstart + 1) + halfcol, (border - lasth) + graphheight,
                                                    (i * colwidth) + (horstart + 1) + halfcol, (border - h) + graphheight ,lastPaint.getColor(), paint.getColor(), Shader.TileMode.CLAMP));

                if (i > 0)                                     //draw the line between 2 points
                    canvas.drawLine(((i - 1) * colwidth) + (horstart + 1) + halfcol, (border - lasth) + graphheight,
                                    (i * colwidth) + (horstart + 1) + halfcol, (border - h) + graphheight, paint);
                lasth = h;
                lastPaint.setColor(paint.getColor());
            }
        }
	}
}
