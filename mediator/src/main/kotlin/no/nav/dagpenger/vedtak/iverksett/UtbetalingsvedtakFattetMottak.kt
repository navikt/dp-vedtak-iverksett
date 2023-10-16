package no.nav.dagpenger.vedtak.iverksett

import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

internal class UtbetalingsvedtakFattetMottak(
    rapidsConnection: RapidsConnection,
    private val sakMediator: SakMediator,
) : River.PacketListener {
    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogger = KotlinLogging.logger("tjenestekall.UtbetalingsvedtakFattetMottak")
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "utbetaling_vedtak_fattet") }
            validate { it.requireKey("@id", "@opprettet") }
            validate {
                it.requireKey(
                    "ident",
                    "behandlingId",
                    "sakId",
                    "vedtakId",
                    "vedtaktidspunkt",
                    "virkningsdato",
                    "utfall",
                    "utbetalingsdager",
                )
            }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val vedtakId = packet["vedtakId"].asText()
        val behandlingId = packet["behandlingId"].asText()

        withLoggingContext(mapOf("vedtakId" to vedtakId, "behandlingId" to behandlingId)) {
            logger.info { "Fått utbetaling_vedtak_fattet hendelse" }
            sikkerlogger.info { "Fått utbetaling_vedtak_fattet hendelse. Hendelse: ${packet.toJson()}" }

            val utbetalingsvedtakFattetHendelse = utbetalingsvedtakFattetHendelse(packet)
            sakMediator.håndter(utbetalingsvedtakFattetHendelse)
        }
    }

    private fun utbetalingsvedtakFattetHendelse(packet: JsonMessage) =
        UtbetalingsvedtakFattetHendelse(
            meldingsreferanseId = packet["@id"].asUUID(),
            ident = packet["ident"].asText(),
            vedtakId = packet["vedtakId"].asUUID(),
            behandlingId = packet["behandlingId"].asUUID(),
            sakId = packet["sakId"].asText(),
            vedtakstidspunkt = packet["vedtaktidspunkt"].asLocalDateTime(),
            virkningsdato = packet["virkningsdato"].asLocalDate(),
            utfall =
                when (packet.utfall()) {
                    "Innvilget" -> UtbetalingsvedtakFattetHendelse.Utfall.Innvilget
                    "Avslått" -> UtbetalingsvedtakFattetHendelse.Utfall.Avslått
                    else -> throw IllegalArgumentException("Vet ikke om utfall ${packet.utfall()}")
                },
            utbetalingsdager = utbetalingsdager(packet),
        )

    private fun JsonMessage.utfall(): String = this["utfall"].asText()

    private fun utbetalingsdager(packet: JsonMessage) =
        packet["utbetalingsdager"].map { utbetalingsdagJson ->
            UtbetalingsvedtakFattetHendelse.Utbetalingsdag(
                dato = utbetalingsdagJson["dato"].asLocalDate(),
                beløp = utbetalingsdagJson["beløp"].asDouble(),
            )
        }.toList()
}

internal fun JsonNode.asUUID() = this.asText().let { UUID.fromString(it) }
