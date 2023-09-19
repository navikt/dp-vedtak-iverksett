package no.nav.dagpenger.vedtak.iverksett.hendelser

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import java.util.UUID

abstract class VedtakFattetHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val vedtakId: UUID,
    val behandlingId: UUID,
    val sakId: String,
    aktivitetslogg: Aktivitetslogg,
) : Hendelse(meldingsreferanseId, ident, aktivitetslogg) {
    override fun kontekstMap(): Map<String, String> =
        mapOf("vedtakId" to vedtakId.toString(), "behandlingId" to behandlingId.toString())
}
