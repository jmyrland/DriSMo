package com.drismo.gui.quickaction;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import com.drismo.R;

public class QuickActionMenu {

    private QuickAction quickAction;
    private String fileName;

    private ActionItem viewAction;
    private ActionItem renameAction;
    private ActionItem deleteAction;
    private ActionItem facebookAction;
    private ActionItem exportAction;

    public QuickActionMenu(final Context context) {
        viewAction = new ActionItem(
            context.getResources().getDrawable(R.drawable.ic_menu_qa_graph),
            context.getString(R.string.view)
        );
        renameAction = new ActionItem(
            context.getResources().getDrawable(R.drawable.ic_menu_qa_pencil),
            context.getString(R.string.rename)
        );
        deleteAction = new ActionItem(
            context.getResources().getDrawable(R.drawable.ic_menu_qa_delete),
            context.getString(R.string.delete)
        );
        facebookAction = new ActionItem(
            context.getResources().getDrawable(R.drawable.ic_menu_qa_facebook),
            context.getString(R.string.share)
        );
        exportAction = new ActionItem(
            context.getResources().getDrawable(R.drawable.ic_menu_qa_upload),
            context.getString(R.string.export)
        );
    }

    public void setViewListener(View.OnClickListener listener) {
        viewAction.setOnClickListener(listener);
    }

    public void setRenameListener(View.OnClickListener listener) {
        renameAction.setOnClickListener(listener);
    }

    public void setDeleteListener(View.OnClickListener listener) {
        deleteAction.setOnClickListener(listener);
    }

    public void setFacebookListener(View.OnClickListener listener) {
        facebookAction.setOnClickListener(listener);
    }

    public void setExportListener(View.OnClickListener listener) {
        exportAction.setOnClickListener(listener);
    }

    public void showQuickActionsForItem(View listItem) {
        quickAction = new QuickAction(listItem);

        final ImageView listItemIcon = (ImageView) listItem.findViewById(R.id.archiveItemIcon);
        listItemIcon.setImageResource(R.drawable.img_archive_d_highlighted);

        quickAction.addActionItem(viewAction);
        quickAction.addActionItem(renameAction);
        quickAction.addActionItem(deleteAction);
        quickAction.addActionItem(facebookAction);
        quickAction.addActionItem(exportAction);
        quickAction.setAnimStyle(QuickAction.ANIM_AUTO);

        quickAction.setOnDismissListener(new PopupWindow.OnDismissListener() {
            public void onDismiss() {
                listItemIcon.setImageResource(R.drawable.img_archive_d);
            }
        });
        quickAction.show();
    }

    public void dismiss() {
        quickAction.dismiss();
    }
}
