package de.rki.coronawarnapp.storage

import androidx.lifecycle.MutableLiveData
import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.http.WebRequestBuilder
import de.rki.coronawarnapp.service.submission.SubmissionConstants.TEST_RESULT_URL
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.UiStateHelper
import de.rki.coronawarnapp.util.formatter.TestResult
import java.util.Date

object SubmissionRepository {
    private val TAG: String? = SubmissionRepository::class.simpleName

    val testResult = MutableLiveData(TestResult.INVALID)
    val testResultReceivedDate = MutableLiveData(Date())
    val deviceUIState = MutableLiveData(DeviceUIState.UNPAIRED)

    fun refreshUIState() {
        deviceUIState.value = UiStateHelper.uiState()
    }

    suspend fun refreshTestResult() {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()
        val testResultValue =
            WebRequestBuilder.asyncGetTestResult(TEST_RESULT_URL, registrationToken)
        testResult.value = TestResult.fromInt(testResultValue)
        if (testResult.value == TestResult.POSITIVE) {
            LocalData.isAllowedToSubmitDiagnosisKeys(true)
        }
        val initialTestResultReceivedTimestamp = LocalData.inititalTestResultReceivedTimestamp()

        if (initialTestResultReceivedTimestamp == null) {
            val currentTime = System.currentTimeMillis()
            LocalData.inititalTestResultReceivedTimestamp(currentTime)
            testResultReceivedDate.value = Date(currentTime)
        } else {
            testResultReceivedDate.value = Date(initialTestResultReceivedTimestamp)
        }
    }

    fun setTeletan(teletan: String) {
        LocalData.teletan(teletan)
    }
}
