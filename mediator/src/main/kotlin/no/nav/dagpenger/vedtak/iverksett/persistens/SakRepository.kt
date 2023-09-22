package no.nav.dagpenger.vedtak.iverksett.persistens

import no.nav.dagpenger.vedtak.iverksett.Sak

interface SakRepository {
    fun hent(sakId: String): Sak?
    fun lagre(sak: Sak)
}

internal class SakId(private val sakId: String)
