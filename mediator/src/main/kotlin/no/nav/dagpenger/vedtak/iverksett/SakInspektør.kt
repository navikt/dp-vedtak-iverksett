package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SakInspektør(sak: Sak) : SakVisitor {

    lateinit var virkningsdato: LocalDate
    lateinit var vedtakstidspunkt: LocalDateTime
    lateinit var vedtakId: UUID
    lateinit var behandlingId: UUID
    lateinit var ident: PersonIdentifikator
    lateinit var sakId: SakId
    val iverksettinger = mutableListOf<Iverksetting>()
    val iverksettingsdager = mutableListOf<IverksettingDagKopi>()

    init {
        sak.accept(this)
    }

    fun forrigeBehandlingId() = BehandlingIdVisitor(forrigeIverksetting()).behandlingId

    private fun forrigeIverksetting() = iverksettinger.sortedDescending()[1]

    override fun visitSak(ident: PersonIdentifikator, sakId: SakId) {
        this.ident = ident
        this.sakId = sakId
    }

    override fun visitIverksetting(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
    ) {
        this.vedtakId = vedtakId
        this.behandlingId = behandlingId
        this.virkningsdato = virkningsdato
        this.vedtakstidspunkt = vedtakstidspunkt
        this.iverksettinger.add(Iverksetting(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, mutableListOf()))
    }

    override fun visitIverksettingDag(dato: LocalDate, beløp: Beløp) {
        iverksettingsdager.add(IverksettingDagKopi(dato, beløp))
    }

    data class IverksettingDagKopi(val dato: LocalDate, val beløp: Beløp)
}
