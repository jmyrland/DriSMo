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

import android.telephony.SmsManager;
import java.util.ArrayList;

/**
 * This class is sending out the SMS-messages, and making sure the receivers doesn't get duplicate messages.
 */
public class MessageHandler {
    private static SmsManager SM = SmsManager.getDefault();
    private static ArrayList<String> notifiedNumbers = new ArrayList<String>();  // All numbers that have received
                                                                            //  the current auto-sms.
    /**
     * Add a new phone number to the notified list.
     * @param nr Phone number to add to the notified list.
     */
    public static void addNumber(String nr) {
       if(!notifiedNumbers.contains(nr)) notifiedNumbers.add(nr);
    }

    /**
     * Clear the notified numbers list.
     */
    public static void clearNumbers() {
       notifiedNumbers.clear();
    }

    /**
     * Get the list of already notified numbers.
     * @return <code>ArrayList</code> with all the numbers.
     */
    public static ArrayList<String> getNumberArray() {
       return notifiedNumbers;
    }

    /**
     * Send an SMS-message to a specified receiver.
     * @param recipient The phone number to send the message to.
     * @param message The message content.
     */
    public static void sendSMS(String recipient, String message) {
        if(message.length() > 160) {                                // 160 chars = max. length of single part sms.
           message = message.substring(0, 160);
        }
        MessageHandler.addNumber(recipient);                        // Adding number to the notified-list,
                                                                    //  to avoid duplications.
        SM.sendTextMessage(recipient, null, message, null, null);   // Sending single part message.
    }

}
