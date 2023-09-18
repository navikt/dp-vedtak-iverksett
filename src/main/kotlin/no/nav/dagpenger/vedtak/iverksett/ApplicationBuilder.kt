package no.nav.dagpenger.vedtak.iverksett

import mu.KotlinLogging
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

        IverksettUtbetalingBehovløser(
            rapidsConnection = rapidsConnection,
            iverksettClient = IverksettClient(
                Configuration.iverksettApiUrl,
                Configuration.iverksettClientTokenSupplier,
            ),
        )
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-vedtak-iverksett" }
    }
}
