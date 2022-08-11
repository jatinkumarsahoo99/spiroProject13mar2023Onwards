package com.safey.lungmonitoring.customviews

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet

class CustomButtonCircularStd:androidx.appcompat.widget.AppCompatButton {
    constructor(context: Context) : super(context) {
        initTypeface(context)
    }
    constructor(context: Context,
                attrs: AttributeSet) : super(context, attrs) {
        initTypeface(context)
    }
    constructor(context: Context,
                attrs: AttributeSet,
                defStyleAttr:Int) : super(context, attrs, defStyleAttr) {
        initTypeface(context)
    }
    private fun initTypeface(context: Context) {
        val tf = Typeface.createFromAsset(
                context.assets,
                "fonts/circularstd_medium.otf")
        this.typeface = tf
    }
}