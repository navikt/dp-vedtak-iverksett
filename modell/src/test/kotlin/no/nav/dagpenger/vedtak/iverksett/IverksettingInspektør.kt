package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import java.util.UUID

internal class IverksettingInspektør(iverksetting: Iverksetting) : IverksettingVisitor {

    init {
        iverksetting.accept(this)
    }

    lateinit var iverksettingId: UUID
    lateinit var vedtakId: UUID
    lateinit var tilstand: Iverksetting.Tilstand
    // lateinit var innsendingLogg: Aktivitetslogg

    override fun visitIverksetting(
        id: UUID,
        vedtakId: UUID,
        personIdent: PersonIdentifikator,
        tilstand: Iverksetting.Tilstand,
    ) {
        this.iverksettingId = id
        this.vedtakId = vedtakId
        this.tilstand = tilstand
    }

//    override fun postVisitAktivitetslogg(aktivitetslogg: Aktivitetslogg) {
//       // this.innsendingLogg = aktivitetslogg
//    }
}
