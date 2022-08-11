package com.safey.lungmonitoring.utils

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.SafeyApplication
import java.text.DecimalFormat
import java.util.*

object Constants {
    lateinit var reportEndDate: Date
    lateinit var reportStartDate: Date
    var fromAvatar: Boolean = false
    var FRAGMENT_CODE_HOME ="Home_Fragment"
    var FRAGMENT_CODE_PROFILE ="Profile_Fragment"
    var FRAGMENT_CODE_MEDICATIONS ="Medications_Fragment"
    var FRAGMENT_CODE_REPORT ="Reports_Fragment"
    var FRAGMENT_CODE_TREND ="Trends_Fragment"
    var FRAGMENT_CODE_ABOUT ="About_Fragment"
    var FRAGMENT_CODE_LEGAL ="Legal_Fragment"
    var FRAGMENT_CODE_DEVICE_SETUP ="Device_setup_Fragment"
    var FRAGMENT_CODE_SPIROMETRYTEST ="Spirometry_Test_Fragment"
    var START_OVER ="START OVER"
    var currentFragment = ""
    val lungTest: String = ""
    const val DATE_FORMAT_PATTERN = "yyyy-MM-dd"
    const val DAY_FORMAT_PATTERN = "dd"
    const val DATE_FORMAT_PATTERN1 = "dd-MMM"
    const val DATE_FORMAT_PATTERN2 = "yyyy-MM-dd hh:mm aa"
    const val TIME_FORMAT_PATTERN_24 = "HH:mm"
    const val TIME_FORMAT_PATTERN_12 = "hh:mm aa"
    const val AVATAR = "avatar"
    const val AVATAR_JSON = "Avatar.json"
    val decimalFormat = DecimalFormat("#.####")
    val decimalTimeFormat = DecimalFormat("#.##")
    val decimalInhalationFormat = DecimalFormat("#.#")
    const val MED_ICON_JSON_FILENAME = "MedIcon.json"
    const val MED_COLOR_JSON_FILENAME = "MedColors.json"
    const val Sympton_JSON_FILENAME = "SymptonIcon.json"
    val imageURL = "imageURL"
    val sympton = "sympton"
    var isPost = false
    var isDelete = false
    var postTrialCount = 0

    val alphaBlur = 0.4F
    val alphaClear = 0.9F

    fun updateNavBarProgress(model: NavBarModel) {
        val toolbar = model.toolbar
        val activity = model.activity
        val title = model.title
        val leftIcon = model.iconLeft
        val rightIcon = model.iconRight
        val isHidden = model.isHidden

        if (isHidden) {
            toolbar!!.visibility = View.GONE
        } else {
            toolbar!!.visibility = View.VISIBLE
        }

        val txtHeader = toolbar.findViewById<TextView>(R.id.txtHeader)
        val iconLeft = toolbar.findViewById<ImageView>(R.id.iconLeft)
      /*  val iconRight = toolbar.findViewById<ImageView>(R.id.iconRight)*/

        if (title != null) {
            txtHeader.text = title
        } else {
            txtHeader.text = ""
        }
        try {
            iconLeft.setImageDrawable(null)
        } catch (e: Exception) {
            //Logger.tag("Exception").error(e)
        }
        try {
            /*iconRight.setImageDrawable(null)*/
        } catch (e: Exception) {
            //Logger.tag("Exception").error(e)
        }
        try {
            iconLeft.setImageDrawable(ContextCompat.getDrawable(activity!!, leftIcon))
        } catch (e: Exception) {
            //Logger.tag("Exception").error(e)
        }
        try {
          /*  iconRight.setImageDrawable(ContextCompat.getDrawable(activity!!, rightIcon))*/
        } catch (e: Exception) {
            //Logger.debug("Exception: $e")
        }

    }

    fun getStringResourceById(resId: Int): String {
        val myContext: Context? = SafeyApplication.getMyContext()
        return myContext!!.getString(resId)
    }

    //region view Inflators
    fun showToast(Text: String?, context: Context?) {
        Toast.makeText(context, Text, Toast.LENGTH_LONG).show()
    }

}