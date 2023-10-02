package no.nav.dagpenger.vedtak.iverksett.client.mapper

import no.nav.dagpenger.vedtak.iverksett.Iverksetting
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
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
        utfall: UtbetalingsvedtakFattetHendelse.Utfall,
    ) {
        this.behandlingId = behandlingId
    }
}
