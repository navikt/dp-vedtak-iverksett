package no.nav.dagpenger.vedtak.iverksett.visitor

import no.nav.dagpenger.vedtak.iverksett.Iverksetting

interface IverksettingHistorikkVisitor : IverksettingVisitor {
    fun visitIverksettingHistorikk(iverksettinger: MutableList<Iverksetting>) {}
}
