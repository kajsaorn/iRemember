package com.iremember.subscriber.iremembersubscriber.Fragments;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.iremember.subscriber.iremembersubscriber.R;
import com.iremember.subscriber.iremembersubscriber.Utils.PreferenceUtils;

public class SettingsBackgroundFragment extends Fragment implements View.OnClickListener{

    View mColorPalette;
    String mColorPaletteLabel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View thisFragment = inflater.inflate(R.layout.fragment_settings_background, container, false);
        mColorPalette = thisFragment.findViewById(R.id.color_palette);
        mColorPaletteLabel = getString(R.string.settings_background_label);

        setColorPaletteLabel();
        setColorPaletteListener();
        setCurrentColor();
        return thisFragment;
    }

    /**
     * Set a label on the color palette to describe what the chosen color is used for.
     */
    private void setColorPaletteLabel() {
        TextView tvLabel = mColorPalette.findViewById(R.id.label_color_palette);
        tvLabel.setText(mColorPaletteLabel);
    }

    /**
     * Set this class as on click listener to all the colors in the color palette.
     */
    private void setColorPaletteListener() {
        TableLayout colorTable = mColorPalette.findViewById(R.id.color_table);
        int row, col;

        for (row = 0; row < colorTable.getChildCount(); row++) {
            TableRow colorRow = (TableRow) colorTable.getChildAt(row);

            for (col = 0; col < colorRow.getChildCount(); col++) {
                View colorCell = colorRow.getChildAt(col);
                colorCell.setOnClickListener(this);
            }
        }
    }

    /**
     * Set the currently chosen color of the color palette.
     */
    private void setCurrentColor() {
        int color = PreferenceUtils.readBackgroundColor(this.getContext());
        Log.d("SettingsBackground", "Read current color: " + color);

        View colorView = mColorPalette.findViewById(R.id.selected_color);
        GradientDrawable background = (GradientDrawable) colorView.getBackground();
        background.setColor(color);
    }

    /**
     * Called when a color is clicked.
     */
    @Override
    public void onClick(View view) {
        int color = ((ColorDrawable)view.getBackground()).getColor();
        PreferenceUtils.writeBackgroundColor(this.getContext(), color);
        Log.d("SettingsBackground", "Write new color: " + color);
        setCurrentColor();
    }
}
