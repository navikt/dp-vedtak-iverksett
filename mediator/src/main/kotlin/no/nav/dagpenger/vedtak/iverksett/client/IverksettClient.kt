package no.nav.dagpenger.vedtak.iverksett.client

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.appendEncodedPathSegments
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.vedtak.iverksett.Configuration
import org.slf4j.MDC

val behandlingId = "behandlingId"

internal class IverksettClient(
    private val baseUrl: String = Configuration.iverksettApiUrl,
    private val tokenProvider: () -> String,
    engine: HttpClientEngine = CIO.create {},
) {
    private companion object {
        val logger = KotlinLogging.logger {}
        val sikkerLogg = KotlinLogging.logger("tjenestekall")
    }

    private val httpClient =
        HttpClient(engine) {
            expectSuccess = true
            HttpResponseValidator {
                handleResponseExceptionWithRequest { exception, _ ->
                    val clientException =
                        exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                    val exceptionResponse = clientException.response
                    if (exceptionResponse.status == HttpStatusCode.Conflict) {
                        throw UtbetalingsvedtakAlleredeIverksattException()
                    }
                }
            }

            defaultRequest {
                header(HttpHeaders.Authorization, "Bearer ${tokenProvider.invoke()}")
            }
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }
            }
            install(Logging) {
                level = LogLevel.ALL
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            sikkerLogg.info(message)
                        }
                    }
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
        }

    fun iverksett(iverksettDto: IverksettDto) =
        runBlocking {
            val url = URLBuilder(baseUrl).appendEncodedPathSegments("api", "iverksetting").build()
            withContext(Dispatchers.IO) {
                try {
                    httpClient.post(url) {
                        header("nav-call-id", MDC.get(behandlingId))
                        header(HttpHeaders.XCorrelationId, MDC.get(behandlingId))
                        header(HttpHeaders.ContentType, ContentType.Application.Json)
                        setBody(iverksettDto)
                    }
                } catch (uaie: UtbetalingsvedtakAlleredeIverksattException) {
                    logger.info { "Vedtak med behandlingId: ${iverksettDto.behandlingId} er allerede sendt til iverksetting" }
                }
            }
        }
}

class UtbetalingsvedtakAlleredeIverksattException : RuntimeException()
