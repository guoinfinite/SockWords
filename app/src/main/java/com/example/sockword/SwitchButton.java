package com.example.sockword;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class SwitchButton extends FrameLayout {
    private ImageView open;
    private ImageView close;

    public SwitchButton(Context context) {
        super(context, null);
    }

    public SwitchButton(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet);
    }

    public SwitchButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.SwitchButton);
        Drawable openDrawable = typedArray.getDrawable(R.styleable.SwitchButton_switchOpenImage);
        Drawable closeDrawable = typedArray.getDrawable(R.styleable.SwitchButton_switchCloseImage);

        int switchStatus = typedArray.getInt(R.styleable.SwitchButton_switchStatus,0);
        typedArray.recycle();

        LayoutInflater.from(context).inflate(R.layout.switch_button, this);

        open = (ImageView) findViewById(R.id.iv_switch_open);
        close = (ImageView) findViewById(R.id.iv_switch_close);

        if (openDrawable != null) {
            open.setImageDrawable(openDrawable);
        }
        if (closeDrawable != null) {
            close.setImageDrawable(closeDrawable);
        }

        if (switchStatus == 1) {
            closeSwitch();
        }
    }

    public boolean isSwitchOpen() {
        return open.getVisibility() == View.VISIBLE;
    }

    public void openSwitch() {
        open.setVisibility(View.VISIBLE);
        close.setVisibility(View.INVISIBLE);
    }

    public void closeSwitch() {
        open.setVisibility(View.INVISIBLE);
        close.setVisibility(View.VISIBLE);
    }
}
