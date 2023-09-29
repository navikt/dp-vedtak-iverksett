package no.nav.dagpenger.vedtak.iverksett.visitor

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface IverksettingVisitor : IverksettingDagVisitor {
    fun visitIverksetting(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
    ) {
    }
}
