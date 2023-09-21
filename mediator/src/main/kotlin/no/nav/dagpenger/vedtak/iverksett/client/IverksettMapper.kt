package no.nav.dagpenger.vedtak.iverksett.client

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

// private val BehovIverksett = "Iverksett"

val behandlingId = "behandlingId"

private val logger = KotlinLogging.logger { }

internal fun JsonMessage.tilIverksettDTO(): IverksettDto {
    val BehovIverksett = "Iverksett"
    val sakId = this["$BehovIverksett.sakId"].asText()
    return IverksettDto(
        sakId = sakId?.let { runCatching { UUID.fromString(it) }.getOrNull() } ?: UUID.randomUUID().also {
            logger.warn("Fikk ikke 'sakId' fra behovet. Iverksett APIet krever pt UUID. sakId var '$sakId' ")
        },
        behandlingId = this["$BehovIverksett.behandlingId"].asText().let { UUID.fromString(it) },
        personIdent = this["ident"].asText(),
        vedtak = vedtaksdetaljerDagpengerDto(this, BehovIverksett),
    )
}

internal fun JsonMessage.tilIverksettUtbetalingDTO(): IverksettDto {
    val BehovIverksett = "IverksettUtbetaling"
    val sakId = this["$BehovIverksett.sakId"].asText()
    val forrigeBehandlingId = this["$BehovIverksett.forrigeBehandlingId"].asText()
    return IverksettDto(
        sakId = sakId?.let { runCatching { UUID.fromString(it) }.getOrNull() } ?: UUID.randomUUID().also {
            logger.warn("Fikk ikke 'sakId' fra behovet. Iverksett APIet krever pt UUID. sakId var '$sakId' ")
        },
        behandlingId = this["$BehovIverksett.behandlingId"].asText().let { UUID.fromString(it) },
        personIdent = this["ident"].asText(),
        vedtak = vedtaksdetaljerUtbetalingDto(this, BehovIverksett),
        forrigeIverksetting =
        if (forrigeBehandlingId != "") {
            ForrigeIverksettingDto(behandlingId = forrigeBehandlingId.let { UUID.fromString(it) })
        } else {
            null
        },
    )
}

private fun vedtaksdetaljerDagpengerDto(packet: JsonMessage, behov: String) =
    VedtaksdetaljerDto(
        vedtakstype = VedtakType.RAMMEVEDTAK,
        vedtakstidspunkt = packet["$behov.vedtakstidspunkt"].asLocalDateTime(),
        resultat = when (packet.utfall(behov)) {
            "Innvilget" -> Vedtaksresultat.INNVILGET
            "Avslått" -> Vedtaksresultat.AVSLÅTT
            else -> {
                throw IllegalArgumentException("Ugyldig utfall - vet ikke hvordan en mapper ${packet.utfall(behov)} ")
            }
        },
        saksbehandlerId = "DIGIDAG",
        beslutterId = "DIGIDAG",
        vedtaksperioder = listOf(
            VedtaksperiodeDto(
                fraOgMedDato = packet["$behov.virkningsdato"].asLocalDate(),
            ),
        ),
    )

private fun vedtaksdetaljerUtbetalingDto(packet: JsonMessage, behov: String) =
    VedtaksdetaljerDto(
        vedtakstype = VedtakType.UTBETALINGSVEDTAK,
        vedtakstidspunkt = packet["$behov.vedtakstidspunkt"].asLocalDateTime(),
        resultat = when (packet.utfall(behov)) {
            "Innvilget" -> Vedtaksresultat.INNVILGET
            "Avslått" -> Vedtaksresultat.AVSLÅTT
            else -> {
                throw IllegalArgumentException("Ugyldig utfall - vet ikke hvordan en mapper ${packet.utfall(behov)} ")
            }
        },
        utbetalinger = packet["$behov.utbetalingsdager"].map { utbetalingsdagJson ->
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
                fraOgMedDato = packet["$behov.virkningsdato"].asLocalDate(),
            ),
        ),
    )

private fun JsonMessage.utfall(behov: String): String = this["$behov.utfall"].asText()
