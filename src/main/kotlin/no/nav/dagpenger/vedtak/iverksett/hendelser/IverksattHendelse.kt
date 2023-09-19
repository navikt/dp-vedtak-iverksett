package no.nav.dagpenger.vedtak.iverksett.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import java.util.UUID

class IverksattHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val vedtakId: UUID,
    val iverksettingId: UUID,
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : Hendelse(meldingsreferanseId, ident, aktivitetslogg) {

    override fun kontekstMap(): Map<String, String> = mapOf("iverksettingId" to iverksettingId.toString())
}
