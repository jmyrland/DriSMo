package com.drismo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.drismo.R;
import com.drismo.utils.FileController;

import java.util.Comparator;
import java.util.List;

public class ArchiveListAdapter extends ArrayAdapter<String> {

    private Context context;
    private FileController controller;
    private LayoutInflater layoutInflater;

    public ArchiveListAdapter(Context context, FileController controller) {
        super(context, 0, controller.listFiles());
        this.context = context;
        this.controller = controller;
        layoutInflater = LayoutInflater.from(context);
    }

    public void addTrips(List<String> trips) {
        if(trips != null) {
            for(String trip : trips) {
                add(appendFileExtensionIfNecessary(trip));
            }
        }
    }

    private String appendFileExtensionIfNecessary(String trip) {
        if(!trip.endsWith(FileController.FILE_EXTENSION))
            trip += FileController.FILE_EXTENSION;
        return trip;
    }

    @Override
    public void sort(Comparator<? super String> comparator) {
        super.sort(comparator);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView	= layoutInflater.inflate(R.layout.archive_item, null);
            holder = new ViewHolder();
            holder.tripNameView = (TextView) convertView.findViewById(R.id.archiveItemName);
            holder.tripDurationView = (TextView) convertView.findViewById(R.id.archiveItemDuration);
            holder.tripDateView = (TextView) convertView.findViewById(R.id.archiveItemDescription);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tripNameView.setText(getItem(position));
        holder.tripDurationView.setText(getTripDuration(getItem(position)));
        holder.tripDateView.setText(controller.getTimestamp(getItem(position)));

        return convertView;
    }

    static class ViewHolder {
        TextView tripNameView;
        TextView tripDurationView;
        TextView tripDateView;
    }


    public String getTripDuration(String fileName) {
        return formattedDuration(controller.getSecondDurationFromTripFile(fileName));
    }

    private String formattedDuration(int seconds) {
        int minutes  = seconds / 60;
        int hours    = minutes / 60;
        minutes %= 60;
        seconds %= 60;

        String formattedDuration = ((hours > 0)   ? hours   + "h " : "") +
                                   ((minutes > 0) ? minutes + "m " : "") +
                                     seconds + "s";
        return context.getString(R.string.tripLength) + " " + formattedDuration;
    }
}
