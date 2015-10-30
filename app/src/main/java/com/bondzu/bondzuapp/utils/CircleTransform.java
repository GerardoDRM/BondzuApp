package com.bondzu.bondzuapp.utils;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

/**
 * Created by gerardo on 30/08/15.
 */
public class CircleTransform implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        return ImageUtils.getCircularBitmapImage(source);
    }

    @Override
    public String key() {
        return "circle-img";
    }
}
