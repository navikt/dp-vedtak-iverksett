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

internal class IverksettBehovløser(
    private val rapidsConnection: RapidsConnection,
    private val iverksettClient: IverksettClient,
) : River.PacketListener {

    private val BehovIverksett = "Iverksett"

    private companion object {
        val logger = KotlinLogging.logger {}
        val sikkerLogger = KotlinLogging.logger("tjenestekall.IverksettBehovløser")
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "behov") }
            validate { it.demandAllOrAny("@behov", listOf(BehovIverksett)) }
            validate { it.requireKey("ident") }
            validate { it.requireKey("$BehovIverksett.vedtakId") }
            validate { it.requireKey("$BehovIverksett.behandlingId") }
            validate { it.requireKey("$BehovIverksett.vedtakstidspunkt") }
            validate { it.requireKey("$BehovIverksett.virkningsdato") }
            validate { it.interestedIn("$BehovIverksett.utbetalingsdager") }
            validate { it.requireKey("$BehovIverksett.utfall") }
            validate { it.interestedIn("@behovId", "iverksettingId") }
            validate { it.rejectKey("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        withLoggingContext(contextMap(packet)) {
            logger.info { "Mottok $BehovIverksett. Se sikkerlogg for mer detaljer" }
            sikkerLogger.info { "Mottok behov om iverksetting av vedtak: " + packet.toJson() }
            runBlocking {
                withContext(MDCContext()) {
                    try {
                        iverksettClient.iverksett(packet.tilIverksettDTO())
                        packet["@løsning"] = mapOf(BehovIverksett to true)
                        rapidsConnection.publish(packet.toJson())
                    } catch (e: Exception) {
                        logger.error { "Feil mot iverksetting. Se sikkerlogg for detaljer" }
                        sikkerLogger.error { "Feil mot iverksetting $e" }
                        // throw e
                    }
                }
            }
        }
    }

    private fun contextMap(packet: JsonMessage) = mapOf(
        behandlingId to packet["$BehovIverksett.behandlingId"].asText(),
        "vedtakId" to packet["$BehovIverksett.vedtakId"].asText(),
        "iverksettingId" to packet["iverksettingId"].asText(),
        "behovId" to packet["@behovId"].asText(),
    )
}
