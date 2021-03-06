package com.standalone.watchdog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.standalone.watchdog.adapter.AlertAdapter;

import java.util.Objects;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {

    private final AlertAdapter adapter;

    public RecyclerItemTouchHelper(AlertAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAbsoluteAdapterPosition();
        if (direction == ItemTouchHelper.LEFT) {
            // Remove Item
            AlertDialog.Builder builder = new AlertDialog.Builder((adapter.getContext()));
            builder.setTitle(Constant.DIALOG_TITLE_DELETE);
            builder.setMessage(Constant.DIALOG_MSG_DELETE);
            builder.setPositiveButton(Constant.APPLY, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.deleteItem(position);
                }
            });
            builder.setNegativeButton(Constant.CANCEL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.notifyItemChanged(Objects.requireNonNull(viewHolder).getAbsoluteAdapterPosition());
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    adapter.notifyItemChanged(Objects.requireNonNull(viewHolder).getAbsoluteAdapterPosition());
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            // Edit Item
            adapter.editItem(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        Drawable icon;
        ColorDrawable background;

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        // Initialize icon and background
        if (dX > 0) {
            // Swiping left
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_edit);
            background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.colorPrimaryDark));
        } else {
            // Swiping right
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_delete);
            background = new ColorDrawable(ContextCompat.getColor(adapter.getContext(), R.color.danger_dark));
        }

        // align icon
        assert icon != null;
        int icMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int icTop = itemView.getTop() + icMargin;
        int icBottom = icTop + icon.getIntrinsicHeight();

        if (dX > 0) {// Swiping to the right
            int icLeft = itemView.getLeft() + icMargin;
            int icRight = icLeft + icon.getIntrinsicWidth();
            icon.setBounds(icLeft, icTop, icRight, icBottom);

            int bgRight = itemView.getLeft() + ((int) dX) + backgroundCornerOffset;

            background.setBounds(itemView.getLeft(), itemView.getTop(), bgRight, itemView.getBottom());

        } else if (dX < 0) { // Swiping to the left
            int icRight = itemView.getRight() - icMargin;
            int icLeft = icRight - icon.getIntrinsicWidth();
            icon.setBounds(icLeft, icTop, icRight, icBottom);

            int bgLeft = itemView.getRight() + ((int) dX) - backgroundCornerOffset;
            background.setBounds(bgLeft,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {// view is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }
}
