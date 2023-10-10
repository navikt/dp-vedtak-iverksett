package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor

class Sak(
    private val ident: PersonIdentifikator,
    private val sakId: SakId,
    private val iverksettinger: MutableList<Iverksetting>,
) : Aktivitetskontekst {

    companion object {
        val kontekstType: String = "Sak"
        fun rehydrer(
            ident: PersonIdentifikator,
            sakId: SakId,
            iverksettinger: List<Iverksetting>,
        ): Sak {
            return Sak(
                ident = ident,
                sakId = sakId,
                iverksettinger = mutableListOf() //TODO Fix
            )
        }
    }
    fun sakId() = sakId

    fun accept(visitor: SakVisitor) {
        visitor.visitSak(ident, sakId)
        visitAlleIverksettinger(visitor)
    }

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        utbetalingsvedtakFattetHendelse.kontekst(this)
        utbetalingsvedtakFattetHendelse.info("Utbetalingsvedtak håndteres i Sak.")
        val iverksetting = utbetalingsvedtakFattetHendelse.mapTilIverksetting()
        utbetalingsvedtakFattetHendelse.kontekst(iverksetting)
        utbetalingsvedtakFattetHendelse.info("Utbetalingsvedtak fører til iverksetting")
        this.iverksettinger.add(iverksetting)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst("sak", mapOf("sakId" to sakId.sakId))
    }

    private fun visitAlleIverksettinger(visitor: IverksettingVisitor) =
        iverksettinger.forEach { iverksetting -> iverksetting.accept(visitor) }
}
