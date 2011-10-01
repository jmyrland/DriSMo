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

import android.location.*;
import android.os.Bundle;
import com.drismo.model.Config;
import java.util.ArrayList;

/**
 * This class handles everything related to receiving and distributing location updates
 * to listeners which requires this service. To receive location updates, add a
 * <code>NewLocationListener</code> via the <code>registerNewLocationListener</code>
 * method. Don't forget to <code>unregisterNewLocationListener</code> the listener when
 * finished.
 *
 * @see NewLocationListener
 * @see LocationHandler#registerNewLocationListener(NewLocationListener)
 */
public class LocationHandler implements LocationListener, GpsStatus.Listener {
    private LocationManager locationManager = null;
    private final ArrayList<NewLocationListener> newLocationListeners = new ArrayList<NewLocationListener>();

    /**
     * Constructor. Sets the location manager.
     * @param lm Location manager.
     */
    public LocationHandler(LocationManager lm){
        locationManager = lm;
    }

    /**
     * Stars listening for GPS updates, if required.
     */
    public void startListening(){
        if(Config.logGps()){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (Config.powerSaverOn() ? 6000 : 1000), 0, this);
            locationManager.addGpsStatusListener(this);
        }
    }

    /**
     * Stops the GPS updates.
     */
    public void stopListening(){
        locationManager.removeUpdates(this);
        locationManager.removeGpsStatusListener(this);
    }

    /**
     * Registers a new listeners, which requires location updates.
     * @param listener The listener to add.
     */
    public void registerNewLocationListener(NewLocationListener listener) {

        if(newLocationListeners.size() == 0){
            startListening();
        }

        if(!newLocationListeners.contains(listener))
            newLocationListeners.add(listener);
    }

    /**
     * Removes a listener from the update-list. The subject stops listening if no other listeners are present.
     * @param listener The listener to remove.
     */
    public void unregisterNewLocationListener(NewLocationListener listener) {
        newLocationListeners.remove(listener);

        if(newLocationListeners.size() == 0){           // If empty listeners list, stop listening..
            stopListening();
        }
    }


    /**
     * On location change, update all the listeners!
     * @param loc The new location provided.
     * @see NewLocationListener#onNewLocation(android.location.Location)
     */
    public void onLocationChanged(Location loc) {
        if(loc != null && (loc.getLongitude() != 0.0 && loc.getLatitude() != 0.0)){

            for(NewLocationListener listener : newLocationListeners)
                listener.onNewLocation(loc);
        }
    }

    /**
     * Required but not used.
     * @param event not used.
     */
    public void onGpsStatusChanged(int event){    }

    /**
     * Required but not used.
     * @param provider not used.
     */
    public void onProviderDisabled(String provider) {    }

    /**
     * Required but not used.
     * @param provider not used.
     */
    public void onProviderEnabled(String provider) {    }

    /**
     * Required but not used.
     * @param provider not used.
     */
    public void onStatusChanged(String provider, int status, Bundle arg) {    }
}
