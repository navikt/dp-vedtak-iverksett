package no.nav.dagpenger.vedtak.iverksett.visitor

import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface IverksettingVisitor : IverksettingDagVisitor {
    fun visitIverksetting(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
        utfall: UtbetalingsvedtakFattetHendelse.Utfall,
    ) {
    }
}
