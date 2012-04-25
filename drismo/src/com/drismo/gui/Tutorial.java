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

package com.drismo.gui;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.*;
import com.drismo.R;
import com.drismo.model.Config;

/**
 * Tutorial activity, connected to several different layout-files.
 * The layout and content is changed according to user navigation.
 */
public class Tutorial extends Activity {
    // Current page in the tutorial. Initiated to be 1, and will be counted up/down according to navigation.
    private int currentStep = 0;
    private final int TUTORIAL_PAGES = 8;
    private final String TAG = "DriSMo Tutorial";

    /**
     * This function will change the window content when the user navigates between pages.
     * @param step The current position in the page navigation. Initial page value is 0.
     */
    private void setContent(int step){
        switch(step){
            case 0:     setContentView(R.layout.tutorial);   break;
            case 1:     setContentView(R.layout.tutorial1);  break;
            case 2:     setContentView(R.layout.tutorial2);  break;
            case 3:     setContentView(R.layout.tutorial3);  break;
            case 4:     setContentView(R.layout.tutorial4);  break;
            case 5:     setContentView(R.layout.tutorial5);  break;
            case 6:     setContentView(R.layout.tutorial6);  break;
            case 7:     setContentView(R.layout.tutorial7);  break;
            case 8:     setContentView(R.layout.tutorial8);  break;
            default:    setContentView(R.layout.tutorial);   break;
        }

        if(getResources().getConfiguration().orientation == 2) {    // Horizontal/Landscape
            RelativeLayout tutorial = (RelativeLayout) findViewById(R.id.tutorialScreen);
            tutorial.setBackgroundResource(R.drawable.bg_ls);
        }

        setTutorialFonts("eras-bold.ttf", "eras-demi.ttf");         // Fonts from assets

        final Button prevButton = (Button)this.findViewById(R.id.prevTutorScreen);
        prevButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentStep > 0) setContent(--currentStep);      // Load content for previous page
            }
        });

        final Button nextButton = (Button)this.findViewById(R.id.nextTutorScreen);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {                           // Load content for next page
                                                                    // Don't count up variable when on last page
                if(currentStep < TUTORIAL_PAGES) setContent(++currentStep);
                else finish();
            }
        });
        nextButton.requestFocus();
    }

    /**
     * Loading the content for the initial tutorial page, and set button listeners.
     * @param savedInstanceState <code>Bundle</code> containing the current step of the tutorial.
     * Note: value is null if nothing has been stored yet.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            Config.setConfigLocale(getBaseContext(), Config.getLanguageCode());
        } catch(Exception e){}
        super.onCreate(savedInstanceState);
        try{
            currentStep = savedInstanceState.getInt("pageNr");}
        catch(Exception e){
            currentStep = 0;
//            Log.i(TAG, "pageNr has not been stored yet.");
        }
        setContent(currentStep);
    }

    /**
     * If the device is flipped when navigating in the tutorial, we make sure we store the current page nr.,
     * so the same page will be loaded when the activity restarts, and the <code>savedInstanceState</code> is reloaded.
     * @param savedInstanceState <code>Bundle</code> containing the current step of the tutorial.
     * @see com.drismo.gui.Tutorial#currentStep
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("pageNr", currentStep);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Get the current page nr., in case the activity has had an (unintended) restart mid-tutorial.
     * The most common use is when the orientation change.
     * @param savedInstanceState <code>Bundle</code> to store the current step of the tutorial.
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentStep = savedInstanceState.getInt("pageNr");
    }

    /**
     * Create a font <code>(Typeface)</code> from asset, and assign it to a <code>TextView</code>.
     * @param fontName Name of the font-file in the asset-folder.
     * @param txt The <code>TextView</code> which the font will be assigned to.
     */
    public void setFont(String fontName, TextView txt) {
        Typeface font = Typeface.createFromAsset(getAssets(), fontName);
        txt.setTypeface(font);
    }

    /**
     * Going through the whole tutorial and set the fonts for each <code>TextView</code> on every page.
     * @param fontNameHeader Font name for the first <code>TextView</code> on the page.
     * @param fontNameTxt Font name for the remaining <code>TextView</code>s
     */
    private void setTutorialFonts(String fontNameHeader, String fontNameTxt) {
        // The number of TextViews for each tutorial page varies,
        //   so there needs to be conditions (instead of try-catch) to avoid NullPointer.

        setFont(fontNameHeader, (TextView)findViewById(R.id.tutorialTxt1));
        setFont(fontNameTxt, (TextView)findViewById(R.id.tutorialTxt2));

        if(currentStep == 0 || currentStep == 2 || currentStep == 5 || currentStep == 8 )
            setFont(fontNameTxt, (TextView)findViewById(R.id.tutorialTxt3));
        if(currentStep == 8) {
            SpannableString str = new SpannableString(getText(R.string.tutorialContent8_3));
            str.setSpan(new UnderlineSpan(), 0, str.length(), 0);
            ((TextView)findViewById(R.id.tutorialTxt3)).setText(str);

            setFont(fontNameTxt, (TextView)findViewById(R.id.tutorialTxt4));
            setFont(fontNameTxt, (TextView)findViewById(R.id.tutorialTxt5));
        }
        if(currentStep == TUTORIAL_PAGES) {     // On last page, change text on forward button
            ((TextView)findViewById(R.id.nextTutorScreen)).setText(getString(R.string.finish));
        }
        setFont(fontNameTxt, (TextView)findViewById(R.id.prevTutorScreen));
        setFont(fontNameTxt, (TextView)findViewById(R.id.nextTutorScreen));
    }
}
