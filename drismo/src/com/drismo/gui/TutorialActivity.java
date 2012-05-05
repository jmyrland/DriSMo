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

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.drismo.R;

public class TutorialActivity extends BaseActivity {
    private final String PAGE_NUMBER_KEY = "pageNr";
    private final int LAST_PAGE = 8;

    private int currentStep = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
            currentStep = savedInstanceState.getInt(PAGE_NUMBER_KEY);
        setContent(currentStep);
    }

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
        setTutorialFonts();

        final Button prevButton = (Button)this.findViewById(R.id.prevTutorScreen);
        prevButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentStep > 0) setContent(--currentStep);
            }
        });

        final Button nextButton = (Button)this.findViewById(R.id.nextTutorScreen);
        nextButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(currentStep < LAST_PAGE) setContent(++currentStep);
                else finish();
            }
        });
        nextButton.requestFocus();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(PAGE_NUMBER_KEY, currentStep);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentStep = savedInstanceState.getInt(PAGE_NUMBER_KEY);
    }

    private void setTutorialFonts() {
        setHeaderFont(getTextView(R.id.tutorialTxt1));
        setContentFont(getTextView(R.id.tutorialTxt2));
        setContentFont(getTextView(R.id.tutorialTxt3));
        setContentFont(getTextView(R.id.tutorialTxt4));
        setContentFont(getTextView(R.id.tutorialTxt5));
        setContentFont(getTextView(R.id.prevTutorScreen));
        setContentFont(getTextView(R.id.nextTutorScreen));

        if(currentStep == 8) {
            SpannableString str = new SpannableString(getText(R.string.tutorialContent8_3));
            str.setSpan(new UnderlineSpan(), 0, str.length(), 0);
            getTextView(R.id.tutorialTxt3).setText(str);
        }
        if(currentStep == LAST_PAGE)
            (getTextView(R.id.nextTutorScreen)).setText(getString(R.string.finish));
    }

    public void setHeaderFont(TextView textView) {
        Typeface font = Typeface.createFromAsset(getAssets(), "eras-bold.ttf");
        if(textView != null) textView.setTypeface(font);
    }

    public void setContentFont(TextView textView) {
        Typeface font = Typeface.createFromAsset(getAssets(), "eras-demi.ttf");
        if(textView != null) textView.setTypeface(font);
    }
}
