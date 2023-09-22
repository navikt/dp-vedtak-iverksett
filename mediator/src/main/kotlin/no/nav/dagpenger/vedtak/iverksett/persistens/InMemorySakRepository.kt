package no.nav.dagpenger.vedtak.iverksett.persistens

import no.nav.dagpenger.vedtak.iverksett.Sak

class InMemorySakRepository : SakRepository {
    private val sakDb = mutableMapOf<String, Sak>()
    override fun hent(sakId: String): Sak? = sakDb[sakId]
    override fun lagre(sak: Sak) {
        sakDb[sak.sakId] = sak
    }
}
