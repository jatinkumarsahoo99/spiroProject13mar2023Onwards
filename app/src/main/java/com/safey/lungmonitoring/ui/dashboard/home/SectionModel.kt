package com.safey.lungmonitoring.ui.dashboard.home

import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.data.tables.patient.Symptoms


data class SectionModel(var section:String?=null, var subSectionModel: List<SubSectionModel>?=null,var testResult: List<AirTestResult>?=null,var subSymptonsSectionModel: List<SymptonSectionModel>?=null,var symptonList: List<Symptoms>?=null, var date : String?=null)

data class SubSectionModel(var section:String?=null, var testResult: List<AirTestResult>?=null, var date : String?=null)
data class SymptonSectionModel(var section:String?=null,var listSymptons: List<Symptoms>?=null, var date : String?=null)