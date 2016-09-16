package com.abhijitvalluri.android.fitnotifications.alobar.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.abhijitvalluri.android.fitnotifications.R;

/**
 * A {@link android.preference.Preference} that displays a number picker as a dialog.
 */
public class NumberPickerPreference extends DialogPreference {

    public static final int DEFAULT_MAX_VALUE = 100;
    public static final int DEFAULT_MIN_VALUE = 0;
    public static final boolean DEFAULT_WRAP_SELECTOR_WHEEL = true;

    private final int minValue;
    private final int maxValue;
    private final boolean wrapSelectorWheel;

    private NumberPicker picker;
    private TextView beforeTV, afterTV;
    private String beforeText, afterText;
    private int value;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);
        minValue = a.getInteger(R.styleable.NumberPickerPreference_minValue, DEFAULT_MIN_VALUE);
        maxValue = a.getInteger(R.styleable.NumberPickerPreference_maxValue, DEFAULT_MAX_VALUE);
        beforeText = a.getString(R.styleable.NumberPickerPreference_beforeText);
        afterText = a.getString(R.styleable.NumberPickerPreference_afterText);
        wrapSelectorWheel = a.getBoolean(R.styleable.NumberPickerPreference_wrapSelectorWheel, DEFAULT_WRAP_SELECTOR_WHEEL);
        a.recycle();
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.weight = 1;

        picker = new NumberPicker(getContext());
        picker.setLayoutParams(layoutParams);

        beforeTV = new TextView(getContext());
        beforeTV.setText(beforeText);
        beforeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f);
        beforeTV.setPadding(0,20,0,0);
        beforeTV.setGravity(Gravity.END);
        beforeTV.setTypeface(Typeface.DEFAULT_BOLD);

        afterTV = new TextView(getContext());
        afterTV.setText(afterText);
        afterTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f);
        afterTV.setPadding(0,0,0,20);
        afterTV.setGravity(Gravity.START);
        afterTV.setTypeface(Typeface.DEFAULT_BOLD);

        layoutParams.weight = 5;
        beforeTV.setLayoutParams(layoutParams);
        afterTV.setLayoutParams(layoutParams);

        LinearLayout dialogView = new LinearLayout(getContext());
        dialogView.setOrientation(LinearLayout.VERTICAL);
        dialogView.addView(beforeTV);
        dialogView.addView(picker);
        dialogView.addView(afterTV);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        picker.setMinValue(minValue);
        picker.setMaxValue(maxValue);
        picker.setWrapSelectorWheel(wrapSelectorWheel);
        picker.setValue(getValue());
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            picker.clearFocus();
            int newValue = picker.getValue();
            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, minValue);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(minValue) : (Integer) defaultValue);
    }

    public void setValue(int value) {
        this.value = value;
        persistInt(this.value);
    }

    public int getValue() {
        return this.value;
    }
}
