package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import java.time.LocalDate
import java.util.UUID

class Iverksetting private constructor(
    private val id: UUID,
    private val vedtakId: UUID,
    private val virkningsdato: LocalDate,
    private val iverksettingsdager: MutableList<IverksettingDag>,
) : Aktivitetskontekst {

    constructor(
        vedtakId: UUID,
        virkningsdato: LocalDate,
        iverksettingsdager: MutableList<IverksettingDag>,
    ) : this(
        id = UUID.randomUUID(),
        vedtakId = vedtakId,
        virkningsdato = virkningsdato,
        iverksettingsdager = iverksettingsdager,
    )

    fun accept(visitor: IverksettingVisitor) {
        visitor.visitIverksetting(id, vedtakId, virkningsdato)
        visitAlleIverksettingsdager(visitor)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            "Iverksetting",
            mapOf(
                "iverksettingId" to id.toString(),
                "vedtakId" to vedtakId.toString(),
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
