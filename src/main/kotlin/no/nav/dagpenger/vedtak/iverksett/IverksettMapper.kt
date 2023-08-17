package no.nav.dagpenger.vedtak.iverksett

import mu.KotlinLogging
import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

private val BehovIverksett = "Iverksett"

val behandlingId = "behandlingId"

private val logger = KotlinLogging.logger { }

internal fun JsonMessage.tilIverksettDTO(): IverksettDto {
    val sakId = this["$BehovIverksett.sakId"]?.asText()
    return IverksettDto(
        sakId = sakId?.let { runCatching { UUID.fromString(it) }.getOrNull() } ?: UUID.randomUUID().also {
            logger.warn("Fikk ikke 'sakId' fra behovet. Iverksett APIet krever pt UUID. sakId var '$sakId' ")
        },
        behandlingId = this["$BehovIverksett.behandlingId"].asText().let { UUID.fromString(it) },
        personIdent = this["ident"].asText(),
        vedtak = vedtaksdetaljerDagpengerDto(this),
        forrigeIverksetting =
        when (bestemVedtakstype(this)) {
            VedtakType.UTBETALINGSVEDTAK -> {
                if (this["$BehovIverksett.forrigeBehandlingId"].asText() != "") {
                    ForrigeIverksettingDto(
                        behandlingId =
                        this["$BehovIverksett.forrigeBehandlingId"].asText().let {
                            UUID.fromString(it)
                        },
                    )
                } else {
                    null
                }
            }

            else -> null
        },
    )
}

private fun vedtaksdetaljerDagpengerDto(packet: JsonMessage) =
    VedtaksdetaljerDto(
        vedtakstype = bestemVedtakstype(packet),
        vedtakstidspunkt = packet["$BehovIverksett.vedtakstidspunkt"].asLocalDateTime(),
        resultat = when (packet.utfall()) {
            "Innvilget" -> Vedtaksresultat.INNVILGET
            "Avslått" -> Vedtaksresultat.AVSLÅTT
            else -> {
                throw IllegalArgumentException("Ugyldig utfall - vet ikke hvordan en mapper ${packet.utfall()} ")
            }
        },
        utbetalinger = packet["$BehovIverksett.utbetalingsdager"].map { utbetalingsdagJson ->
            UtbetalingDto(
                belopPerDag = utbetalingsdagJson["beløp"].asInt(),
                fraOgMedDato = utbetalingsdagJson["dato"].asLocalDate(),
                tilOgMedDato = utbetalingsdagJson["dato"].asLocalDate(),
            )
        },
        saksbehandlerId = "DIGIDAG",
        beslutterId = "DIGIDAG",
        vedtaksperioder = listOf(
            VedtaksperiodeDto(
                fraOgMedDato = packet["$BehovIverksett.virkningsdato"].asLocalDate(),
            ),
        ),
    )

private fun bestemVedtakstype(packet: JsonMessage) =
    if (packet["$BehovIverksett.utbetalingsdager"].size() == 0) VedtakType.RAMMEVEDTAK else VedtakType.UTBETALINGSVEDTAK

private fun JsonMessage.utfall(): String = this["$BehovIverksett.utfall"].asText()
