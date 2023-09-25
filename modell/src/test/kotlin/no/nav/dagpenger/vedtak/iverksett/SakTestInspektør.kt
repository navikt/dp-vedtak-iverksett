package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor

internal class SakTestInspektør(sak: Sak) : SakVisitor {

    val iverksettinger = mutableListOf<Iverksetting>()
    lateinit var ident: PersonIdentifikator
    lateinit var sakId: SakId

    init {
        sak.accept(this)
    }

    override fun visitSak(ident: PersonIdentifikator, sakId: SakId) {
        this.ident = ident
        this.sakId = sakId
    }

    override fun visitIverksettingHistorikk(iverksettinger: MutableList<Iverksetting>) {
        iverksettinger.forEach { iverksetting ->
            this.iverksettinger.add(iverksetting)
        }
    }
}
