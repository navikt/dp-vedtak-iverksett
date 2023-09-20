package no.nav.dagpenger.vedtak.iverksett.visitor

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator

interface PersonVisitor :
    IverksettingHistorikkVisitor,
    SakVisitor,
    AktivitetsloggVisitor {

    fun visitPerson(personIdentifikator: PersonIdentifikator) {}
}
