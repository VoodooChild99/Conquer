package com.example.logindemo.ui.login;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EditTextDefense extends androidx.appcompat.widget.AppCompatEditText {


    public EditTextDefense(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void sendAccessibilityEvent(int eventType) {
        return;
    }
}
