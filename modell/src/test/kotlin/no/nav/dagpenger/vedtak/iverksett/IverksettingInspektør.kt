package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import java.util.UUID

internal class IverksettingInspektør(iverksetting: Iverksetting) : IverksettingVisitor {

    init {
        iverksetting.accept(this)
    }

    lateinit var iverksettingId: UUID
    lateinit var vedtakId: UUID

    override fun visitIverksetting(id: UUID, vedtakId: UUID, personIdent: PersonIdentifikator) {
        this.iverksettingId = id
        this.vedtakId = vedtakId
    }
}
