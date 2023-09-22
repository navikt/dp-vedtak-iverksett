package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.entitet.TemporalCollection

class IverksettingHistorikk(private val iverksettinger: MutableList<Iverksetting>) {

    // TODO skal konstruktøren være public?
    constructor() : this(mutableListOf<Iverksetting>())

    private val beløpTilUtbetalingHistorikk = TemporalCollection<Beløp>()
}
