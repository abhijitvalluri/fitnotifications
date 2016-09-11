package com.abhijitvalluri.android.fitnotifications.setup;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhijitvalluri.android.fitnotifications.R;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;

/**
 * Created by Abhijit Valluri on 9/11/2016.
 */
public class CustomSlideFragment extends SlideFragment {

    private boolean mCanGoForward;
    private boolean mCanGoBackward;

    private ImageView mImageView;
    private TextView mTitleTV;
    private TextView mDescriptionTV;

    private int mTitleResId;
    private int mDescResId;
    private int mImageResId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_intro, container, false);
        mImageView = (ImageView) v.findViewById(R.id.mi_image);
        mTitleTV = (TextView) v.findViewById(R.id.mi_title);
        mDescriptionTV = (TextView) v.findViewById(R.id.mi_description);
        setupViews();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupViews();
    }

    @Override
    public boolean canGoForward() {
        return mCanGoForward;
    }

    @Override
    public boolean canGoBackward() {
        return mCanGoBackward;
    }

    private void setupViews() {
        if (mImageResId != 0) {
            mImageView.setImageResource(mImageResId);
        }

        if (mTitleResId != 0) {
            mTitleTV.setText(mTitleResId);
            mTitleTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        }

        if (mDescResId != 0) {
            mDescriptionTV.setText(mDescResId);
            mDescriptionTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
    }

    public CustomSlideFragment setCanGoForward(boolean canGoFwd) {
        mCanGoForward = canGoFwd;
        return this;
    }

    public CustomSlideFragment setCanGoBackward(boolean canGoBwd) {
        mCanGoBackward = canGoBwd;
        return this;
    }

    public CustomSlideFragment setImage(@DrawableRes int resId) {
        mImageResId = resId;
        return this;
    }

    public CustomSlideFragment setTitleText(@StringRes int resId) {
        mTitleResId = resId;
        return this;
    }

    public CustomSlideFragment setDescriptionText(@StringRes int resId) {
        mDescResId = resId;
        return this;
    }
}
