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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

//import org.openintents.sensorsimulator.hardware.Sensor;
//import org.openintents.sensorsimulator.hardware.SensorEvent;
//import org.openintents.sensorsimulator.hardware.SensorEventListener;
//import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;

import java.util.*;

/**
 * This object handles everything regarding collecting and filtering acceleration data. To listen for filtered
 * acceleration data, implement the <code>FilteredAccelerationListener</code> interface.
 * @see com.drismo.logic.FilteredAccelerationListener
 */
public class AccelerationHandler implements SensorEventListener {

    private SensorManager sensorManager = null;
//    private SensorManagerSimulator sensorManager = null;

    private double pitch;
    private double roll;
    private double yaw;

    private MAQueue maQueue;
    private final ArrayList<FilteredAccelerationListener> filteredAccelerationListeners = new ArrayList<FilteredAccelerationListener>();

    /**
     * Set up the sensor manager and the moving average queue.
     * @param sm The sensor manager used to register a accelerometer data listener.
     */
    public AccelerationHandler(SensorManager sm){
//    public AccelerationHandler(SensorManagerSimulator sm){
        sensorManager = sm;
        maQueue = new MAQueue();
    }


    /**
     * Start listening for acceleration data.
     */
    public void startListening() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManagerSimulator.SENSOR_DELAY_FASTEST);
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * Stop listening for acceleration data.
     */
    public void stopListening() {
        sensorManager.unregisterListener(this);
    }

    /**
     * Add an object to listen for filtered acceleration data.
     * @param listener The listener to add.
     */
    public void registerFilteredAccelerationListener(FilteredAccelerationListener listener){
        if(filteredAccelerationListeners.size() == 0){
            startListening();
        }

        if(!filteredAccelerationListeners.contains(listener))
            filteredAccelerationListeners.add(listener);
    }

    /**
     * Remove an added listener from the list.
     * @param listener The listener to remove.
     */
    public void unregisterFilteredAccelerationListener(FilteredAccelerationListener listener){
        filteredAccelerationListeners.remove(listener);

        if(filteredAccelerationListeners.size() == 0){
            stopListening() ;
        }
    }

    /**
     * Filters the incoming acceleration data using the WMA or EMA algorithm, and fires the
     * callback method of all the listeners.
     * @param event Sensor event. Only using Sensor.TYPE_ACCELEROMETER.
     * @see FilteredAccelerationListener#onFilteredAccelerationChange(float[], float[])
     */
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();
//        int sensorType = event.type;
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            maQueue.put(event.values.clone());
            float[] filteredAcceleration = maQueue.getEMAValues();

            /**
             * If vectors are noise reduced, and listeners is registered:
             */
            if(filteredAcceleration != null && filteredAccelerationListeners.size() > 0){

                float[] rotatedAcceleration = filteredAcceleration.clone();
                rotateAll(rotatedAcceleration);

                for(FilteredAccelerationListener listener : filteredAccelerationListeners)
                    listener.onFilteredAccelerationChange(filteredAcceleration, rotatedAcceleration);
            }

        }

    }

    /**
     * Updates the pitch angle relative to the vehicle.
     * @param YZ The new angle (radians)
     */
    public void updatePitchAngle(double YZ){
        pitch = YZ;
    }

    /**
     * Updates the yaw angle relative to the vehicle.
     * @param XY The new angle (radians)
     */
    public void updateYawAngle(double XY){
        yaw = XY;
    }

    /**
     * Updates the roll angle relative to the vehicle.
     * @param XZ The new angle (radians)
     */
    public void updateRollAngle(double XZ){
        roll = XZ;
    }

    /**
     * Rotates all the acceleration vectors given as parameter, according to the <code>roll</code>,
     * <code>pitch</code> and <code>yaw</code> angles.
     * @param vectors The acceleration vectors
     */
    public synchronized void rotateAll(float[] vectors){
        rotate(roll,  0, 2, vectors);
        rotate(pitch, 1, 2, vectors);
        rotate(yaw,   0, 1, vectors);
    }

    /**
     * Rotates two vectors using polar rotation, using each vector as a coordinate.
     * @param radAngle The rotation angle in radians.
     * @param indexX Index of the X coordinate
     * @param indexY Index of the Y coordinate
     * @param vectors Acceleration vector array.
     */
    public static void rotate(double radAngle, int indexX, int indexY, float[] vectors){
        float tempY = vectors[indexY];
        vectors[indexY] = (float) (vectors[indexX] * Math.sin(radAngle) +  vectors[indexY]  * Math.cos(radAngle));
        vectors[indexX] = (float) (vectors[indexX] * Math.cos(radAngle) - tempY * Math.sin(radAngle));
    }

    /**
     * An inner static class used to remove noise from the acceleration values. Keeps a simple FIFO queue of N
     *  acceleration values, used to filter out noise using the Weighted/Exponential Moving Average algorithm.
     */
    private static final class MAQueue {

        /**
         * N elements in the queue.
         */
        private final int N = 31;

        /**
         * The queue of vectors.
         */
        private LinkedList<float[]> list = new LinkedList<float[]>();

        /**
         * Puts the acceleration vector in the FIFO queue, and removes excessive vectors.
         * @param v The XYZ acceleration vectors.
         */
        public synchronized void put(float v[]) {
            list.addLast(v);
            if(list.size() > N)
                list.removeFirst();
        }

        /**
         * Calculates the WMA value of the element i in the queue, based on the <b>Weighted Moving Average</b> algorithm.
         * @return The weighted moving average value of the element i.
         */
        public float[] getWMAValues(){
            float returnValue[] = new float[3];

            if(list.size() < N)   {                                     // If the list is not complete, we
                return null;                                            //   can't calculate the moving average.
            }

            else{                                                       // If enough elements to get calculate WMA:
                int i = (N / 2) + (N % 2);                              //  get the current element position

                for(int vector = 0; vector < 3; vector++){              // For each acceleration vector:

                    int denominator = 0;
                    float avgSum = 0;

                    for(int j = -(N / 2); j < i; j++){                  // Go from element i-(N/2) to i+(N/2):
                        int multiplier = (j < 0) ? i + j : i - j;           // Get the multiplier / weight
                        denominator += multiplier;                          // Add multiplier to the denominator
                        avgSum += multiplier * list.get((i+j)-1)[vector];   // Add the weighted element to the avg sum
                    }

                    returnValue[vector] = avgSum / denominator;         // Calculate the WMA of this vector.
                }
            }

            return returnValue;
        }

        /**
         * Calculates the EMA value of the element i in the queue, based on the <b>Exponential Moving Average</b> algorithm.
         * @return The exponential moving average value of the element i.
         */
        private synchronized float[] getEMAValues(){
            float returnValue[] = new float[3];

            if(list.size() < N)   {                                     // If the list is not complete, we
                return null;                                            //   can't calculate the moving average.
            }

            else{                                                       // If enough elements to get calculate EMA:
                int i = (N / 2) + (N % 2);                              //  get the current element position

                for(int vector = 0; vector < 3; vector++){              // For each acceleration vector:

                    int denominator = 0;
                    float avgSum = 0;
                    int multiplier = 0;

                    for(int j = -(N / 2); j < i; j++){                  // Go from element i-(N/2) to i+(N/2):
                        multiplier = (j < 0) ? N/(1+-j) : N/(j+1) ;         // Calculate the exponential multiplier
                        denominator += multiplier;                          // Add multiplier to the denominator
                        avgSum += multiplier *  list.get((i+j)-1)[vector];  // Sum the exponential weighted elements
                    }

                    returnValue[vector] = avgSum / denominator;         // Calculate the EMA of this vector.
                }
            }
            return returnValue;
        }
    }

    /** Never used */
    public void onAccuracyChanged(Sensor sensor, int i) { }
}
