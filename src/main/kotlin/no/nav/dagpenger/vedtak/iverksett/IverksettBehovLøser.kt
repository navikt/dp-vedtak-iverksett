package no.nav.dagpenger.vedtak.iverksett

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class IverksettBehovLøser(private val rapidsConnection: RapidsConnection) : River.PacketListener {

    private val BehovIverksett = "Iverksett"

    private companion object {
        var logger = KotlinLogging.logger {}
        var sikkerLogger = KotlinLogging.logger("tjenestekall.VedtakFattetMottak")
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
            validate { it.requireKey("$BehovIverksett.utbetalingsdager") }
            validate { it.requireKey("$BehovIverksett.utfall") }
            validate { it.interestedIn("@behovId", "iverksettingId") }
            validate { it.rejectKey("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val tilIverksettDTO = packet.tilIverksettDTO()

        packet["@løsning"] = mapOf(BehovIverksett to true)

        rapidsConnection.publish(packet.toJson())
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logger.error { problems.toExtendedReport() }
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        logger.error { error }
    }
}
