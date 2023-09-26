package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor

internal class SakTestInspektør(sak: Sak) : SakVisitor {

    val iverksettinger = mutableListOf<Iverksetting>()
    lateinit var ident: PersonIdentifikator
    lateinit var sakId: SakId
    lateinit var iverksettingHistorikk: IverksettingHistorikk

    init {
        sak.accept(this)
    }

    override fun visitSak(ident: PersonIdentifikator, sakId: SakId, iverksettingHistorikk: IverksettingHistorikk) {
        this.ident = ident
        this.sakId = sakId
        this.iverksettingHistorikk = iverksettingHistorikk
    }

    override fun visit(iverksettinger: MutableList<Iverksetting>) {
        iverksettinger.forEach { iverksetting ->
            this.iverksettinger.add(iverksetting)
        }
    }
}
