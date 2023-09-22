package no.nav.dagpenger.vedtak.iverksett.visitor

import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator
import no.nav.dagpenger.vedtak.iverksett.SakId

interface SakVisitor : IverksettingHistorikkVisitor {
    fun visitSak(ident: PersonIdentifikator, sakId: SakId) {}
}
