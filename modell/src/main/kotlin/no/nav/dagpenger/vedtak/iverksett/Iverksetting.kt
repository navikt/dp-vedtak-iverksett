package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Iverksetting private constructor(
    private val id: UUID,
    private val vedtakId: UUID,
    private val behandlingId: UUID,
    private val vedtakstidspunkt: LocalDateTime,
    private val virkningsdato: LocalDate,
    private val utfall: UtbetalingsvedtakFattetHendelse.Utfall,
    private val iverksettingsdager: MutableList<IverksettingDag>,
) : Aktivitetskontekst, Comparable<Iverksetting> {

    constructor(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
        utfall: UtbetalingsvedtakFattetHendelse.Utfall,
        iverksettingsdager: MutableList<IverksettingDag>,
    ) : this(
        id = UUID.randomUUID(),
        vedtakId = vedtakId,
        behandlingId = behandlingId,
        vedtakstidspunkt = vedtakstidspunkt,
        virkningsdato = virkningsdato,
        utfall = utfall,
        iverksettingsdager = iverksettingsdager,
    )

    fun id() = this.id

    fun accept(visitor: IverksettingVisitor) {
        visitor.visitIverksetting(vedtakId, behandlingId, vedtakstidspunkt, virkningsdato, utfall)
        visitAlleIverksettingsdager(visitor)
    }

    override fun compareTo(other: Iverksetting) = this.vedtakstidspunkt.compareTo(other.vedtakstidspunkt)

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            "Iverksetting",
            mapOf(
                "iverksettingId" to id.toString(),
                "vedtakId" to vedtakId.toString(),
                "behandlingId" to behandlingId.toString(),
                "vedtakstidspunkt" to vedtakstidspunkt.toString(),
                "virkningsdato" to virkningsdato.toString(),
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
