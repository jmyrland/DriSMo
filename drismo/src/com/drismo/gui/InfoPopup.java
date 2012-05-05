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

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.drismo.R;

/**
 * Create a modal window with header, content and close button.
 * The header and content is sent as extras to the intent, when this activity is started.
 */
public class InfoPopup extends BaseActivity {
    /**
     * Fill the modal window with content from the intent extras.
     * @param savedInstanceState Not beeing used, as <code>onSaveInstanceState(Bundle)</code> is not implemented.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pref_info);

        // Header and content for the modal window should be sent as extras to this activity.
        // Getting and setting them here:
        this.setTitle(getIntent().getExtras().getString("infoPopupHeader"));
        TextView infoTxt = (TextView) this.findViewById(R.id.infoTxt);
        infoTxt.setText("\n"+getIntent().getExtras().getString("infoPopupContent")+"\n");

        Button closeButton = (Button)this.findViewById(R.id.closeInfo);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

}
