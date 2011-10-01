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

/**
 * Calibrates the (roll, pitch and yaw) angles of the device <b>RELATIVE</b> to the cars position and driving direction.
 * By calibrating we can rotate the acceleration vectors relative to the car, which means we can use these vectors
 * to analyze and rate the quality of driving.
 */
public class Calibration extends Thread implements FilteredAccelerationListener {

    private AccelerationHandler accHandler;
    private CalibrationListener calibrationListener;

    private double xyMagnitudeOffset = 0;

    private static final int N = 25;                        // The average factor.

    private static final double DRIVING_THRESHOLD = 0.5;    // Used to determine if we are driving.

    private boolean levelCalibrated = false;
    private boolean offsetFound = false;

    private double averageBuffer = 0;                       // Some buffer variables used to calculate average.
    private int averageCounter = 0;

    /**
     * Stores the acceleration handler and the calibration listener. Starts the calibration thread.
     *
     * @param ah The acceleration handler.
     * @param cl The object that listens for changes in calibration.
     */
    public Calibration(AccelerationHandler ah, CalibrationListener cl) {
        accHandler = ah;
        calibrationListener = cl;
        this.start();
    }

    /**
     * Waits 1 second to reduce touch vibration/noise, then initiates the calibration.
     */
    public synchronized void run() {
        try {
                                                // Waits 1 second to remove excessive touch vibration/noise.
            wait(1000);
                                                // Registers this object to listen for filtered acceleration values.
            accHandler.registerFilteredAccelerationListener(this);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels the calibration.
     */
    public void cancel() {
        accHandler.unregisterFilteredAccelerationListener(this);        // Unregister this object and
        this.interrupt();                                               //   interrupt the running thread.
    }


    /**
     * Computes the roll, pitch and yaw angles relative to the car. This means that (by rotating the acceleration
     * vectors) we can detect turns in the X axis, acceleration in the Y axis and bumps in the Z axis.
     *
     * Noise reduced acceleration values is given from the AccelerationHandler object, on change.
     *
     * @param filteredVectors The original weighted moving average acceleration vectors (X,Y,Z).
     * @param rotatedVectors The rotated weighted moving average acceleration vectors (X,Y,Z).
     * @see AccelerationHandler#onSensorChanged(android.hardware.SensorEvent)
     */
    public synchronized void onFilteredAccelerationChange(float[] filteredVectors, float[] rotatedVectors) {

        if(!offsetFound){                           // If driving offset not found, we need to find it.

            if(!levelCalibrated ){                                   // If we just started calibrating
                /** We need to find the ROLL and PITCH and rotate the given values. */
                double roll = StrictMath.atan2(filteredVectors[0], filteredVectors[2]);
                AccelerationHandler.rotate(roll, 0, 2, filteredVectors);
                double pitch = StrictMath.atan2(filteredVectors[1], filteredVectors[2]);
                AccelerationHandler.rotate(pitch, 1, 2, filteredVectors);

                accHandler.updateRollAngle(roll);                            // Update tilt angles:
                accHandler.updatePitchAngle(pitch);
                accHandler.updateYawAngle(0);

                levelCalibrated = true;
            }else{                                                   // Level is calibrated, find the XY offset:

                double magnitude = Math.hypot(rotatedVectors[0], rotatedVectors[1]);

                if(averageCounter == 0 ||                                         // If this is the first entry OR
                        ( magnitude < (averageBuffer/averageCounter) + 0.05  &&   // if the new magnitude is relatively
                          magnitude > (averageBuffer/averageCounter) - 0.05 ) ){  //   the same value as the last one:

                    averageBuffer += magnitude;                                     // Sum the magnitude of XY:
                    averageCounter++;

                    if(averageCounter >= N){                                         // If we have enough values to get the avg:
                        xyMagnitudeOffset = averageBuffer / averageCounter;             // Get the average offset.
                        averageBuffer = 0;                                              // Reset the average variables:
                        averageCounter = 0;
                        offsetFound = true;                                             // We found the offset!

                        calibrationListener.onOffsetCalculationComplete();              // Update the listener.

                    }                                                // If the new magnitude was not relatively like,
                }else{                                               //  the vehicle is in motion!
                    levelCalibrated = false;                                        // Reset the level calibration.
                    averageBuffer = 0;                                              // Reset the average variables:
                    averageCounter = 0;
                }
            }

        } else {                                    // If we got the XY magnitude, we can detect the driving direction

                                                                    // Get the magnitude of X and Y minus the offset.
            double drivingDirectionMagnitude = Math.hypot(rotatedVectors[0], rotatedVectors[1]) - xyMagnitudeOffset;

            if( drivingDirectionMagnitude > DRIVING_THRESHOLD){     // If true, we are in motion:

                                                                    // Add the new yaw angle in the avg buffer:
                averageBuffer += Math.atan2(rotatedVectors[0], rotatedVectors[1]);
                averageCounter++;

                if(averageCounter > N){                             // If we have enough angles to calculate the direction:
                    double yaw = averageBuffer / averageCounter;        // Get the average yaw angle, and update
                    accHandler.updateYawAngle(yaw);                     //  the acceleration handler.

                    accHandler.unregisterFilteredAccelerationListener(this);    // We are done calibrating, so we unregister.
                    calibrationListener.onCalibrationCompleted();               // Update the listener.
                }

            }
        }
    }

}
