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

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import com.drismo.model.Config;
import com.drismo.model.Quality;

import java.util.Locale;
import java.util.Random;

/**
 * TODO: Kommenter JAM
 */
public class QualityToSpeech implements TextToSpeech.OnInitListener, QualityListener {
    private static final String TAG = "QTS";

    private TextToSpeech mTts;

    private Context context;

    private int frequency = 0;

    private long lastSpoken = 0;

    private long sumScore = 0;
    private int scoreCounter = 0;

    QualityToSpeech(Context c){
        context = c;
        frequency = Config.getTtsMinuteFrequency() * 60000;
        mTts = new TextToSpeech(context, this);
    }

    /**
     * Implemented method to initiate text to speech.
     * @param status Given status.
     */
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.UK);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Lanuage data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.

                // The TTS engine has been successfully initialized.
                mTts.setPitch(1.6f);
                mTts.setSpeechRate(1.05f);

                speak("Calibration started");
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    public void shutdown(){
        mTts.shutdown();
    }

    public void speak(String message){
        mTts.speak(message,
            TextToSpeech.QUEUE_FLUSH,  // Drop all pending entries in the playback queue.
            null);
    }

    private static final Random RANDOM = new Random();
    private static final String[] MESSAGE = {
      "You have been driving ?",
      "Your average driving quality is ? at the moment",
      "Nobody is driving as ? as you do right now",
      "Who knew that driving could be this ?",
      "This is ? driving!",
      "You are driving really ?",
      "You are ? at driving.",
      "You're driving skills are pretty ?",
      "I have never seen such an ? driver before",
    };

    /**
     *
     * @param newScore The new/updated quality score.
     */
    public void onQualityUpdate(int newScore) {
        sumScore += newScore;
        scoreCounter++;

        if(lastSpoken == 0){
            lastSpoken = System.currentTimeMillis();
        }else if(scoreCounter > 0 && lastSpoken + frequency < System.currentTimeMillis()){
            lastSpoken = System.currentTimeMillis();
            int avgScore = (int) (sumScore / scoreCounter);

            String quality = "BAD";

            if(avgScore > Quality.EXCELLENT_SCORE)
                quality = "EXCELLENT";
            else if(avgScore <= Quality.EXCELLENT_SCORE && avgScore > Quality.GOOD_SCORE)
                quality = "GOOD";
            else if(avgScore <= Quality.GOOD_SCORE && avgScore > Quality.OK_SCORE)
                quality = "OK";

            String avgQualityString = MESSAGE[RANDOM.nextInt(MESSAGE.length)].replace(
                    "?",
                    quality
            );

            speak( avgQualityString );

            sumScore = scoreCounter = 0;
        }
    }
}
