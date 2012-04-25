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

import android.util.Log;
import com.drismo.R;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Used to get the city/street name from a location.
 * Uses 2 different webservices from www.geonames.org
 * @see com.drismo.gui.ViewTrip
 */
public class JsonFunctions {

    /**
     * Borrowed from:
     * http://p-xr.com/android-tutorial-how-to-parse-read-json-data-into-a-android-listview/
     * and slightly modified.
     * @param url the url to get JSON data from
     * @return the JSONObject containing all the info.
     */
    public static JSONObject getJSONfromURL(String url){
        //initialize
        InputStream is = null;
        String result = "";
        JSONObject jArray = null;
        //http post
        try{
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        }catch(Exception e){
            Log.e("log_tag", "Error in http connection " + e.toString());
        }
        //convert response to string
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is),8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            result=sb.toString();
        }catch(Exception e){
            Log.e("log_tag", "Error converting result "+e.toString());
        }
        //try parse the string to a JSON object
        try{
            jArray = new JSONObject(result);
        }catch(JSONException e){
            Log.e("log_tag", "Error parsing data "+e.toString());
        }
        return jArray;
    }

    /**
     * Get the city from a position.
     * @param lat the latitude
     * @param lon longitude
     * @return returns the City name or -- if something went wrong
     */
    public static String getCity(float lat, float lon){
        try {
            JSONObject json = getJSONfromURL("http://api.geonames.org/findNearbyPlaceNameJSON?lat=" + lat + "&lng=" + lon + "&username=drismo&style=full");
            JSONArray city = json.getJSONArray("geonames");

            return city.getJSONObject(0).getString("adminName2");
        } catch (Exception e) {
            e.printStackTrace();
            return "--";
        }
    }

    /**
     * Gets the streetname from a position.
     * @param lat the latitude
     * @param lon longitude
     * @return   returns the street name or -- if something went wrong
     */
    public static String getStreet(float lat, float lon){
        try {
            JSONObject json = getJSONfromURL("http://api.geonames.org/findNearbyStreetsOSMJSON?lat=" + lat + "&lng=" + lon + "&username=drismo");
            JSONArray street = json.getJSONArray("streetSegment");

            return street.getJSONObject(0).getString("name");
        } catch (Exception e) {
            e.printStackTrace();
            return "--";
        }
    }
}
