package com.safey.lungmonitoring.data.datautils

import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.utils.Constants


enum class enumGender(val value: Int, private val labelExportFormatId: Int) {
    MALE(1, R.string.male),
    FEMALE(2, R.string.female);

    fun getFormatString() : String? {
        return Constants.getStringResourceById(labelExportFormatId)
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
        fun fromString(value: String) = values().first { it.getFormatString() == value }
    }
}
enum class Ethnicities(val value: Int, private val labelExportFormatId: Int) {
    AFRICAN_AMERICAN(1, R.string.American_African),
    CAUCASIAN(2, R.string.caucasian),
    North_East_Asian(3, R.string.North_East_Asian),
    South_East_Asian(4, R.string.South_East_Asian),
    Others(20, R.string.Others);

    fun getFormatString() : String? {
        return Constants.getStringResourceById(labelExportFormatId)
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
        fun fromString(value: String) = values().first { it.getFormatString() == value }
    }
}


enum class enumMedicationFrequency(val value: Int, private val labelExportFormatId: Int) {
    EveryDay(1, R.string.everyday),
    CertatinDays(2, R.string.certain_days_week);

    fun getFormatString() : String? {
        return Constants.getStringResourceById(labelExportFormatId)
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
        fun fromString(value: String) = values().first { it.getFormatString() == value }
    }
}

enum class enumHeightUnit(val value: Int, private val labelExportFormatId: Int) {
    CM(1, R.string.cm),
    FT(2, R.string.ft);

    fun getFormatString() : String? {
        return Constants.getStringResourceById(labelExportFormatId)
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
        fun fromString(value: String) = values().first { it.getFormatString() == value }
    }
}


enum class enumMeasurementsDashboard(val value: Int, private val labelExportFormatId: Int) {
    FVC(1, R.string.fvc),
    FEV1(2, R.string.fev1),
    PEF(3, R.string.pef),

    FEV025(4, R.string.fev025),
    FEV005(5, R.string.fev05),
    FEV075(6, R.string.fev075),

    FEV005fvc(7, R.string.fev05fvc),
    FEV075fvc(8, R.string.fev075fvc),
    FEV3(9, R.string.fev3),
    FEV6(10, R.string.fev6),
    FEV1FEV6(11, R.string.fev1fev6),
    eotv(12, R.string.eotv),
    bev(13, R.string.bev),
    t0(14, R.string.t0)
    ;

    fun getFormatString() : String? {
        return Constants.getStringResourceById(labelExportFormatId)
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
        fun fromString(value: String) = values().first { it.getFormatString() == value }
    }
}


enum class enumMeasurementsFEVC(val value: Int, private val labelExportFormatId: Int) {
    FVC(1, R.string.fvc),
    FEV1(2, R.string.fev1),
    PEF(3, R.string.pef),
    FEV025(4, R.string.fev025),
    FEV005(5, R.string.fev05),
    FEV075(6, R.string.fev075),
    FEV005fvc(7, R.string.fev05fvc),
    FEV075fvc(8, R.string.fev075fvc),
    FEV3(9, R.string.fev3),
    FEV6(10, R.string.fev6),
    FEV1FEV6(11, R.string.fev1fev6),
    FEV1FVC(12, R.string.fev1fvc),
    FEV3FVC(13, R.string.fev3fvc),
    FEV6FVC(14, R.string.fev6fvc),
    FEF2575(15, R.string.fef2575),
    FEF2575FVC(16, R.string.fef2575fvc),
    FEF10(17, R.string.fef10),
    FEF25(18, R.string.fef25),
    FEF40(19, R.string.fef40),
    FEF50(20, R.string.fef50),
    FEF60(21, R.string.fef60),
    FEF75(22, R.string.fef75),
    FEF80(23, R.string.fef80),
    FEF50FVC(24, R.string.fef50FVC),
    FET(25, R.string.fet),
    FET2575(26, R.string.fet2575),
    PEFT(27, R.string.peft),
    eotv(28, R.string.eotv),
    bev(29, R.string.bev),
    t0(30, R.string.t0)
    ;

    fun getFormatString() : String? {
        return Constants.getStringResourceById(labelExportFormatId)
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
        fun fromString(value: String) = values().first { it.getFormatString() == value }
    }
}

enum class enumMeasurementsFIVC(val value: Int, private val labelExportFormatId: Int) {
    FIVC(1, R.string.fivc),
    FIV1FIVC(2, R.string.fiv1fivc),
    FIF2575(3, R.string.fif2575),
    FIF25(4, R.string.fif25),
    FIF50(5, R.string.fif50),
    FIF75(6, R.string.fif75),
    FIV1(7, R.string.fiv1),
    PIF(8, R.string.pif),
    bev(9, R.string.bev)

    ;

    fun getFormatString() : String? {
        return Constants.getStringResourceById(labelExportFormatId)
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
        fun fromString(value: String) = values().first { it.getFormatString() == value }
    }
}




enum class enumDay(val value: Int, private val labelExportFormatId: Int) {
    Morning(0, R.string.morning),
    Afternoon(1, R.string.afternoon),
    Evening(2, R.string.evening);

    fun getFormatString() : String? {
        return Constants.getStringResourceById(labelExportFormatId)
    }

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
        fun fromString(value: String) = values().first { it.getFormatString() == value }
    }
}