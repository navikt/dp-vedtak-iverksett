package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.iverksett.Sak.Companion.finnSak
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.hendelser.Hendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.PersonVisitor
import java.time.LocalDate

class Person internal constructor(
    private val ident: PersonIdentifikator,
    private val saker: MutableList<Sak>,
    internal val iverksettingHistorikk: IverksettingHistorikk,
) : Aktivitetskontekst, IverksettingObserver {

    init {
        iverksettingHistorikk.addObserver(this)
    }

    constructor(ident: PersonIdentifikator) : this(
        ident = ident,
        saker = mutableListOf(),
        iverksettingHistorikk = IverksettingHistorikk(),
    )

    companion object {
        val kontekstType: String = "Person"
//  TODO      fun rehydrer(
//            ident: PersonIdentifikator,
//            saker: MutableList<Sak>,
//            vedtak: List<Vedtak>,
//            perioder: List<Rapporteringsperiode>,
//        ): Person {
//            return Person(
//                ident = ident,
//                saker = saker,
//                vedtakHistorikk = VedtakHistorikk(vedtak.toMutableList()),
//                rapporteringsperioder = Rapporteringsperioder(perioder),
//            )
//        }
    }

    private val observers = mutableListOf<PersonObserver>()

    fun ident() = ident

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        kontekst(utbetalingsvedtakFattetHendelse)
        val sak = finnEllerOpprettSak(utbetalingsvedtakFattetHendelse)
        sak.håndter(utbetalingsvedtakFattetHendelse)
    }

    private fun finnEllerOpprettSak(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) =
        saker.finnSak(utbetalingsvedtakFattetHendelse.sakId) ?: Sak(
            utbetalingsvedtakFattetHendelse.sakId,
            this,
        )

    fun beløpTilUtbetalingFor(dato: LocalDate): Beløp = iverksettingHistorikk.beløpTilUtbetalingFor(dato)

    internal fun leggTilIverksetting(iverksetting: Iverksetting) {
        iverksettingHistorikk.leggTilIverksetting(iverksetting)
    }

//    override fun utbetalingsvedtakIverksatt(utbetalingsvedtakIverksatt: VedtakObserver.UtbetalingsvedtakIverksatt) {
//        observers.forEach {
//            it.utbetalingsvedtakIverksatt(ident.identifikator(), utbetalingsvedtakIverksatt)
//        }
//    }

    fun addObserver(personObserver: PersonObserver) {
        observers.add(personObserver)
    }

    fun accept(visitor: PersonVisitor) {
        visitor.visitPerson(ident)
        saker.forEach { sak ->
            sak.accept(visitor)
        }
        iverksettingHistorikk.accept(visitor)
    }

    internal fun leggTilSak(sak: Sak) {
        saker.add(sak)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst =
        SpesifikkKontekst(kontekstType, mapOf("ident" to ident.identifikator()))

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
    }
}
