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
 * Used by objects who listens for noise reduced acceleration. Listeners of this type is added to, and
 * updated by the <code>AccelerationHandler</code>.
 * @see AccelerationHandler
 */
public interface FilteredAccelerationListener {
    /**
     * Fires whenever there is a new acceleration entry.
     * @param filteredAccelerationValues Noise reduced acceleration vectors.
     * @param rotatedAccelerationValues Same as the <code>filteredAccelerationValues</code>, only rotated relative to
     *         the vehicle (if the tilt angles is set).
     */
    public void onFilteredAccelerationChange(float filteredAccelerationValues[], float[] rotatedAccelerationValues);
}
