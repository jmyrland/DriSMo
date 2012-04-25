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

package com.drismo.logic.sms;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.drismo.model.Config;

import java.util.ArrayList;

/**
 * This class reads and listens to changes in the phone state.
 */
public class MyPhoneStateListener extends PhoneStateListener {

    /**
     * The function checks if there is any incoming calls.
     * On incoming calls, reply with preset SMS if auto-reply is enabled.
     * @param state The current phone state. (CALL_STATE_IDLE = 0, CALL_STATE_RINGING = 1, CALL_STATE_OFFHOOK = 2)
     * @param incomingNumber When a phone call is received, this is the callers number.
     */
    @Override
    public void onCallStateChanged(int state, String incomingNumber){
        boolean alreadyNotified;
        ArrayList<String> notifiedNumbers = MessageHandler.getNumberArray();

        alreadyNotified = false;                            // Check if the sender has already received your auto-reply:
        for (String nr : notifiedNumbers) {
            if(nr.equals(incomingNumber)) alreadyNotified = true;
        }

    // When a call is received, the auto-reply function is called. Parameters/settings decide if a reply will be sent.
        if(state == TelephonyManager.CALL_STATE_RINGING && !alreadyNotified) {
            MessageHandler.sendSMS(incomingNumber, Config.getAutoReplyMsg());
        }
    }
}
