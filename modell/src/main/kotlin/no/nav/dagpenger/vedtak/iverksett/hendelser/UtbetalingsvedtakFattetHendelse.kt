package no.nav.dagpenger.vedtak.iverksett.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.aktivitetslogg.IAktivitetslogg
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.iverksett.Beløp
import no.nav.dagpenger.vedtak.iverksett.Iverksetting
import no.nav.dagpenger.vedtak.iverksett.IverksettingDag
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class UtbetalingsvedtakFattetHendelse(
    private val meldingsreferanseId: UUID,
    private val ident: String,
    val vedtakId: UUID,
    val behandlingId: UUID,
    val sakId: String,
    val vedtakstidspunkt: LocalDateTime,
    val virkningsdato: LocalDate,
    val utfall: Utfall,
    val utbetalingsdager: List<Utbetalingsdag>,
    internal val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Aktivitetskontekst, IAktivitetslogg by aktivitetslogg {
    fun meldingsreferanseId() = meldingsreferanseId

    fun ident() = ident

    fun mapTilIverksetting() =
        Iverksetting(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            virkningsdato = virkningsdato,
            utfall = utfall,
            iverksettingsdager = mapTilIverksettingsdager(utbetalingsdager),
        )

    private fun mapTilIverksettingsdager(utbetalingsdager: List<Utbetalingsdag>) =
        utbetalingsdager.map { IverksettingDag(dato = it.dato, beløp = Beløp(it.beløp)) }
            .toMutableList().sorted()

    data class Utbetalingsdag(val dato: LocalDate, val beløp: Double)

    enum class Utfall {
        Innvilget,
        Avslått,
    }

    override fun toSpesifikkKontekst() =
        SpesifikkKontekst(
            kontekstType = this.javaClass.simpleName,
            kontekstMap =
                mapOf(
                    "ident" to ident,
                    "vedtakId" to vedtakId.toString(),
                    "behandlingId" to behandlingId.toString(),
                ),
        )
}
