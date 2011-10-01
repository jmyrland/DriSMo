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

package com.drismo.gui.monitor;

import android.content.Context;
import android.content.Intent;
import com.drismo.model.Config;

/**
 * This class is responsible of creating intents to create monitors.
 * @see MonitorFactory#getMonitorActivityIntent(android.content.Context, int)
 */
public class MonitorFactory {
    public static int selectedMonitor = -1;

    public static final int DEBUG_MONITOR = 0;
    public static final int PRIMITIVE_MONITOR = 1;
    public static final int METER_MONITOR = 2;
    public static final int MAP_MONITOR = 3;

    /**
     * Should not be instantiated.
     */
    private MonitorFactory(){}

    /**
     * Creates and returns an intent to startMonitoring a monitor based on the given parameters.
     * @param c Context to relate to the intent.
     * @param monitor Selected monitor. See this class' static final variables.
     * @return Returns a new intent to startMonitoring a new monitor activity.
     * @see MonitorFactory#DEBUG_MONITOR
     * @see MonitorFactory#PRIMITIVE_MONITOR
     * @see MonitorFactory#METER_MONITOR
     * @see MonitorFactory#MAP_MONITOR
     */
    public static Intent getMonitorActivityIntent(Context c, int monitor) {
        selectedMonitor = monitor;
        switch (monitor) {
            case PRIMITIVE_MONITOR:
                return new Intent(c, PrimitiveMonitor.class);
            case METER_MONITOR:
                return new Intent(c, QualityMeterMonitor.class);
            case MAP_MONITOR:
                return new Intent(c, MapMonitor.class);
            default:
                return new Intent(c, DebugMonitor.class);
        }
    }

    public static Intent getSelectedMonitorActivityIntent(Context c){
        if(selectedMonitor == -1)
            selectedMonitor = Config.getPrefMonitor();

        return getMonitorActivityIntent(c, selectedMonitor);
    }
}
