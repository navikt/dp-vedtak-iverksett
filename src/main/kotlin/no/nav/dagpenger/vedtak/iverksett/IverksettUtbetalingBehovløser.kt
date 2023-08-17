package no.nav.dagpenger.vedtak.iverksett

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class IverksettUtbetalingBehovløser(
    private val rapidsConnection: RapidsConnection,
    private val iverksettClient: IverksettClient,
) : River.PacketListener {

    private val BehovIverksettUtbetaling = "IverksettUtbetaling"

    private companion object {
        val logger = KotlinLogging.logger {}
        val sikkerLogger = KotlinLogging.logger("tjenestekall.IverksettUtbetalingBehovløser")
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "behov") }
            validate { it.demandAllOrAny("@behov", listOf(BehovIverksettUtbetaling)) }
            validate { it.requireKey("ident") }
            validate { it.requireKey("$BehovIverksettUtbetaling.vedtakId") }
            validate { it.requireKey("$BehovIverksettUtbetaling.behandlingId") }
            validate { it.requireKey("$BehovIverksettUtbetaling.vedtakstidspunkt") }
            validate { it.requireKey("$BehovIverksettUtbetaling.virkningsdato") }
            validate { it.requireKey("$BehovIverksettUtbetaling.sakId") }
            validate { it.requireKey("$BehovIverksettUtbetaling.utfall") }
            validate { it.interestedIn("$BehovIverksettUtbetaling.utbetalingsdager") }
            validate { it.interestedIn("$BehovIverksettUtbetaling.forrigeBehandlingId") }
            validate { it.interestedIn("@behovId", "iverksettingId") }
            validate { it.rejectKey("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        withLoggingContext(contextMap(packet)) {
            logger.info { "Mottok $BehovIverksettUtbetaling. Se sikkerlogg for detaljer." }
            sikkerLogger.info { "Mottok behov om iverksetting av utbetalingsvedtak: " + packet.toJson() }
            runBlocking {
                withContext(MDCContext()) {
                    try {
                        iverksettClient.iverksett(packet.tilIverksettUtbetalingDTO())
                    } catch (e: Exception) {
                        logger.error { "Feil ved iverksetting av utbetalingsvedtak. Se sikkerlogg for detaljer." }
                        sikkerLogger.error { "Feil ved iverksetting av utbetalingsvedtak. $e" }
                        throw e
                    }
                }
            }
            packet["@løsning"] = mapOf(BehovIverksettUtbetaling to true)
            rapidsConnection.publish(packet.toJson())
        }
    }

    private fun contextMap(packet: JsonMessage) = mapOf(
        behandlingId to packet["$BehovIverksettUtbetaling.behandlingId"].asText(),
        "vedtakId" to packet["$BehovIverksettUtbetaling.vedtakId"].asText(),
        "iverksettingId" to packet["iverksettingId"].asText(),
        "behovId" to packet["@behovId"].asText(),
    )
}
