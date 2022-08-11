package com.safey.lungmonitoring.ui.dashboard.symptons



data class SymptomModel(var id : Int, var imageResourse : String, var resourceId:Int, var sympton:String, var checked : Boolean = false)