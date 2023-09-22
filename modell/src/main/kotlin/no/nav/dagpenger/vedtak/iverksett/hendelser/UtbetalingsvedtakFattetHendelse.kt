package no.nav.dagpenger.vedtak.iverksett.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksett.Iverksetting
import no.nav.dagpenger.vedtak.iverksett.IverksettingDag
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class UtbetalingsvedtakFattetHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val vedtakId: UUID,
    val behandlingId: UUID,
    val sakId: String,
    val vedtakstidspunkt: LocalDateTime,
    val virkningsdato: LocalDate,
    val utbetalingsdager: List<Utbetalingsdag>,
    val utfall: Utfall,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Hendelse(meldingsreferanseId, ident, aktivitetslogg) {

    fun mapTilIverksetting(): Iverksetting = Iverksetting(vedtakId, ident(), mapTilIverksettingsdager(utbetalingsdager))

    private fun mapTilIverksettingsdager(utbetalingsdager: List<Utbetalingsdag>) =
        utbetalingsdager.map { IverksettingDag(dato = it.dato, beløp = Beløp.fra(it.beløp.toBigDecimal())) }.toMutableList()

    data class Utbetalingsdag(val dato: LocalDate, val beløp: Double)
    enum class Utfall {
        Innvilget,
        Avslått,
    }

    override fun kontekstMap(): Map<String, String> =
        mapOf("vedtakId" to vedtakId.toString(), "behandlingId" to behandlingId.toString())
}
