package no.nav.dagpenger.vedtak.iverksett.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.iverksett.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class UtbetalingsvedtakFattetMottak(
    rapidsConnection: RapidsConnection,
    private val hendelseMediator: IHendelseMediator,
) : River.PacketListener {

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogger = KotlinLogging.logger("tjenestekall.VedtakFattetMottak")
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "utbetaling_vedtak_fattet") }
            validate { it.requireKey("@id", "@opprettet") }
            validate { it.requireKey("utbetalingsdager") }
            validate {
                it.requireKey(
                    "ident",
                    "behandlingId",
                    "sakId",
                    "vedtakId",
                    "vedtaktidspunkt",
                    "virkningsdato",
                    "utfall",
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtakId = packet["vedtakId"].asText()
        val behandlingId = packet["behandlingId"].asText()
        withLoggingContext(mapOf("vedtakId" to vedtakId, "behandlingId" to behandlingId)) {
            logger.info { "Fått utbetaling_vedtak_fattet hendelse" }
            sikkerlogger.info { "Fått utbetaling_vedtak_fattet hendelse. Hendelse: ${packet.toJson()}" }
            val vedtakFattetHendelseMessage = UtbetalingsvedtakFattetHendelseMessage(packet)
            vedtakFattetHendelseMessage.behandle(hendelseMediator, context)
        }
    }
}
