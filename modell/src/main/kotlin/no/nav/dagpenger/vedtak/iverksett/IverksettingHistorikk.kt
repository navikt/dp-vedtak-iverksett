package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.entitet.TemporalCollection
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingHistorikkVisitor
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import java.time.LocalDate

class IverksettingHistorikk internal constructor(private val iverksettinger: MutableList<Iverksetting>) {

    internal constructor() : this(mutableListOf<Iverksetting>())

    private val observers = mutableSetOf<IverksettingObserver>()

    private val beløpTilUtbetalingHistorikk = TemporalCollection<Beløp>()

    init {
        iverksettinger.forEach { HistorikkOppdaterer(this).apply(it::accept) }
    }
    fun addObserver(iverksettingObserver: IverksettingObserver) {
        this.observers.add(iverksettingObserver)
    }

    fun accept(visitor: IverksettingHistorikkVisitor) {
        iverksettinger.forEach { it.accept(visitor) }
    }

    fun beløpTilUtbetalingFor(dato: LocalDate): Beløp = beløpTilUtbetalingHistorikk.get(dato)

    internal fun leggTilIverksetting(iverksetting: Iverksetting) {
        this.iverksettinger.add(
            iverksetting.also {
                HistorikkOppdaterer(this).apply(it::accept)
            },
        )
//  TODO?      this.observers.forEach { iverksettingObserver ->
//            iverksettingObserver.utbetalingsvedtakIverksatt().apply(iverksettinger::accept).utbetalingsvedtakIverksatt
//
//        }
    }

//    internal fun harBehandlet(behandlingId: UUID) = this.iverksettinger.harBehandlet(behandlingId)

    private class HistorikkOppdaterer(private val iverksettingHistorikk: IverksettingHistorikk) : IverksettingVisitor {

        private var dato: LocalDate? = null

        private fun dato() = requireNotNull(dato) { " Forventet at dato er satt. Har du husket preVisit???" }

// TODO
//        override fun visitIverksettingDag(
//            iverksettingId: UUID,
//            vedtakId: UUID,
//            personIdent: PersonIdentifikator,
//            dato: LocalDate,
//        ) {
//            // iverksettingHistorikk.beløpTilUtbetalingHistorikk.put(dato, beløpTilUtbetaling)
//        }
    }
}
