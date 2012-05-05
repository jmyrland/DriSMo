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

import android.graphics.BitmapFactory;
import android.os.Bundle;
import com.drismo.R;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.util.constants.MapViewConstants;

/**
 * Used to display a archived trip on the map.
 * @see ViewTrip
 */
public class BasicMap extends BaseActivity implements MapViewConstants {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        Bundle extras = getIntent().getExtras();
        final float[] lats =  extras.getFloatArray("lat");
        final float[] longs = extras.getFloatArray("long");
        final int[] colors=  extras.getIntArray("colors");
                                                                        //create the overlay used to draw the trip
        TripOverlay currentTrip = new TripOverlay(getApplicationContext(), BitmapFactory.decodeResource(getResources(), R.drawable.img_map_drismobile));

        currentTrip.updateTripHistory(lats, longs, colors);             //sends the route to the overlay

        MapView mapView = (MapView) this.findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        MapController mapController = mapView.getController();

        mapController.setZoom(15);
        final int size = lats.length-1;
        mapController.setCenter(new GeoPoint(lats[size], longs[size])); // TODO: NoSuchElementException

        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(this);
	    mapView.getOverlays().add(mScaleBarOverlay);
        mapView.getOverlays().add(currentTrip);
    }
}