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

/**
 * Small class to define a point, with geo location/score/color
 * @see com.drismo.gui.ViewTrip
 */
public class MapPoint {

    public final float longitude;
    public final float latitude;
    public final int color;
    public final int score;
    public final float speed;

    /**
     * Constructor used for initializing primitive values.
     *
     * @param lats Latitude
     * @param longs Longitude
     * @param colors this point's color
     * @param sco this score
     * @param sp current speed
     */
    public MapPoint( float lats, float longs, int colors, int sco, float sp) {
        latitude = lats;
        longitude = longs;
        color = colors;
        score = sco;
        speed = sp;
    }

    /**
     * Constructor used for initializing non-parsed values.
     *
     * @param lats String value of Latitude
     * @param longs String value of Longitude
     * @param colors this point's color
     * @param sco this score
     * @param sp String value of current speed
     */
    public MapPoint( String lats, String longs, int colors, int sco, String sp) {
//        latitude = stringToFloat(lats);
//        longitude = stringToFloat(longs);
//        color = colors;
//        score = sco;
//        speed = stringToFloat(sp);
        latitude = Float.parseFloat( lats );
        longitude = Float.parseFloat(longs);
        color = colors;
        score = sco;
        speed =  Float.parseFloat(sp);
    }

    /**
     * Custom float parser to optimize the creation of map points.
     * Todo: XX times faster than the <code>Float.parseFloat()</code> method.
     * @param f The string to parse.
     * @return Returns the float value of the given input.
     */
//    public static float stringToFloat(String f){
//        int totLength = f.length();
//        int commaPos = f.indexOf('.') + 1;
//
//        return Integer.parseInt(f.substring(0, commaPos-1)) +
//                (float) (Long.parseLong(f.substring(commaPos, totLength)) / (Math.pow(10, totLength-commaPos)));
//    }
}
