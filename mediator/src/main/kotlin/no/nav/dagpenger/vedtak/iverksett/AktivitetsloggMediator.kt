package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.serde.AktivitetsloggJsonBuilder
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class AktivitetsloggMediator(private val rapidsConnection: RapidsConnection) {
    fun håndter(hendelse: UtbetalingsvedtakFattetHendelse) {
        rapidsConnection.publish(aktivitetsloggJson(hendelse))
    }

    private fun aktivitetsloggJson(hendelse: UtbetalingsvedtakFattetHendelse) =
        JsonMessage.newMessage(
            eventName = "aktivitetslogg",
            mapOf(
                "hendelse" to
                    mapOf(
                        "type" to hendelse.toSpesifikkKontekst().kontekstType,
                        "meldingsreferanseId" to hendelse.meldingsreferanseId(),
                    ),
                "ident" to hendelse.ident(),
                "aktiviteter" to AktivitetsloggJsonBuilder(hendelse).asList(),
            ),
        ).toJson()
}
