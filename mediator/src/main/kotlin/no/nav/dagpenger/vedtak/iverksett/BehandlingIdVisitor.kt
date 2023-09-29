package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class BehandlingIdVisitor(iverksetting: Iverksetting) : IverksettingVisitor {
    lateinit var behandlingId: UUID

    init {
        iverksetting.accept(this)
    }

    override fun visitIverksetting(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
    ) {
        this.behandlingId = behandlingId
    }
}
