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

import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;
import com.drismo.model.AccelerationObject;
import com.drismo.model.Quality;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Handles quality rating and distributing updates to all the registered listeners.
 * @see QualityListener
 */
public class QualityRater extends Thread implements FilteredAccelerationListener {

    private static final int MAX_LIST_LENGTH_MS = 500;

    /**
     * Used to calculate a delta score based on changes in the force the vehicle is exposed to,
     * over a short period of time (MAX_LIST_LENGTH_MS).
     */
    private static final float DIFF_POINTS    = ((100f/3f) * 0.7f);

    /**
     * Used to calculate a delta score based on the constant force the vehicle is exposed to,
     * over a short period of time (MAX_LIST_LENGTH_MS).
     */
    private static final float CONST_POINTS = ((100f/3f) * 0.3f);

                                      //Representing vectors {  X   ,  Y  ,  Z  }
    private static final float THRESHOLD_BAD[]= new float[]  { 2.0f, 1.0f, 0.8f };
    private static final float THRESHOLD_UGLY[]= new float[] { 3.0f, 2.2f, 1.6f };
    private static final float MOTION_THRESHOLD = 0.1f;

    private int currentRating = 0;

    private volatile ArrayList<QualityListener> qualityListeners = new ArrayList<QualityListener>();

    private final LinkedList<AccelerationObject> accelerationObjectList;

    private boolean isRating;

    /**
     * Constructs the accelerationObjectList.
     */
    public QualityRater(){
        accelerationObjectList = new LinkedList<AccelerationObject>();
    }

    /**
     * Adds a quality listener and starts the thread if it is the first listener added to the list.
     * @param listener The listener to add.
     */
    public synchronized void registerQualityListener(QualityListener listener) {
        Log.d("qualityListeners", "++ added " + listener.getClass().getSimpleName());

        if(qualityListeners.size() == 0)                                          {
            Log.d("qualityListeners", "STARTED!");
            isRating = true;
            start();
        }

        if(!qualityListeners.contains(listener))
            qualityListeners.add(listener);
    }

    /**
     * Removes the specified quality listener and stops the thread if the listener list is empty.
     * @param listener The given listener to stopMonitoring.
     */
    public synchronized void unregisterQualityListener(QualityListener listener) {
        qualityListeners.remove(listener);

        Log.d("qualityListeners", "-- removed " + listener.getClass().getSimpleName() );

        if(qualityListeners.size() == 0) {
            isRating = false;
            interrupt();
            accelerationObjectList.clear();
            Log.d("qualityListeners", "STOPPED!");
        }
    }

    /**
     * On each entry an AccelerationObject (containing the vectors and timestamp) is added to
     * the <code>accelerationObjectList</code>, and deletes all the objects that are older
     * than <code>MAX_LIST_LENGTH_MS</code>.
     * @see com.drismo.model.AccelerationObject
     * @see QualityRater#MAX_LIST_LENGTH_MS
     * @param filteredVectors Not used here.
     * @param RotatedVectors This is added as the vectors in the <code>AccelerationObject</code>
     */
    public void onFilteredAccelerationChange(float[] filteredVectors, float[] RotatedVectors) {
        
        synchronized (accelerationObjectList){
            AccelerationObject current = new AccelerationObject(RotatedVectors.clone(), SystemClock.uptimeMillis());
                if(accelerationObjectList.size() > 0){
                    AccelerationObject last;

                    do{                                                  // Remove all objects which is timed out:
                       last = accelerationObjectList.removeLast();
                    }while(accelerationObjectList.size() > 0 &&
                          current.timestamp - last.timestamp > MAX_LIST_LENGTH_MS);

                    if(last != null)
                        accelerationObjectList.addLast(last);            //put back the object that was valid
                }
                accelerationObjectList.addFirst(current);
            }

    }

    /**
     * Evaluates the driving quality and updates all the listeners.
     * @see QualityRater#evaluate()
     * @see QualityListener#onQualityUpdate(int)
     */
    public void run() {
        currentRating = 1600;

        synchronized (this){

            while(isRating){                //while the rating isn't stopped

                Log.d("qualityListeners", "evaluating..");

                evaluate();                                         //Evaluate and update current score

                for(QualityListener listener : qualityListeners )   //Notify listeners:
                    listener.onQualityUpdate(currentRating);

                try {                                               //Wait half of the list length, to overlap
                    wait(MAX_LIST_LENGTH_MS/2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Evaluate the new quality rating by adding the sum of each delta score (calculated by
     * evaluating changes and constant force measured by each vector).
     */
    private void evaluate(){
        if(accelerationObjectList.size() >0){
            synchronized (accelerationObjectList){

                final AccelerationObject first = accelerationObjectList.getFirst();
                final float currentValues[] = first.rotatedVectors;

                if(currentValues[1] > MOTION_THRESHOLD || currentValues[1] < -MOTION_THRESHOLD){

                    float minValues[] = currentValues.clone();
                    float maxValues[] = currentValues.clone();

                    float diffValues[] = new float[3];
                    float deltaScore=0;
                                                                //get the max/min values, used to calculate the max difference
                    for(AccelerationObject previous : accelerationObjectList){
                        float previousValues[] = previous.rotatedVectors;
                        for(int i = 0; i < 3; i++){
                            if(minValues[i] > previousValues[i]){
                                minValues[i] = previousValues[i];
                            }else if(maxValues[i] < previousValues[i]){
                                maxValues[i] = previousValues[i];
                            }
                        }
                    }
                                                                //calculate the delta score for each axis
                    for(int i = 0; i < 3; i++){
                        diffValues[i] = maxValues[i] - minValues[i];
                                                                //calculate the delta score based on the max diff.
                        deltaScore += getDeltaScore(diffValues[i], THRESHOLD_BAD[i], THRESHOLD_UGLY[i], DIFF_POINTS);

                                                                //calculate the delta score based on the constant force.
                        if(i == 2)
                            deltaScore += getDeltaScore(currentValues[i] - SensorManager.STANDARD_GRAVITY, THRESHOLD_BAD[i], THRESHOLD_UGLY[i], CONST_POINTS);
                        else
                            deltaScore += getDeltaScore(currentValues[i], THRESHOLD_BAD[i], THRESHOLD_UGLY[i], CONST_POINTS);
                    }
                                                                //set the current score
                    currentRating += deltaScore;
                                                                //if the new score is less than min, new score == min
                    if(deltaScore < 0 && currentRating < Quality.MIN_SCORE)
                        currentRating = Quality.MIN_SCORE;
                }
            }
        }
    }

    /**
     * Get delta score based on the current state given.
     * @param vector Current acceleration vector, can be either Good, the bad or the ugly.
     * @param badThres Threshold relative to the given vector, representing bad sectors.
     * @param uglyThres Threshold relative to the given vector, representing ugly sectors.
     * @param points unit used to calculate the delta currentRating.
     * @return returns the delta score to update the current score.
     */
    public float getDeltaScore(float vector, float badThres, float uglyThres, float points){
                                                            // Calculate the delta currentRating:
        if(vector > uglyThres || vector < -uglyThres ) {        // given a ugly vector
            return calculateDeltaScore(0f, points * 3);
        }

        else if(vector > badThres || vector < -badThres ) {     // given a bad vector
            return calculateDeltaScore(0.5f, points * 2 );
        }

        else {                                                  // given a good vector
            return calculateDeltaScore(1.0f, points / 2 );
        }
    }

    /**
     * This is used to calculate a score relative to the current score and quality.
     * @param outcome this is the outcome of the current driving quality.
     * @param points unit used to calculate the delta score.
     * @return Returns the new delta score
     */
    public final float calculateDeltaScore(final float outcome, final float points){

        final float relativeScore = 1000*outcome + 500;
                                                                //calculate the expected score
        final float expectedOutcome = (float) (1 / ( 1 + Math.pow(10.0, (relativeScore - currentRating)/100.0)));

        return points * (outcome - expectedOutcome);            //return the new delta score
    }
}
