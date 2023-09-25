package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor

data class SakId(val sakId: String)

class Sak(
    private val ident: PersonIdentifikator,
    private val sakId: SakId,
    private val iverksettingHistorikk: IverksettingHistorikk,
) {

    fun accept(visitor: SakVisitor) {
        visitor.visitSak(ident, sakId)
        visitor.visitSak(iverksettingHistorikk)
        iverksettingHistorikk.accept(visitor)
    }

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        utbetalingsvedtakFattetHendelse.info("Mottatt hendelse om fattet utbetalingsvedtak.")
        iverksettingHistorikk.håndter(utbetalingsvedtakFattetHendelse)
    }

    fun sakId() = sakId
}
