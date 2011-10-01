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

import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import com.drismo.R;
import com.drismo.gui.TripOverlay;
import com.drismo.model.MapPoint;
import com.drismo.logic.NewLocationListener;
import com.drismo.model.Quality;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.util.constants.MapViewConstants;

import java.util.LinkedList;

public class MapMonitor extends MonitorActivityTemplate implements MapViewConstants, NewLocationListener {

    private MapView mapView;
    private MapController mapController;
    private ScaleBarOverlay mScaleBarOverlay;
    private TripOverlay currentTrip;
    private int historySeconds;
    private LinkedList<MapPoint> mapPoints;
    private int score;

    /**
     * Handler required to communicate between the thread firing <code>onQualityUpdate()</code> and
     * the current thread.
     */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg){
            View qualityView = findViewById(R.id.qualityColor);
            int newScore = msg.what;
            if(qualityView != null && qualityView.getVisibility() != View.GONE){
                qualityView.setBackgroundColor(Quality.getDynamicColorFromScore(newScore));
            }
        }
    };

    /**
     * Sets up the layout for the map monitor.
     */
    @Override
    protected void setUpLayout() {
        setContentView(R.layout.map_layout);

        mapPoints = new LinkedList<MapPoint>();
        historySeconds = 50;

        currentTrip = new TripOverlay(getApplicationContext(),BitmapFactory.decodeResource(getResources(), R.drawable.img_map_drismobile));

        mapView = (MapView) this.findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        mapController = this.mapView.getController();
        mapController.setZoom(17);

        findViewById(R.id.loadingScreen).setVisibility(View.VISIBLE);
        findViewById(R.id.changeMonitor).setVisibility(View.VISIBLE);

	    mScaleBarOverlay = new ScaleBarOverlay(this);
	    mapView.getOverlays().add(mScaleBarOverlay);
	    mapView.getOverlays().add(currentTrip);
    }

    /**
     * Starting loading animation when the window gets focus.
     * @param hasFocus True if the window has focus. False otherwise.
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus) {
            ImageView loadingView;
            try {
                loadingView = (ImageView) findViewById(R.id.loadingAnim);
                loadingView.setBackgroundResource(R.anim.loading);
                AnimationDrawable loadingAnim = (AnimationDrawable) loadingView.getBackground();
                loadingAnim.start();
            }
            catch(Exception e) {
                try{
                    loadingView = (ImageView) findViewById(R.id.loadingAnim);
                    loadingView.setBackgroundResource(R.anim.loading);
                    AnimationDrawable loadingAnim = (AnimationDrawable) loadingView.getBackground();
                    loadingAnim.start();
                }
                catch(Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
    }


    /**
     * When there is a new location, add it to the mapPoint list, if it is a real location.
     * @param loc the new location
     */
    public void onNewLocation(Location loc) {

        try{
            if(mapPoints.size() > 0) {
                findViewById(R.id.loadingScreen).setVisibility(View.GONE);
                findViewById(R.id.changeMonitor).setVisibility(View.GONE);
                findViewById(R.id.mapview).setVisibility(View.VISIBLE);
            }

            mapPoints.add(new MapPoint((float) loc.getLatitude(), (float) loc.getLongitude(), Quality.getDynamicColorFromScore(score), 0, loc.getSpeed()));
            if(mapPoints.size() == historySeconds)
                mapPoints.removeFirst();
            refreshTripMap();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * sends the previous map points to the currentTrip overlay.
     * Centers the map on the current location.
     * redraws.
     */
    public void refreshTripMap(){
        currentTrip.updateTripHistory(mapPoints);
        mapController.setCenter(new GeoPoint(mapPoints.getLast().latitude, mapPoints.getLast().longitude));
        mapView.invalidate();
    }

    /**
     * updates the score
     * @param newScore The new quality score.
     */
    @Override
    public void onQualityUpdate(int newScore) {
        score = newScore;
        handler.sendEmptyMessage(newScore);
    }

}
