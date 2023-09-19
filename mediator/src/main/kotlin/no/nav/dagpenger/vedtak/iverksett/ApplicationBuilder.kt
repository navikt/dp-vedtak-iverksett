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

//        HendelseMediator(
//            rapidsConnection = rapidsConnection,
//            hendelseRepository = PostgresHendelseRepository(PostgresDataSourceBuilder.dataSource),
//            personMediator = PersonMediator(
//                aktivitetsloggMediator = AktivitetsloggMediator(rapidsConnection),
//                personRepository = personRepository,
//                personObservers = listOf(
//                    VedtakFattetObserver(rapidsConnection),
//                ),
//            ),
//            iverksettingMediator = IverksettingMediator(
//                aktivitetsloggMediator = AktivitetsloggMediator(rapidsConnection),
//                iverksettingRepository = PostgresIverksettingRepository(PostgresDataSourceBuilder.dataSource),
//                behovMediator = BehovMediator(rapidsConnection, KotlinLogging.logger("tjenestekall.BehovMediator")),
//            ),
//        )
    }

    fun start() = rapidsConnection.start()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-vedtak-iverksett" }
    }
}
