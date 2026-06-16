package com.fameafrica.afm.data.database.model.enums

enum class BoardStatus(val value: String) {
    SAFE("Safe"),
    UNDER_REVIEW("Under Review"),
    ON_THIN_ICE("On Thin Ice"),
    CRITICAL("Critical"),
    SACKED("Sacked")
}

enum class FinancialStatus(val value: String) {
    RICH("Rich"),
    HEALTHY("Healthy"),
    STABLE("Stable"),
    BREAKING_EVEN("Breaking Even"),
    IN_DEBT("In Debt"),
    BANKRUPT("Bankrupt")
}