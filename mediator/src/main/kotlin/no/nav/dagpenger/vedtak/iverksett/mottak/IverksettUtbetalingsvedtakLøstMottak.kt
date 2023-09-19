package no.nav.dagpenger.vedtak.iverksett.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.iverksett.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class IverksettUtbetalingsvedtakLøstMottak(
    rapidsConnection: RapidsConnection,
    private val iHendelseMediator: IHendelseMediator,
) : River.PacketListener {

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerLogger = KotlinLogging.logger("tjenestekall.IverksettingLøstMottak")
        val iverksettBehov = "IverksettUtbetaling"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "behov") }
            validate { it.demandAllOrAny("@behov", listOf(iverksettBehov)) }
            validate { it.requireKey("@id", "@opprettet") }
            validate {
                it.requireKey(
                    "iverksettingId",
                    "ident",
                    "vedtakId",
                    "$iverksettBehov.behandlingId",
                    "$iverksettBehov.sakId",
                    "$iverksettBehov.utbetalingsdager",
                    "@løsning",
                    "@id",
                    "@opprettet",
                )
            }
            validate {
                it.require("@løsning") { løsning ->
                    løsning.required(iverksettBehov)
                }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtakId = packet["vedtakId"].asText()
        val iverksettingId = packet["iverksettingId"].asText()
        withLoggingContext("vedtakId" to vedtakId, "iverksettingId" to iverksettingId) {
            logger.info { "Fått løsning på $iverksettBehov" }
            sikkerLogger.info { "Fått løsning på $iverksettBehov med packet\n ${packet.toJson()}" }
            val iverksattHendelseMessage = IverksattHendelseMessage(packet)
            iverksattHendelseMessage.behandle(iHendelseMediator, context)
        }
    }
}
