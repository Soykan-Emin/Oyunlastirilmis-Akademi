package com.example.sifrelikasam

data class Wheel(
    val wheelId: String,
    val wheelName: String,
    val sections: List<WheelSection>
)

data class WheelSection(
    val sectionId: String,
    val sectionName: String,
    val sectionValue: Int  // Örneğin, her bölümün bir değeri olabilir
)