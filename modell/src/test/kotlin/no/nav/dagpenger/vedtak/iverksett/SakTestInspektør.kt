package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor

internal class SakTestInspektør(sak: Sak) : SakVisitor {

    init {
        sak.accept(this)
    }

    val iverksettinger = mutableListOf<Iverksetting>()
    lateinit var ident: PersonIdentifikator
    lateinit var sakId: SakId

    override fun visitSak(ident: PersonIdentifikator, sakId: SakId) {
        this.ident = ident
        this.sakId = sakId
    }

    override fun visitIverksettingHistorikk(iverksettinger: MutableList<Iverksetting>) {
        this.iverksettinger.addAll(iverksettinger)
        println("Halla! Nå er vi i iverksettingHistorikk")
    }
}
