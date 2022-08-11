package com.safey.lungmonitoring.model

import android.app.Activity
import com.safey.lungmonitoring.interfaces.DialogStyle1Click

data class CustomDialogStyle1DataModel(
        val activity: Activity,
        val title: String? = null,
        val message: String? = null,
        val positiveButton: String? = null,
        val negativeButton: String? = null,
        val dialogStyle1Click: DialogStyle1Click
        )