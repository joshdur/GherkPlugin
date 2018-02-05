package {OUTPUT_PACKAGE}


private const val PASSED = "passed"
private const val FAILED = "failed"
private const val IGNORED = "ignored"

private const val FEATURE = "Feature"
private const val SCENARIO = "Scenario"

private const val DEFAULT_LOCATION = "location"
private const val DEFAULT_LINE = 0
private const val DEFAULT_DURATION = 100L

fun defaultMatch() = Match(DEFAULT_LOCATION)

data class CucumberFeatureReport(val description: String,
                                 val comment: List<Comment>,
                                 val id: String,
                                 val keyword: String = FEATURE,
                                 val name: String,
                                 val line: Int,
                                 val tags: List<Tag>,
                                 val uri: String,
                                 val elements: List<Element>)

data class Comment(val value: String, val line: Int)
data class Tag(val name: String, val line: Int)

data class Element(val id: String,
                   val keyword: String = SCENARIO,
                   val type: String = SCENARIO.toLowerCase(),
                   val name: String,
                   val description: String,
                   val line: Int,
                   val before: List<Before>,
                   val after: List<After>,
                   val steps: List<Step>
)

data class Before(val result: Result, val match: Match = defaultMatch())
data class After(val result: Result, val match: Match = defaultMatch())
data class Step(val name: String, val keyword: String?, val line: Int = DEFAULT_LINE, val result: Result, val match: Match = defaultMatch())

data class Result(val status: String, val duration: Long = DEFAULT_DURATION, val error_message: String? = null)
data class Match(val location: String)


fun transformToCucumberReport(featureReports: Collection<FeatureReport>): List<CucumberFeatureReport> {
    return featureReports.map {
        val elements = it.scenarioReports.map { mapElement(it, emptyList()) }
        mapFeatureReport(it, elements)
    }
}


private fun mapElement(scenario: ScenarioReport, steps: List<Step>): Element {
    return Element(id = scenario.scenarioName,
            name = scenario.scenarioName,
            description = scenario.scenarioName,
            after = listOf(After(result(scenario))),
            before = listOf(Before(Result(PASSED))),
            line = 0,
            steps = steps)
}

private fun mapFeatureReport(feature: FeatureReport, elements: List<Element>): CucumberFeatureReport {
    return CucumberFeatureReport(id = feature.featureName,
            description = feature.featureName,
            name = feature.featureName,
            uri = feature.featureName,
            elements = elements,
            line = 0,
            tags = emptyList(),
            comment = emptyList()
    )
}

private fun result(scenario: ScenarioReport): Result {
    return when (scenario.testStatus) {
        ScenarioTestStatus.PASSED -> Result(PASSED)
        ScenarioTestStatus.ERROR -> Result(FAILED, error_message = scenario.throwable?.message ?: "No message")
        ScenarioTestStatus.IGNORED -> Result(IGNORED)
    }
}
