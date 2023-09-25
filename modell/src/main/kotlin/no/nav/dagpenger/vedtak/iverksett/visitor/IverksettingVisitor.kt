package no.nav.dagpenger.vedtak.iverksett.visitor

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator
import java.time.LocalDate
import java.util.UUID

interface IverksettingVisitor : AktivitetsloggVisitor {
    fun visitIverksetting(id: UUID, vedtakId: UUID, personIdent: PersonIdentifikator, virkningsdato: LocalDate) {}
}
