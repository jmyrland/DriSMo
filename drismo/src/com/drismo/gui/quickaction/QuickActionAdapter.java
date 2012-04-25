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

package com.drismo.gui.quickaction;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.drismo.R;

/**
 * BaseAdapter class to handle and set the layout and content of the whole archive list.<br />
 * Arrays with filenames, durations and timestamp should be sorted correctly in relation to each other,
 * before they are shipped to this class.
 * @see com.drismo.gui.ViewArchive
 */
public class QuickActionAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private String[] data;
    private String[] timestamp;
    private String[] duration;
    private Context c;

    /**
     * When constructing the <code>QuickActionAdapter</code>,
     * get the <code>LayoutInflater</code> to populate the <code>ListView</code>
     * with the archive items as specified in the xml.
     * @param context Context to get the LayoutInflater from.
     */
	public QuickActionAdapter(Context context) {
        c = context;
		mInflater = LayoutInflater.from(context);
	}

    /**
     * Set the array with all the file names for the trips in the archive.
     * @param data A string array containing the file names for all the stored trips.
     */
	public void setData(String[] data) {
		this.data = data;
	}

    /**
     * Set the array with all the timestamps for the trips in the archive.
     * @param ts Array with timestamps for all the archive files.
     */
	public void setTimestamp(String[] ts) {
		this.timestamp = ts;
	}

    /**
     *  Set the array with converted duration strings for the trips in the archive.
     * @param dura Array with duration information for archive files.
     */
    public void setDuration(String[] dura) {
        this.duration = dura;
    }

    /**
     * Get the count of the data array, to determine how many trip files we have in the archive.
     * @return Trip file count
     */
	public int getCount() {
		return data.length;
	}

    /**
     * Get filename for a specific trip, based on where it is in the sort order.
     * @param item Trip position in the sort order.
     * @return Filename for the requested trip.
     */
	public Object getItem(int item) {
		return data[item];
	}

    /**
     * Unused function, but implemented as required for <code>BaseAdapter</code>.
     * @param position Integer
     * @return The parameter value
     */
	public long getItemId(int position) {
		return position;
	}

    /**
     * Build the content for each list element in the archive.
     * @param position The element's position in the list.
     * @param convertView A view holding the list item layout, filled with content.
     * @param parent The parent that this view will be attached to.
     * @return A view with the layout and data for the specific position in the list.
     */
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView	= mInflater.inflate(R.layout.archive_item, null);
			holder = new ViewHolder();
			holder.mTripName = (TextView) convertView.findViewById(R.id.listItemName);
            holder.mTripDura = (TextView) convertView.findViewById(R.id.listItemDura);
            holder.mTripDate = (TextView) convertView.findViewById(R.id.listItemDesc);

			convertView.setTag(holder);
		}
        else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mTripName.setText(data[position]);
        holder.mTripDura.setText(c.getString(R.string.tripLength)+" "+duration[position]);
        holder.mTripDate.setText(timestamp[position]);

		return convertView;
	}

    /**
     * Inner class of <code>QuickActionAdapter</code>, to store the <code>TextView</code> with the filename.
     */
	static class ViewHolder {
		TextView mTripName, mTripDura, mTripDate;
	}
}