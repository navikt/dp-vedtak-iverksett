package no.nav.dagpenger.vedtak.iverksett

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksett.melding.HendelseMediator
import no.nav.dagpenger.vedtak.iverksett.persistens.InMemoryIverksettingRepository
import no.nav.dagpenger.vedtak.iverksett.persistens.InMemoryMeldingRepository
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config),
    ).build()

    init {
        rapidsConnection.register(this)

        HendelseMediator(
            rapidsConnection = rapidsConnection,
            hendelseRepository = InMemoryMeldingRepository(),
            iverksettingMediator = IverksettingMediator(
                aktivitetsloggMediator = AktivitetsloggMediator(rapidsConnection),
                iverksettingRepository = InMemoryIverksettingRepository(),
                behovMediator = BehovMediator(rapidsConnection, KotlinLogging.logger("tjenestekall.BehovMediator")),
            ),
        )
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-vedtak-iverksett" }
    }
}
