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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import com.drismo.R;
import com.drismo.model.Config;

import java.util.ArrayList;

/**
 * Class to catch incoming SMS-messages, and send relevant information to the <code>MessageHandler</code>.
 * @see com.drismo.logic.sms.MessageHandler
 */
public class SMSReceiver extends BroadcastReceiver {
    ArrayList<String> notifiedNumbers = MessageHandler.getNumberArray();

    /**
     * This function will run whenever an SMS-message is received.
     * It will get the PDUs, and create the SMS-message from this.
     * The senders number will be extracted, and sent to the <code>MessageHandler</code>.
     * @param context The <code>Context</code> in which the receiver is running.
     * @param intent The <code>Intent</code> being received.
     * @see com.drismo.logic.sms.MessageHandler
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        boolean alreadyNotified;

        /*  A PDU is a "protocol description unit", which is the industry format for an SMS message.
            A large message might be broken into many, which is why it is an array of objects.  */

        Object messages[] = (Object[]) bundle.get("pdus");                  // Getting the received message object.
        SmsMessage smsMessage[] = new SmsMessage[messages.length];

        smsMessage[0] = SmsMessage.createFromPdu((byte[]) messages[0]);     // Make it readable

        alreadyNotified = false;                            // Check if the sender has already received your auto-reply:
        for (String nr : notifiedNumbers) {
            if(nr.equals(smsMessage[0].getOriginatingAddress())) alreadyNotified = true;
        }

        if(!alreadyNotified) {     // Reply with preset SMS if auto-reply is enabled:
            MessageHandler.sendSMS(smsMessage[0].getOriginatingAddress(), Config.getAutoReplyMsg());
            Toast toast = Toast.makeText(context, context.getString(R.string.sentSms), Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
