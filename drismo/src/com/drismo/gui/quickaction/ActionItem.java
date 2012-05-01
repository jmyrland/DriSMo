package com.drismo.gui.quickaction;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

public class ActionItem {
	private Drawable icon;
	private String title;
	private OnClickListener listener;

	public ActionItem(Drawable icon, String title) {
		this.icon = icon;
        this.title = title;
	}
	public String getTitle() {
		return this.title;
	}

	public Drawable getIcon() {
		return this.icon;
	}

	public void setOnClickListener(OnClickListener listener) {
		this.listener = listener;
	}

	public OnClickListener getListener() {
		return this.listener;
	}
}