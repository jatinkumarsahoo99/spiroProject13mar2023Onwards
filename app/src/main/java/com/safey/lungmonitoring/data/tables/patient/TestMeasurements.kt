package com.safey.lungmonitoring.data.tables.patient

data class TestMeasurements(
     var measurement: String? = null,
     var measuredValue: Double = 0.0,
     var predictedValue: String? = null,
     var unit: String? = null,
     var lln: String? = null,
     var uln: String? = null,
     var zScore: String? = null,
     var predictedPer : Double = 0.0
)
