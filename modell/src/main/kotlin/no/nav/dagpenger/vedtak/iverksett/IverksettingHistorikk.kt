package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.entitet.TemporalCollection
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingHistorikkVisitor

class IverksettingHistorikk(private val iverksettinger: MutableList<Iverksetting>) {
    fun accept(visitor: IverksettingHistorikkVisitor) {
        visitAlleIverksettinger(visitor)
    }

    // TODO skal konstruktøren være public?
    constructor() : this(mutableListOf<Iverksetting>())

    private val beløpTilUtbetalingHistorikk = TemporalCollection<Beløp>()

    private fun visitAlleIverksettinger(visitor: IverksettingHistorikkVisitor) {
        iverksettinger.forEach { iverksetting ->
            iverksetting.accept(visitor)
        }
    }

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        this.iverksettinger.add(utbetalingsvedtakFattetHendelse.mapTilIverksetting())
    }
}
