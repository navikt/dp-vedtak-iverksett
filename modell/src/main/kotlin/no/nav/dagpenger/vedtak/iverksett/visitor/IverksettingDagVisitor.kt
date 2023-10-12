package no.nav.dagpenger.vedtak.iverksett.visitor

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.vedtak.iverksett.SakId
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface IverksettingDagVisitor : AktivitetsloggVisitor {
    fun preVisitIverksettingDag(
        sakId: SakId,
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
        utfall: UtbetalingsvedtakFattetHendelse.Utfall,
    ) {}

    fun visitIverksettingDag(
        dato: LocalDate,
        beløp: Beløp,
    ) {
    }

    fun postVisitIverksettingDag(
        sakId: SakId,
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
        utfall: UtbetalingsvedtakFattetHendelse.Utfall,
    ) {}
}
