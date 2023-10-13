package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Iverksetting(
    private val vedtakId: UUID,
    private val behandlingId: UUID,
    private val vedtakstidspunkt: LocalDateTime,
    private val virkningsdato: LocalDate,
    private val utfall: UtbetalingsvedtakFattetHendelse.Utfall,
    private val iverksettingsdager: List<IverksettingDag>,
) : Aktivitetskontekst, Comparable<Iverksetting> {
    fun accept(visitor: IverksettingVisitor) {
        visitor.visitIverksetting(vedtakId, behandlingId, vedtakstidspunkt, virkningsdato, utfall)
        visitor.preVisitIverksettingDag(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            virkningsdato = virkningsdato,
            utfall = utfall,
        )
        visitAlleIverksettingsdager(visitor)
        visitor.postVisitIverksettingDag(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            virkningsdato = virkningsdato,
            utfall = utfall,
        )
    }

    override fun compareTo(other: Iverksetting) = this.vedtakstidspunkt.compareTo(other.vedtakstidspunkt)

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            "Iverksetting",
            mapOf(
                "vedtakId" to "$vedtakId",
                "behandlingId" to "$behandlingId",
                "vedtakstidspunkt" to "$vedtakstidspunkt",
                "virkningsdato" to "$virkningsdato",
                "utfall" to utfall.name,
            ),
        )
    }

    private fun visitAlleIverksettingsdager(visitor: IverksettingVisitor) {
        iverksettingsdager.forEach { iverksettingDag ->
            iverksettingDag.accept(visitor)
        }
    }
}
