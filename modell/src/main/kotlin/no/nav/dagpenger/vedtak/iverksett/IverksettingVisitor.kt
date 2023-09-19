package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import java.util.UUID

interface IverksettingVisitor : AktivitetsloggVisitor {

    fun visitIverksetting(id: UUID, vedtakId: UUID, personIdent: PersonIdentifikator, tilstand: Iverksetting.Tilstand) {}
}
