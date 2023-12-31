package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class ModellInspektør(sak: Sak) : SakVisitor {
    lateinit var virkningsdato: LocalDate
    lateinit var vedtakstidspunkt: LocalDateTime
    lateinit var utfall: UtbetalingsvedtakFattetHendelse.Utfall
    lateinit var vedtakId: UUID
    lateinit var behandlingId: UUID
    lateinit var ident: PersonIdentifikator
    lateinit var sakId: SakId
    val iverksettingsdager = mutableListOf<IverksettingDagKopi>()

    init {
        sak.accept(this)
    }

    override fun visitSak(
        ident: PersonIdentifikator,
        sakId: SakId,
    ) {
        this.ident = ident
        this.sakId = sakId
    }

    override fun visitIverksetting(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
        utfall: UtbetalingsvedtakFattetHendelse.Utfall,
    ) {
        this.vedtakId = vedtakId
        this.behandlingId = behandlingId
        this.virkningsdato = virkningsdato
        this.vedtakstidspunkt = vedtakstidspunkt
        this.utfall = utfall
    }

    override fun visitIverksettingDag(
        dato: LocalDate,
        beløp: Beløp,
    ) {
        iverksettingsdager.add(IverksettingDagKopi(dato, beløp))
    }
}

data class IverksettingDagKopi(val dato: LocalDate, val beløp: Beløp)
