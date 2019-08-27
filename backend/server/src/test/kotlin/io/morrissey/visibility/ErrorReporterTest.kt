package io.morrissey.visibility

import io.ktor.client.HttpClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.morrissey.HomeServerConfig
import io.morrissey.model.IotLocation
import io.morrissey.routes.PhysicalSwitchResult
import io.morrissey.routes.getPhysicalSwitches
import org.apache.commons.mail.HtmlEmail
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertTrue

class ErrorReporterTest {
    @Test
    fun verifyErrorReport() {
        mockkStatic("io.morrissey.routes.SwitchesKt")
        val errorMessage = "ERROR_MESSAGE"
        every { getPhysicalSwitches(any(), eq(IotLocation.Frontyard)) } returns
                PhysicalSwitchResult.FailureResult(IotLocation.Frontyard, RuntimeException(errorMessage))
        every { getPhysicalSwitches(any(), eq(IotLocation.Backyard)) } returns
                PhysicalSwitchResult.SuccessResult(emptyList())
        val httpClient = mockk<HttpClient>()
        val emailFactory = mockk<EmailFactory>()
        val mockEmail = mockk<HtmlEmail>(relaxed = true)
        every { emailFactory.buildEmail() } returns mockEmail
        val homeServerConfig = mockk<HomeServerConfig>(relaxed = true)
        every { homeServerConfig.nodeRetryWait } returns 0L
        val nodeMaxRetry = 2
        every { homeServerConfig.nodeMaxRetry } returns nodeMaxRetry

        ErrorReporter(Timer(), emailFactory, httpClient, homeServerConfig)
        Thread.sleep(500)
        verify(exactly = 1) {
            mockEmail.subject = withArg { subject: String ->
                assertTrue { subject.contains(IotLocation.Frontyard.name) }
            }
            mockEmail.setHtmlMsg(withArg { message ->
                assertTrue { message.contains(errorMessage) }
            })
            mockEmail.send()
        }
        verify(exactly = 2) {
            getPhysicalSwitches(any(), eq(IotLocation.Frontyard))
        }
    }
}