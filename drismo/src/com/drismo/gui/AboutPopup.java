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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.drismo.R;
import com.drismo.model.Config;

/**
 * Create a modal window, with DriSMo (software and project) information.
 * @author The DriSMo Team
 */
public class AboutPopup extends Activity {
    /**
     * Get the window layout, and set the listener for the close button.
     * @param savedInstanceState Not beeing used, as <code>onSaveInstanceState(Bundle)</code> is not implemented.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Config.setConfigLocale(getBaseContext(), Config.getLanguageCode());
        } catch(Exception e){}
        this.setTitle(getString(R.string.about));
        setContentView(R.layout.about);

        Button closeButton = (Button)this.findViewById(R.id.closeAbout);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

}
