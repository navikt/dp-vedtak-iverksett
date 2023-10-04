package no.nav.dagpenger.vedtak.iverksett.persistens

import no.nav.dagpenger.vedtak.iverksett.Sak
import no.nav.dagpenger.vedtak.iverksett.SakId

interface SakRepository {
    fun hent(sakId: SakId): Sak?

    fun lagre(sak: Sak)
}
