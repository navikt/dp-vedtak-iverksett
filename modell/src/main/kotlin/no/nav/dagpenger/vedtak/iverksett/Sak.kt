package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.iverksett.hendelser.Hendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor

typealias SakId = String

class Sak private constructor(
    private val sakId: SakId,
    private val person: Person,
    val iverksettinger: MutableList<Iverksetting>,
) : Aktivitetskontekst {
    constructor(sakId: SakId, person: Person) : this(
        sakId = sakId,
        person = person,
        iverksettinger = mutableListOf(),
    )

    init {
        person.leggTilSak(this)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst("sak", mapOf("sakId" to sakId))
    }

    fun accept(sakVisitor: SakVisitor) {
        sakVisitor.visitSak(sakId)
    }

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        kontekst(utbetalingsvedtakFattetHendelse)
        val iverksetting = utbetalingsvedtakFattetHendelse.tilIverksetting()
        iverksettinger.add(iverksetting)
        person.leggTilIverksetting(iverksetting)
    }

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
    }

    companion object {
        internal fun Collection<Sak>.finnSak(sakId: String): Sak? {
            return this.find { it.sakId == sakId }
        }
    }
}
