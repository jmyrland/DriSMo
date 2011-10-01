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
import com.drismo.model.MapPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import java.util.LinkedList;

/**
 * Overlay for the BasicMap an mapMonitor, draws the route.
 */
public class TripOverlay extends Overlay {

    private MapPoint[] mapPoints;
    private float[] lat;
    private float[] longs;
    private int[] color;
    private Bitmap drismobil;
    private Paint paint;
    private int lastPaint;

    /**
     * @param applicationContext The context
     * @param drawable the bitmap to draw at the end (car)
     */
    public TripOverlay(Context applicationContext, Bitmap drawable) {
        super(applicationContext);
        drismobil = drawable;
        paint = new Paint();
        lastPaint =0;
        lat = new float[0];
        longs =new float[0];
        color =new int[0];
        paint.setStrokeWidth(10);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
        mapPoints = new MapPoint[0];
    }

    /**
     * The drawing method
     * @param canvas the canvas to draw on.
     * @param mapView The mapView, used to get the projection
     * @param b dummy
     */
    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean b) {

        lastPaint = Color.GREEN;
        paint.setColor(Color.GREEN);

        Point from = new Point();
        Point to = new Point();
        synchronized (this){
            Rect rect = mapView.getProjection().getScreenRect();     //used to find out if points are on the screen

            final int size = lat.length;
            if(lat != null && size >1){

                for(int i =1; i < size; i++){
                    mapView.getProjection().toPixels(new GeoPoint(lat[i-1], longs[i-1]), from);
                    mapView.getProjection().toPixels(new GeoPoint(lat[i], longs[i]), to);

                    if(rect.contains(to.x ,to.y)){                  //if the point is on the screen, draw it.
                        paint.setColor(color[i]);
                        paint.setShader(new LinearGradient(from.x, from.y, to.x, to.y, lastPaint, paint.getColor(), Shader.TileMode.CLAMP));
                        canvas.drawLine(from.x, from.y, to.x, to.y, paint);
                        lastPaint = paint.getColor();
                    }
                }
                canvas.drawBitmap(drismobil, to.x - (60*mScale), to.y - (20*mScale), null);
            }
        }
    }

    /**
     * updates the trip from a linkedlist of MapPoints.
     * @see com.drismo.gui.monitor.MapMonitor
     * @param mapP the linkedlist
     */
    public void updateTripHistory(LinkedList<MapPoint> mapP) {
        mapPoints = mapP.toArray(mapPoints);
        final int size = mapPoints.length;
        lat =new float[size];
        longs =new float[size];
        color =new int[size];

        for(int i =0; i<size; i++){
            lat[i]   = mapPoints[i].latitude;
            longs[i] = mapPoints[i].longitude;
            color[i] = mapPoints[i].color;
        }
    }

    /**
     * Used to update the trip from arrays.
     * @see BasicMap
     * @param lats array with all the latitudes
     * @param long1 array with longitudes
     * @param colors array with all the colors, matching the lats/long1
     */
    public void updateTripHistory(float[] lats, float[] long1, int[] colors) {
        lat = lats.clone();
        longs = long1.clone();
        color = colors.clone();
    }
}
