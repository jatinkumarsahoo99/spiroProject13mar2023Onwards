package com.safey.lungmonitoring.utils

import android.app.Activity
import androidx.appcompat.widget.Toolbar

class NavBarModel {
    var activity: Activity? = null
    var toolbar: Toolbar? = null
    var node = 0
    var max = 0
    var title: String? = null
    var iconLeft = 0
    var iconRight = 0
    var isHidden = false
    var hasError = false
}