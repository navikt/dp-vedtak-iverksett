package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Iverksetting private constructor(
    private val id: UUID,
    private val vedtakId: UUID,
    private val behandlingId: UUID,
    private val virkningsdato: LocalDate,
    private val vedtakstidspunkt: LocalDateTime,
    private val iverksettingsdager: MutableList<IverksettingDag>,
) : Aktivitetskontekst, Comparable<Iverksetting> {

    constructor(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        iverksettingsdager: MutableList<IverksettingDag>,
    ) : this(
        id = UUID.randomUUID(),
        vedtakId = vedtakId,
        behandlingId = behandlingId,
        virkningsdato = virkningsdato,
        vedtakstidspunkt = vedtakstidspunkt,
        iverksettingsdager = iverksettingsdager,
    )

    fun id() = this.id

    fun accept(visitor: IverksettingVisitor) {
        visitor.visitIverksetting(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt)
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
                "virkningsdato" to virkningsdato.toString(),
            ),
        )
    }

    private fun visitAlleIverksettingsdager(visitor: IverksettingVisitor) {
        iverksettingsdager.forEach { iverksettingDag ->
            iverksettingDag.accept(visitor)
        }
    }
}
