package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse

class Sak(
    private val ident: PersonIdentifikator,
    val sakId: String,
    private val iverksettingHistorikk: IverksettingHistorikk,
) {
    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        // kontekst(utbetalingsvedtakFattetHendelse)

// TDOD duplikatkontroll

        val iverksetting = utbetalingsvedtakFattetHendelse.mapTilIverksetting()

        utbetalingsvedtakFattetHendelse.info("Mottatt hendelse om fattet utbetalingsvedtak.")
        // this.leggTilIverksetting(iverksetting)

        TODO("Gjør noe fornuftig")
    }
}
