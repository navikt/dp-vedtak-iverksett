package no.nav.dagpenger.vedtak.iverksett

import mu.KLogger
import mu.KotlinLogging
import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.vedtak.iverksett.hendelser.Hendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

class BehovMediator(
    private val rapidsConnection: RapidsConnection,
    private val sikkerLogg: KLogger,
) {

    private companion object {
        val logger = KotlinLogging.logger { }
    }

    internal fun håndter(hendelse: Hendelse) {
        hendelse.kontekster().forEach { håndter(hendelse, it.behov()) }
    }

    private fun håndter(
        hendelse: Hendelse,
        behov: List<Aktivitet.Behov>,
    ) {
        behov
            .groupBy { it.kontekst() }
            .onEach { (_, behovMap) ->
                require(
                    behovMap.size == behovMap.map { it.type.name }
                        .toSet().size,
                ) { "Kan ikke produsere samme behov på samme kontekst" }
            }
            .forEach { (kontekst, behov) ->
                val behovMap: Map<String, Map<String, Any>> =
                    behov.associate { enkeltBehov -> enkeltBehov.type.name to enkeltBehov.detaljer() }
                val behovParametere =
                    behovMap.values.fold<Map<String, Any>, Map<String, Any>>(emptyMap()) { acc, map -> acc + map }
                (kontekst + behovMap + behovParametere).let { JsonMessage.newNeed(behovMap.keys, it) }
                    .also { message ->
                        sikkerLogg.info("sender behov for {}:\n{}", behovMap.keys, message.toJson())
                        rapidsConnection.publish(hendelse.ident(), message.toJson())
                        logger.info("Sender behov for {}", behovMap.keys)
                    }
            }
    }
}
