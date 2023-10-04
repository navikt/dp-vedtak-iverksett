package no.nav.dagpenger.vedtak.iverksett.persistens

import no.nav.dagpenger.vedtak.iverksett.Sak
import no.nav.dagpenger.vedtak.iverksett.SakId

class InMemorySakRepository : SakRepository {
    private val sakDb = mutableMapOf<SakId, Sak>()

    override fun hent(sakId: SakId): Sak? = sakDb[sakId]

    override fun lagre(sak: Sak) {
        sakDb[sak.sakId()] = sak
    }
}
