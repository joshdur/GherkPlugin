package {OUTPUT_PACKAGE}

import java.util.*


data class FeatureReport(val featureName: String, val scenarioReports: HashSet<ScenarioReport> = HashSet<ScenarioReport>())

data class ScenarioReport(
        val scenarioName: String,
        val testStatus: ScenarioTestStatus,
        val throwable: Throwable? = null
)

enum class ScenarioTestStatus {
    PASSED, IGNORED, ERROR
}