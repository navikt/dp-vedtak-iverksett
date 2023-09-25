package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingHistorikkVisitor
import java.time.LocalDate

class IverksettingHistorikk(private val iverksettinger: MutableList<Iverksetting>) {
    constructor() : this(mutableListOf<Iverksetting>())

    fun accept(visitor: IverksettingHistorikkVisitor) {
        visitor.visitIverksettingHistorikk(iverksettinger)
        visitAlleIverksettinger(visitor)
    }

    private val beløpTilUtbetalingForDag = mutableMapOf<LocalDate, Beløp>()

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        this.iverksettinger.add(
            utbetalingsvedtakFattetHendelse.mapTilIverksetting().also { iverksetting ->
                iverksetting.iverksettingsdager.forEach { iverksettingDag ->
                    beløpTilUtbetalingForDag.put(iverksettingDag.dato, iverksettingDag.beløp)
                }
            },
        )
    }

    fun beløpTilUtbetalingForDag(dato: LocalDate): Beløp {
        val beløp: Beløp? = beløpTilUtbetalingForDag.get(dato)
        if (beløp != null) {
            return beløp
        } else {
            throw IllegalArgumentException("Det finnes ingen iverksetting med dato $dato")
        }
    }

    private fun visitAlleIverksettinger(visitor: IverksettingHistorikkVisitor) {
        iverksettinger.forEach { iverksetting ->
            iverksetting.accept(visitor)
        }
    }
}
