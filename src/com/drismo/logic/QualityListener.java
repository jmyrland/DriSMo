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
 * Used by objects who listens for quality updates. Listeners of this type is added to, and
 * updated by the <code>QualityRater</code>.
 * @see QualityRater
 * @see com.drismo.model.Quality
 */
public interface QualityListener {
    /**
     * Fired whenever the quality score is updated. Control the given input by using the <code>Quality</code> class.
     * @param newScore The new/updated quality score.
     * @see com.drismo.model.Quality
     */
    public abstract void onQualityUpdate(int newScore);
}
