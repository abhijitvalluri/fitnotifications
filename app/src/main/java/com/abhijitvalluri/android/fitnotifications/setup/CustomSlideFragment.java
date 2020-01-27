/*
   Copyright 2017 Abhijit Kiran Valluri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.abhijitvalluri.android.fitnotifications.setup;

import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhijitvalluri.android.fitnotifications.R;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;

/**
 * Custom Slide Fragment for the app intro.
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
            mTitleTV.setTextColor(ContextCompat.getColor(this.getContext(),
                    com.heinrichreimersoftware.materialintro.R.color.mi_text_color_primary_dark));
        }

        if (mDescResId != 0) {
            mDescriptionTV.setText(mDescResId);
            mDescriptionTV.setTextColor(ContextCompat.getColor(this.getContext(),
                    com.heinrichreimersoftware.materialintro.R.color.mi_text_color_secondary_dark));
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
