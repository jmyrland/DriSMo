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

import android.graphics.Color;
import com.drismo.R;

/**
 * A static class to model quality. Use this class to get a color to represent
 * the current quality based of a given score, and find out if a score is good/bad/ugly.
 * This class holds all the Quality constants.
 */
public class Quality {
    public static final int MAX_SCORE  = 1670;
    public static final int EXCELLENT_SCORE = 1650;
    public static final int GOOD_SCORE = 1550;
    public static final int OK_SCORE = 1400;
    public static final int MIN_SCORE  = 1250;

    public static final int EXCELLENT_COLOR = Color.rgb(12, 109, 255);
    public static final int GOOD_COLOR = Color.rgb(82,208,9);
    public static final int BAD_COLOR = Color.YELLOW;
    public static final int UGLY_COLOR = Color.RED;

    /**
     * This class is not instantiated!
     */
    private Quality(){ }

    public static int getColorFromScore(int score){
        if(score > EXCELLENT_SCORE)
           return EXCELLENT_COLOR;
        else if(score <= EXCELLENT_SCORE && score > GOOD_SCORE)
           return GOOD_COLOR;
        else if(score <= GOOD_SCORE && score > OK_SCORE)
           return BAD_COLOR;
        else
           return UGLY_COLOR;
    }

    /**
     * Used to get a color from the score. The color changes fades from red -> yellow -> green -> blue
     * @see com.drismo.gui.monitor.PrimitiveMonitor
     * @param score The score to get the color from
     * @return  Returns the color
     */
    public static int getDynamicColorFromScore(int score){
        float delta = GOOD_SCORE - score;

        if(delta > 0){              //if the score is less than good, calculate the red and green values
            return Color.rgb( (int) (delta*(155f/300f))+65,
                              (int) (delta*-(195f/300f))+195 ,
                               0);
        }else                       //the score is better than good, calculate the rgb values
            return Color.rgb( (int) (delta*(0.54f))+65,
                              (int) (delta*(0.45f))+155 ,
                              (int) (-delta*(1.4f)));
    }

    public static int getQualityStringId(int score){
        if(score > EXCELLENT_SCORE)
            return R.string.excellent;
        else if(score <= EXCELLENT_SCORE && score > GOOD_SCORE)
            return R.string.good;
        else if(score <= GOOD_SCORE && score > OK_SCORE)
            return R.string.ok;
        else
            return R.string.bad;
    }

}
