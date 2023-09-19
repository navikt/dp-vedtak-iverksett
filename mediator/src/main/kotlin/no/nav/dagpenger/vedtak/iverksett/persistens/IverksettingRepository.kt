package no.nav.dagpenger.vedtak.iverksett.persistens

import no.nav.dagpenger.vedtak.iverksett.Iverksetting
import java.util.UUID

interface IverksettingRepository {
    fun hent(vedtakId: UUID): Iverksetting?
    fun lagre(iverksetting: Iverksetting)
}
