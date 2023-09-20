package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.persistens.IverksettingRepository
import java.util.UUID

class InMemoryIverksettingRepository : IverksettingRepository {
    private val iverksettingDb = mutableMapOf<UUID, Iverksetting>()

    override fun hent(vedtakId: UUID): Iverksetting? = iverksettingDb[vedtakId]

    override fun lagre(iverksetting: Iverksetting) {
        iverksettingDb[iverksetting.vedtakId] = iverksetting
    }
}
