package no.nav.dagpenger.vedtak.iverksett

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksett.client.IverksettClient
import no.nav.dagpenger.vedtak.iverksett.persistens.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.iverksett.persistens.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.vedtak.iverksett.persistens.PostgresSakRepository
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val sakRepository = PostgresSakRepository(PostgresDataSourceBuilder.dataSource)

    private val rapidsConnection =
        RapidApplication.Builder(
            RapidApplication.RapidApplicationConfig.fromEnv(config),
        ).build()

    init {
        rapidsConnection.register(this)

        val sakMediator =
            SakMediator(
                sakRepository = sakRepository,
                iverksettClient = IverksettClient(tokenProvider = Configuration.iverksettClientTokenSupplier),
            )

        UtbetalingsvedtakFattetMottak(rapidsConnection, sakMediator)
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration()
        logger.info { "Starter opp dp-vedtak-iverksett" }
    }
}
