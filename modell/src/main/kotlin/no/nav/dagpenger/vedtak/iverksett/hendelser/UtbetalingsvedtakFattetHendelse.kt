package no.nav.dagpenger.vedtak.iverksett.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksett.Iverksetting
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class UtbetalingsvedtakFattetHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    vedtakId: UUID,
    behandlingId: UUID,
    sakId: String,
    val vedtakstidspunkt: LocalDateTime,
    val virkningsdato: LocalDate,
    val utbetalingsdager: List<Utbetalingsdag>,
    val utfall: Utfall,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : VedtakFattetHendelse(meldingsreferanseId, ident, vedtakId, behandlingId, sakId, aktivitetslogg) {

    fun tilIverksetting(): Iverksetting = Iverksetting(vedtakId, ident())

    data class Utbetalingsdag(val dato: LocalDate, val beløp: Double)
    enum class Utfall {
        Innvilget,
        Avslått,
    }
}
