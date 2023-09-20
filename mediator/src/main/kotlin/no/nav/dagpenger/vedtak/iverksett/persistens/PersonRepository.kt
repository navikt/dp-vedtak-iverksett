package no.nav.dagpenger.vedtak.iverksett.persistens

import no.nav.dagpenger.vedtak.iverksett.Person
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator

interface PersonRepository {
    fun hent(ident: PersonIdentifikator): Person?
    fun lagre(person: Person)
}
