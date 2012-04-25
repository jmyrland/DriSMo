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

package com.drismo.model;

import android.content.Context;
import android.location.Location;
import com.drismo.R;
import com.drismo.logic.JsonFunctions;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Model of a trip, containing all elements for viewing a trip. This class also
 * handles reading a trip from a file.
 */
public class Trip {

    private final ArrayList<MapPoint> pointList = new ArrayList<MapPoint>();       //arrayList to read the mapPoints from the file

    private final int refreshRate;

    private final int[] scoreArray;
    private final int[] colorArray;
    private final float[] speedArray;

    private boolean gpsCordsAvailable = false;
    private final int[] shortColorArray;
    private final float[] latitudeArray;
    private final float[] longitudeArray;
    private float tripLength;
    private Context context;
    private String fileName;


    /**
     * Load a trip by reading the specified file.
     * @param filename The given file name to read.
     * @param c Context requesting the trip.
     * @throws IOException If reading fails an IOException is thrown.
     */
    public Trip(String filename, Context c) throws IOException {

        String buff;
        String[] splitBuff = new String[5];
        context = c;
        fileName = filename;

        if(!filename.endsWith(".csv")) {
            filename +=".csv";
        }

        final BufferedReader in = new BufferedReader(
                new InputStreamReader(c.openFileInput(filename))
        );

        refreshRate = Integer.parseInt(in.readLine().split(":")[1]);
        in.readLine();

        StringTokenizer st;

        while (!((buff = in.readLine()) == null)){    //reads all the map points from file

            st = new StringTokenizer(buff, ",");

            for(int i = 0; i < 5; i++)
                splitBuff[i] = st.nextToken();

            pointList.add(
                     new MapPoint(
                             splitBuff[2],
                             splitBuff[3],
                             Quality.getDynamicColorFromScore(Integer.parseInt(splitBuff[1])),
                             Integer.parseInt(splitBuff[1]),
                             splitBuff[4]
                     )
            );

        }

        in.close();

        int size = pointList.size();
        scoreArray = new int[size];
        colorArray = new int[size];
        speedArray = new float[size];
        tripLength =0;
        float[] lengthBuffer = new float[3];

        int invalidGpsPoints = 0;

        for(int i =0; i < size; i++){
            scoreArray[i] = pointList.get(i).score;
            speedArray[i] = pointList.get(i).speed;
            colorArray[i] = pointList.get(i).color;
            if(pointList.get(i).longitude != 0.0){
                gpsCordsAvailable = true;
                if( i > 0 && pointList.get(i).longitude == pointList.get(i-1).longitude
                         && pointList.get(i).latitude == pointList.get(i-1).latitude){
                    invalidGpsPoints ++;
                }
            }else invalidGpsPoints ++;
        }

        shortColorArray = new int[size -invalidGpsPoints];
        latitudeArray = new float[size -invalidGpsPoints];
        longitudeArray = new float[size -invalidGpsPoints];

        if(size -invalidGpsPoints >1){
            latitudeArray[0] = 0;
            longitudeArray[0] = 0;

            int j=0;
            for (MapPoint aPointList : pointList) {
                if (aPointList.longitude != 0.0) {                      //only add the location if it is a real location.
                    if(j==0) {
                        latitudeArray[j] = aPointList.latitude;
                        longitudeArray[j] = aPointList.longitude;
                        shortColorArray[j] = aPointList.color;
                        j++;
                    }
                    else if(!(aPointList.longitude == longitudeArray[j-1] && aPointList.latitude == latitudeArray[j-1])){
                        latitudeArray[j] = aPointList.latitude;
                        longitudeArray[j] = aPointList.longitude;
                        shortColorArray[j] = aPointList.color;
                        try{
                            Location.distanceBetween(latitudeArray[j-1] ,longitudeArray[j-1], latitudeArray[j] , longitudeArray[j], lengthBuffer);
                            tripLength += lengthBuffer[0];
                        }catch (IllegalArgumentException e){
                            //
                        }
                        j++;
                    }
                }
            }
        }
    }


//    /** TODO: fjern?
//     * Custom float parser to optimize the creation of map points.
//     * Todo: XX times faster than the <code>Float.parseFloat()</code> method.
//     * @param f The string to parse.
//     * @return Returns the float value of the given input.
//     */
//    private static float stringToFloat(String f){
//        int totLength = f.length();
//        int commaPos = f.indexOf('.') + 1;
//
//        return Integer.parseInt(f.substring(0, commaPos-1)) +
//                (float) (Long.parseLong(f.substring(commaPos, totLength)) / (Math.pow(10, totLength-commaPos)));
//    }

    // TODO: kommenter ned igjennom ..
    public int getRefreshRate(){
        return refreshRate;
    }

    public int[] getScoreArray() {
        return scoreArray;
    }

    public int[] getColorArray() {
        return colorArray;
    }

    public float[] getSpeedArray() {
        return speedArray;
    }

    public boolean isGpsCordsAvailable() {
        return gpsCordsAvailable;
    }

    public int[] getShortColorArray() {
        return shortColorArray;
    }

    public float[] getLatitudeArray() {
        return latitudeArray;
    }

    public float[] getLongitudeArray() {
        return longitudeArray;
    }

    public float getTripLengthInMeters(){
        return tripLength;
    }

    public float getTripLengthInKM(){
        return tripLength/1000f;
    }

    public String getStartCity(){
        if(gpsCordsAvailable){
            return JsonFunctions.getCity(latitudeArray[0], longitudeArray[0]);
        }
        return context.getString(R.string.unknown);
    }

    public String getDestinationCity(){
        if(gpsCordsAvailable){
            final int length = latitudeArray.length-1;
            return JsonFunctions.getCity(latitudeArray[length], longitudeArray[length]);
        }
        return context.getString(R.string.unknown);
    }
    public String getStartStreet(){
        if(gpsCordsAvailable){
            return JsonFunctions.getStreet(latitudeArray[0], longitudeArray[0]);
        }
        return context.getString(R.string.unknown);
    }

    public String getDestinationStreet(){
        if(gpsCordsAvailable){
            final int length = latitudeArray.length-1;
            return JsonFunctions.getStreet(latitudeArray[length], longitudeArray[length]);
        }
        return context.getString(R.string.unknown);
    }

    public String getTripSummary() {
        int scoreArray[];
        float speedArray[];
        float tripLength, maxSpeed = 0, speedAverage = 0;
        float speedConverter = Config.getSpeedConv();
        int size = 0, nulls = 0, scoreAverage = 0;
        int durationSeconds, durationMinutes, durationHours;
        String fromCity, toCity, fromStreet, toStreet;
        String tripLengthString = "--", tripDurationString = "--";

        String speedUnit;
        String speedUnitPref = Config.getSpeedUnit();
        if      (speedUnitPref.equals("km/h")) speedUnit = context.getString(R.string.km_h);
        else if (speedUnitPref.equals("mph"))  speedUnit = context.getString(R.string.mph);
        else                                   speedUnit = context.getString(R.string.m_s);

        fromCity   = getStartCity();
        toCity     = getDestinationCity();
        fromStreet = getStartStreet();
        toStreet   = getDestinationStreet();

        scoreArray = getScoreArray();
        speedArray = getSpeedArray();
        size = speedArray.length;
        durationSeconds = (getRefreshRate() / 1000)*size;

        durationMinutes = durationSeconds / 60;
        durationHours = durationMinutes / 60;
        durationMinutes %= 60;
        durationSeconds %= 60;          // Make human readable duration from seconds.
        tripDurationString = ((durationHours > 0) ? durationHours+ "h " : "") +
                             ((durationMinutes > 0) ? durationMinutes + "m " : "") +
                               durationSeconds +"s";

        tripLength = (int)getTripLengthInMeters();
        if(Config.getSpeedUnit().equals("mph")) {
            double lengthTemp = tripLength / 1609.344; // Converting meters to miles
            lengthTemp = Double.valueOf((new DecimalFormat("#.###")).format(lengthTemp));
            tripLengthString = lengthTemp + " miles";
        }
        else if(tripLength >= 1000) {                  // Distance is 1 km +
            tripLength /= 1000f;
            tripLength = Float.valueOf((new DecimalFormat("#.###")).format(tripLength));
            tripLengthString = tripLength + " km";
        }
        else tripLengthString = tripLength + " m";

        if(size > 0) {
            for(int i = 0; i < size; i++){
                speedAverage += speedArray[i];
                scoreAverage += scoreArray[i];

                if(speedArray[i] > maxSpeed)
                    maxSpeed = speedArray[i];

                if(speedArray[i] == 0){
                    nulls++;
                }
            }
        }
        scoreAverage /= scoreArray.length;

        return buildFacebookShare(scoreAverage, tripDurationString, tripLengthString,
                          ((int)((speedAverage / (size-nulls)) * speedConverter)),
                          ((int)(maxSpeed * speedConverter)), speedUnit,
                           fromCity, fromStreet, toCity, toStreet);
    }

    /**
     * Build a trip summary string to share on Facebook.
     * @param score The average quality rating.
     * @param duration Trip duration, as a human readable string.
     * @param tripLength Trip length, as a human readable string.
     * @param avgSpeed Average speed.
     * @param maxSpeed Maximum speed.
     * @param speedUnit Speed unit to display with <code>avgSpeed</code> and <code>maxSpeed</code>.
     * @param fromCity City where the GPS-monitoring first started.
     * @param fromStreet Nearest street to where the GPS-monitoring first started.
     * @param toCity City of the last GPS-reading.
     * @param toStreet Nearest street to last GPS-reading.
     * @return A short trip summary.
     *
     * @see Config#getSpeedConv()
     * @see Config#getSpeedUnit()
     */
    public String buildFacebookShare(int score, String duration, String tripLength,
                                     int avgSpeed, int maxSpeed, String speedUnit,
                                     String fromCity, String fromStreet, String toCity, String toStreet){
        String shareMessage;

        // Include GPS-readings only when available.
        if(avgSpeed != 0)
            shareMessage = context.getString(R.string.myDrivingQuality) + " " + context.getString(R.string.between) +
                   " " + fromStreet + " (" + fromCity +") " + context.getString(R.string.and) +
                   " " + toStreet   + " (" + toCity   +") " + context.getString(R.string.was) + " " +
                   context.getString(Quality.getQualityStringId(score)).toLowerCase() + ". " +
                   "(" + context.getString(R.string.averageQuality) + " " + score + ")" + "\n" +
                   context.getString(R.string.duration) + " " + duration + "\n" +
                   context.getString(R.string.distance) + " " + tripLength + "\n" +
                   context.getString(R.string.maxSpeed) + " " +
                   ((maxSpeed != 0) ? maxSpeed : "--") + " " + speedUnit + "\n" +
                   context.getString(R.string.averageSpeed) + " " + avgSpeed + " " + speedUnit;

        else
            shareMessage = context.getString(R.string.myDrivingQuality) + " " + context.getString(R.string.was) + " " +
                   context.getString(Quality.getQualityStringId(score)).toLowerCase() + ". " +
                   "(" + context.getString(R.string.averageQuality) + " " + score + ")" + "\n" +
                   context.getString(R.string.duration) + " " + duration + "\n" +
                   "- "+context.getString(R.string.noGpsDataAvailable)+" -";

        return shareMessage;
    }

}
