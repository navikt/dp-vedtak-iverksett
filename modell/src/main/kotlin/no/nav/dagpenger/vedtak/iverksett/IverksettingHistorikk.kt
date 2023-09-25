package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.entitet.TemporalCollection
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingHistorikkVisitor
import java.time.LocalDate

class IverksettingHistorikk(private val iverksettinger: MutableList<Iverksetting>) {
    constructor() : this(mutableListOf<Iverksetting>())

    fun accept(visitor: IverksettingHistorikkVisitor) {
        visitor.visitIverksettingHistorikk(iverksettinger)
        visitAlleIverksettinger(visitor)
    }

    private val beløpTilUtbetalingHistorikk = TemporalCollection<Beløp>()

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        this.iverksettinger.add(
            utbetalingsvedtakFattetHendelse.mapTilIverksetting().also { iverksetting ->
                iverksetting.iverksettingsdager.forEach { iverksettingDag ->
                    beløpTilUtbetalingHistorikk.put(iverksettingDag.dato, iverksettingDag.beløp)
                }
            },
        )
    }

    fun beløpTilUtbetalingFor(dato: LocalDate): Beløp {
        if (beløpTilUtbetalingHistorikk.harHistorikk()) {
            var sisteDato: LocalDate = LocalDate.MIN
            this.iverksettinger.forEach { iverksetting ->
                iverksetting.iverksettingsdager.forEach { dag ->
                    if (sisteDato < dag.dato) {
                        sisteDato = dag.dato
                    }
                }
            }
            if (sisteDato > LocalDate.MIN) {
                return beløpTilUtbetalingHistorikk.get(dato)
            } else {
                throw IllegalArgumentException("Iverksettingshistorikken har ingen utbetaling for dato $dato")
            }
        } else {
            throw IllegalArgumentException("Det finnes ingen iverksettinger ennå")
        }
    }

    private fun visitAlleIverksettinger(visitor: IverksettingHistorikkVisitor) {
        iverksettinger.forEach { iverksetting ->
            iverksetting.accept(visitor)
        }
    }
}
