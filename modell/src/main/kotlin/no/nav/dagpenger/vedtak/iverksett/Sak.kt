package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor

class Sak(
    private val ident: PersonIdentifikator,
    private val sakId: SakId,
    private val iverksettinger: MutableList<Iverksetting>,
) {
    fun sakId() = sakId

    fun accept(visitor: SakVisitor) {
        visitor.visitSak(ident, sakId)
        visitAlleIverksettinger(visitor)
    }

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        utbetalingsvedtakFattetHendelse.info("Mottatt hendelse om fattet utbetalingsvedtak.")
        val iverksetting = utbetalingsvedtakFattetHendelse.mapTilIverksetting()
        this.iverksettinger.add(iverksetting)
    }

    private fun visitAlleIverksettinger(visitor: IverksettingVisitor) =
        iverksettinger.forEach { iverksetting -> iverksetting.accept(visitor) }
}
