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
 * Class to model acceleration at a given time. Contains 3 vectors and a time stamp.
 * Used in <code>AccelerationHandler</code> and <code>QualityRater</code>
 * @see com.drismo.logic.AccelerationHandler
 * @see com.drismo.logic.QualityRater
 */
public class AccelerationObject {

    /**
     * Public data attribute to represent time stamp of the object. Getter method not needed.
     */
    public final long timestamp;

    /**
     * Public data attribute to represent the three vectors of the object. Getter method not needed.
     */
    public final float rotatedVectors[];

    /**
     * Creates an acceleration object.
     * @param rot Rotated acceleration vectors.
     * @param ts Time stamp given.
     */
    public AccelerationObject(float rot[], long ts){
        timestamp = ts;
        rotatedVectors = rot;
    }
}
