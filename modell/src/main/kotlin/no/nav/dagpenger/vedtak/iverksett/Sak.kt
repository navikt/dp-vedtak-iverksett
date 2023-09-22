package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse

class Sak(
    private val ident: PersonIdentifikator,
    private val sakId: String,
    private val iverksettingHistorikk: IverksettingHistorikk,
) {
    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        // kontekst(utbetalingsvedtakFattetHendelse)
        val iverksetting = utbetalingsvedtakFattetHendelse.tilIverksetting()
        TODO("Gjør noe fornuftig")
    }
}
