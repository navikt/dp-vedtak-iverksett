package no.nav.dagpenger.vedtak.iverksett.visitor

import java.time.LocalDate
import java.util.UUID

interface IverksettingVisitor : IverksettingDagVisitor {
    fun visitIverksetting(id: UUID, vedtakId: UUID, virkningsdato: LocalDate) {}
}
