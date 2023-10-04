package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.serde.AktivitetsloggJsonBuilder
import no.nav.dagpenger.vedtak.iverksett.hendelser.Hendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class AktivitetsloggMediator(private val rapidsConnection: RapidsConnection) {
    fun håndter(hendelse: Hendelse) {
        rapidsConnection.publish(
            JsonMessage.newMessage(
                "aktivitetslogg",
                mapOf(
                    "hendelse" to
                        mapOf(
                            "type" to hendelse.toSpesifikkKontekst().kontekstType,
                            "meldingsreferanseId" to hendelse.meldingsreferanseId(),
                        ),
                    "ident" to hendelse.ident(),
                    "aktiviteter" to AktivitetsloggJsonBuilder(hendelse).asList(),
                ),
            ).toJson(),
        )
    }
}
