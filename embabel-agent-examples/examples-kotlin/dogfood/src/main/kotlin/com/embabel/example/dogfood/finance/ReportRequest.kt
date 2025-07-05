package com.embabel.example.dogfood.finance

data class ReportRequest(
    val userRequest: String?,
    val agent: String,
    val outputDirectory: String = "/Users/deniz/Downloads",
    val outputFile: String = "report.md"
)