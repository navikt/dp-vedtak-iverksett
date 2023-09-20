package no.nav.dagpenger.vedtak.iverksett

interface PersonObserver {

    fun utbetalingsvedtakIverksatt(ident: String, utbetalingsvedtakIverksatt: IverksettingObserver.UtbetalingsvedtakIverksatt) {}
}
