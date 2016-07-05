package views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

import com.motthoidecode.findplacesnearby.R;

import utils.Util;

/**
 * Created by Ran on 21/05/2016.
 */

public class NumberPickerPreferences extends DialogPreference {

    private int radius;
    private NumberPicker radiusPick;
    private int DEFAULT_VALUE;

    public NumberPickerPreferences(Context context, AttributeSet attrs) {
        super(context, attrs);

        DEFAULT_VALUE = 5;

        setDialogLayoutResource(R.layout.number_picker_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            persistInt(radiusPick.getValue());
            this.setSummary(getSharedPreferences().getInt(Util.KEY_RADIUS_PICKER,0) + " Km");
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            radius = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            radius = (Integer) defaultValue;
            persistInt(radius);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index,  DEFAULT_VALUE);
    }

    @Override
    protected View onCreateDialogView() {

        int max = 10;
        int min = 1;

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_dialog, null);

        radiusPick = (NumberPicker) view.findViewById(R.id.radius_picker);

        // Initialize state
        radiusPick.setMaxValue(max);
        radiusPick.setMinValue(min);
        radiusPick.setValue(this.getPersistedInt(DEFAULT_VALUE));
        radiusPick.setWrapSelectorWheel(false);

        return view;
    }



    //  This code copied from android's settings guide.

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readInt();  // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeInt(value);  // Change this to write the appropriate data type
        }

        // Standard creator object using an instance of this class
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }



    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        // Check whether this Preference is persistent (continually saved)
        if (isPersistent()) {
            // No need to save instance state since it's persistent, use superclass state
            return superState;
        }

        // Create instance of custom BaseSavedState
        final SavedState myState = new SavedState(superState);
        // Set the state's value with the class member that holds current setting value
        myState.value = radius;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // Set this Preference's widget to reflect the restored state
        radiusPick.setValue(myState.value);
    }
}
