package com.safey.lungmonitoring.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.*
import com.safey.lungmonitoring.databinding.DialogDeleteBinding
import com.safey.lungmonitoring.databinding.DialogSuccessDashboardBinding
import com.safey.lungmonitoring.model.CustomDialogStyle1DataModel
import android.view.WindowManager
import com.safey.lungmonitoring.databinding.DialogProgressBinding
import com.safey.lungmonitoring.databinding.DialogShareFinishtestBinding


inline fun <T : View> T.afterMeasured(crossinline fn: (Int, Int) -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                fn(measuredWidth, measuredHeight)
            }
        }
    })
}

object CustomDialogs {
    @SuppressLint("InflateParams")
    fun dialogStyleDelete(model: CustomDialogStyle1DataModel): Boolean {
        try {
            val binding = DialogDeleteBinding.inflate(LayoutInflater.from(model.activity),
                model.activity.window.decorView.rootView as ViewGroup, false)


            if (model.message != null) {
                binding.txtmessage.text = model.message
                binding.txtmessage.visibility = View.VISIBLE
            } else {
                binding.txtmessage.visibility = View.GONE
            }

            if (model.positiveButton != null) {
                binding.safeyNextCancelButton.buttonNext.text = model.positiveButton
                binding.safeyNextCancelButton.buttonNext.visibility = View.VISIBLE
            } else {
                binding.safeyNextCancelButton.buttonNext.visibility = View.GONE
            }
            if (model.negativeButton != null) {
                binding.safeyNextCancelButton.buttonCancel.text = model.negativeButton
                binding.safeyNextCancelButton.buttonCancel.visibility = View.VISIBLE
            } else {
                binding.safeyNextCancelButton.buttonCancel.visibility = View.GONE
            }
            val dialog = Dialog(model.activity)
            dialog.setContentView(binding.root)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)

            binding.safeyNextCancelButton.buttonNext.setOnClickListener {
                dialog.dismiss()
                model.dialogStyle1Click.positiveButtonClick()
            }
            binding.safeyNextCancelButton.buttonCancel.setOnClickListener {
                dialog.dismiss()
                model.dialogStyle1Click.negativeButton()
            }
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setGravity(Gravity.NO_GRAVITY)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            val lp = dialog.window!!.attributes
            lp.dimAmount = 0.8f // Dim level. 0.0 - no dim, 1.0 - completely opaque

            dialog.window!!.attributes = lp
            dialog.show()
            return true
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
            return false
        }
    }


    @SuppressLint("InflateParams")
    fun dialogStyleShare(model: CustomDialogStyle1DataModel): Boolean {
        try {
            val binding = DialogShareFinishtestBinding.inflate(LayoutInflater.from(model.activity),
                model.activity.window.decorView.rootView as ViewGroup, false)

            val dialog = Dialog(model.activity)
            dialog.setContentView(binding.root)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)

            binding.imageView3.setOnClickListener {
                dialog.dismiss()
                model.dialogStyle1Click.positiveButtonClick()
            }
            binding.imageViewHomebg.setOnClickListener {
                dialog.dismiss()
                model.dialogStyle1Click.negativeButton()
            }
            binding.imageViewhome.setOnClickListener {
                binding.imageViewHomebg.performClick()
            }
            binding.textViewDashboard.setOnClickListener {
                binding.imageViewHomebg.performClick()
            }
            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setGravity(Gravity.NO_GRAVITY)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            val lp = dialog.window!!.attributes
            lp.dimAmount = 0.8f // Dim level. 0.0 - no dim, 1.0 - completely opaque

            dialog.window!!.attributes = lp
            dialog.show()
            return true
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
            return false
        }
    }


    @SuppressLint("InflateParams")
    fun dialogStyleSuccess(model: CustomDialogStyle1DataModel): Boolean {
        try {
            val binding = DialogSuccessDashboardBinding.inflate(LayoutInflater.from(model.activity),
                model.activity.window.decorView.rootView as ViewGroup, false)


            if (model.message != null) {
                binding.txtmessage.text = model.message
                binding.txtmessage.visibility = View.VISIBLE
            } else {
                binding.txtmessage.visibility = View.GONE
            }

            if (model.positiveButton != null) {
                binding.safeySaveButton.buttonSave.text = model.positiveButton
                binding.safeySaveButton.buttonSave.visibility = View.VISIBLE
            } else {
                binding.safeySaveButton.buttonSave.visibility = View.GONE
            }

            val dialog = Dialog(model.activity)
            dialog.setContentView(binding.root)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)

            binding.safeySaveButton.buttonSave.setOnClickListener {
                dialog.dismiss()
                model.dialogStyle1Click.positiveButtonClick()
            }

            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setGravity(Gravity.NO_GRAVITY)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            val lp = dialog.window!!.attributes
            lp.dimAmount = 0.8f // Dim level. 0.0 - no dim, 1.0 - completely opaque

            dialog.window!!.attributes = lp
            dialog.show()
            return true
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
            return false
        }
    }

    fun dialogStyleProgress(model: CustomDialogStyle1DataModel): Dialog? {
        try {
            val binding = DialogProgressBinding.inflate(LayoutInflater.from(model.activity),
                model.activity.window.decorView.rootView as ViewGroup, false)

            if (model.message != null) {
                binding.txtmessage.text = model.message
                binding.txtmessage.visibility = View.VISIBLE
            } else {
                binding.txtmessage.visibility = View.GONE
            }
            val dialog = Dialog(model.activity)
            dialog.setContentView(binding.root)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setCancelable(false)


            dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window!!.setGravity(Gravity.NO_GRAVITY)
            dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            val lp = dialog.window!!.attributes
            lp.dimAmount = 0.8f // Dim level. 0.0 - no dim, 1.0 - completely opaque

            dialog.window!!.attributes = lp
            dialog.show()
            return dialog
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
            return null
        }
    }

}


