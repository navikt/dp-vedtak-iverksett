package no.nav.dagpenger.vedtak.iverksett.persistens

import no.nav.dagpenger.vedtak.iverksett.Iverksetting
import java.util.UUID

class InMemoryIverksettingRepository : IverksettingRepository {
    private val iverksettingDb = mutableMapOf<UUID, Iverksetting>()

    override fun hent(vedtakId: UUID): Iverksetting? = iverksettingDb[vedtakId]

    override fun lagre(iverksetting: Iverksetting) {
        iverksettingDb[iverksetting.vedtakId] = iverksetting
    }
}
