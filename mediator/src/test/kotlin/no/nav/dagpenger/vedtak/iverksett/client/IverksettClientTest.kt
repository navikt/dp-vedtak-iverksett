package no.nav.dagpenger.vedtak.iverksett.client

import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.kontrakter.felles.Personident
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class IverksettClientTest {
    private val tokenProvider = { "token" }

    @Test
    fun `202 status fra iverksett`() {
        runBlocking {
            val mockEngine = mockEngine(202)
            val client = IverksettClient(baseUrl = "http://localhost/", tokenProvider, mockEngine)
            client.iverksett(
                iverksettDagpengerdDtoDummy(),
            )
        }
    }

    @Test
    fun `409 status, tidligere iverksatte vedtak fører ikke til at vi kaster Exception`() {
        runBlocking {
            val mockEngine = mockEngine(HttpStatusCode.Conflict.value)
            val client = IverksettClient(baseUrl = "http://localhost/", tokenProvider, mockEngine)
            assertDoesNotThrow {
                client.iverksett(iverksettDagpengerdDtoDummy())
            }
        }
    }

    @Test
    fun `4xx og 5xx status resulterer i exception, bortsett fra 409`() =
        runBlocking {
            (399 until 599).forEach { statusCode ->
                if (statusCode != HttpStatusCode.Conflict.value) {
                    val mockEngine = mockEngine(statusCode)
                    val client = IverksettClient(baseUrl = "http://localhost/", tokenProvider, mockEngine)
                    assertThrows<RuntimeException> {
                        runBlocking {
                            client.iverksett(
                                iverksettDagpengerdDtoDummy(),
                            )
                        }
                    }
                }
            }
        }

    private fun iverksettDagpengerdDtoDummy() =
        IverksettDto(
            sakId = UUID.randomUUID(),
            behandlingId = UUID.randomUUID(),
            personident = Personident("15507600333"),
            vedtak =
                VedtaksdetaljerDto(
                    vedtakstype = VedtakType.UTBETALINGSVEDTAK,
                    vedtakstidspunkt = LocalDateTime.now(),
                    resultat = Vedtaksresultat.INNVILGET,
                    saksbehandlerId = "DIGIDAG",
                    beslutterId = "DIGIDAG",
                    vedtaksperioder =
                        listOf(
                            VedtaksperiodeDto(
                                fraOgMedDato = LocalDate.now(),
                            ),
                        ),
                ),
        )

    private fun mockEngine(statusCode: Int) =
        MockEngine { request ->
            request.headers[HttpHeaders.Accept] shouldBe "application/json"
            request.headers[HttpHeaders.Authorization] shouldBe "Bearer ${tokenProvider.invoke()}"
            respond(content = "", HttpStatusCode.fromValue(statusCode))
        }
}
