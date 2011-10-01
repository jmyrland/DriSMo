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

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.drismo.R;
import com.drismo.model.Config;
import com.drismo.model.Quality;

import java.text.DecimalFormat;

/**
 * TODO: FK
 * Used to give a text presentation of the trip quality
 * @see ViewTrip
 */
public class ViewExtraInfo extends Activity {

    /**
     * Set up the layout, and fill in all the values
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            Config.setConfigLocale(getBaseContext(), Config.getLanguageCode());
        } catch(Exception e){}
        setContentView(R.layout.infoview);

        TextView avgSpeed = (TextView) findViewById(R.id.avgspeedvalue);
        TextView maxSpeed = (TextView) findViewById(R.id.maxspeedvalue);
        TextView badDur = (TextView) findViewById(R.id.badrivingvalue);
        TextView okDur = (TextView) findViewById(R.id.okdrivingvalue);
        TextView goodDur = (TextView) findViewById(R.id.gooddrivingvalue);
        TextView exelDur = (TextView) findViewById(R.id.excellentdrivingvalue);
        TextView avgScore = (TextView) findViewById(R.id.averagequalityvalue);
        TextView avgScoreText = (TextView) findViewById(R.id.averagequalitytextvalue);
        TextView distanceText = (TextView) findViewById(R.id.totaldistancevalue);
        TextView totDur = (TextView) findViewById(R.id.totaldrivingvalue);
        TextView fromLocation = (TextView) findViewById(R.id.fromLocation);
        TextView toLocation = (TextView) findViewById(R.id.toLocation);

        Bundle extras = getIntent().getExtras();

        final float[] speed = extras.getFloatArray("speed");
        final int[] score=  extras.getIntArray("score");
        final int updateS=  extras.getInt("ms")/1000;
        final float tripM = extras.getFloat("tripM");
        final float tripKM = extras.getFloat("tripKM");

        String speedUnit;
        String speedUnitPref = Config.getSpeedUnit();
        if      (speedUnitPref.equals("km/h")) speedUnit = getString(R.string.km_h);
        else if (speedUnitPref.equals("mph"))  speedUnit = getString(R.string.mph);
        else                                   speedUnit = getString(R.string.m_s);

        float speedBuff=0;
        float maxSpeedBuff =0;
        float tripLength;
        String tripLengthString;
        final int size =speed.length;
        int badCount=0;
        int okCount=0;
        int goodCount=0;
        int excellentCount=0;
        int avgBuff=0;
        int totSec = updateS * size;
        int minutes = totSec/ 60;
        int hour = minutes/60;
        minutes %= 60;
        totSec %= 60;
        int nulls=0;

        if(size >0){

            for(int i = 0; i < size; i++){
                speedBuff += speed[i];
                avgBuff += score[i];

                if(speed[i] > maxSpeedBuff)
                    maxSpeedBuff = speed[i];

                if(score[i] > Quality.EXCELLENT_SCORE)
                    excellentCount++;
                else if(score[i] <= Quality.EXCELLENT_SCORE && score[i] > Quality.GOOD_SCORE)
                    goodCount++;
                else if(score[i] <= Quality.GOOD_SCORE && score[i] > Quality.OK_SCORE)
                    okCount++;
                else
                    badCount++;

                if(speed[i] == 0){
                    nulls++;
                }
            }

            avgBuff/=size;
            avgSpeed.setText((size - nulls > 0) ? ((int) ((speedBuff / (size-nulls))* Config.getSpeedConv())) +" "+ speedUnit : "--");
            maxSpeed.setText((maxSpeedBuff > 0) ? ((int) (maxSpeedBuff * Config.getSpeedConv())) +" "+speedUnit : "--" );
            badDur.setText(getPercent(badCount,size)+"%");
            okDur.setText(getPercent(okCount, size)+"%");
            goodDur.setText(getPercent(goodCount, size)+"%");
            exelDur.setText(getPercent(excellentCount,size)+"%");

            avgScore.setText(Integer.toString(avgBuff));
            avgScore.setTextColor(Quality.getColorFromScore(avgBuff));

            avgScoreText.setText(
                    getString(Quality.getQualityStringId(avgBuff))
            );
            avgScoreText.setTextColor(Quality.getColorFromScore(avgBuff));

            tripLength = (int)tripM;
            if(Config.getSpeedUnit().equals("mph")) {
                double lengthTemp = tripLength / 1609.344;
                lengthTemp = Double.valueOf((new DecimalFormat("#.###")).format(lengthTemp));
                tripLengthString = lengthTemp + " miles";
            }
            else if(tripLength >= 1000) {
                tripLength = tripKM;
                tripLength = Float.valueOf((new DecimalFormat("#.###")).format(tripLength));
                tripLengthString = tripLength + " km";
            }
            else tripLengthString = tripLength + " m";

            if(!avgSpeed.getText().equals("--")) {
                distanceText.setText(tripLengthString);
                fromLocation.setText(extras.getString("fromStreet") + ", " + extras.getString("fromCity"));
                toLocation.setText(extras.getString("toStreet") + ", " + extras.getString("toCity"));
            }
            else {                             // No interesting GPS-readings, so we don't show those rows.
                findViewById(R.id.gpsunavailable).setVisibility(View.VISIBLE);
                findViewById(R.id.avgspeed).setVisibility(View.GONE);
                findViewById(R.id.maxspeed).setVisibility(View.GONE);
                avgSpeed.setVisibility(View.GONE);
                maxSpeed.setVisibility(View.GONE);
                distanceText.setVisibility(View.GONE);
                fromLocation.setVisibility(View.GONE);
                toLocation.setVisibility(View.GONE);
                findViewById(R.id.distanceRow).setVisibility(View.GONE);
                findViewById(R.id.fromRow).setVisibility(View.GONE);
                findViewById(R.id.toRow).setVisibility(View.GONE);
            }

            totDur.setText(((hour > 0) ? hour + "h " : "") + ((minutes > 0) ? minutes + "m " : "") + totSec + "s");
        }else
            Toast.makeText(getApplicationContext(), R.string.noInfo, Toast.LENGTH_SHORT).show(); //todo
    }

    private final int getPercent(final int x, final int total){
        return (int)(((float)x/(float) total)*100);
    }

}
