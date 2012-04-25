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
 * Used by objects who listens for changes in calibration, when the calibration is initiated. These changes is
 * fired from <code>Calibration</code>, where the listener is added.
 * @see Calibration
 */
public interface CalibrationListener {
    /**
     * This method is fired when the offset calibration is completed. This means that we got the "starting-point" to
     * detect the driving direction, in addition to the calibrated <code>roll</code> and <code>pitch</code> angles.
     */
    public abstract void onOffsetCalculationComplete();

    /**
     * This method is fired when the calibration is done, which means that we got valid <code>yaw</code>,
     * <code>roll</code> and <code>pitch</code> angles (we successfully can rotate the acceleration).
     * vectors.
     */
    public abstract void onCalibrationCompleted();
}
