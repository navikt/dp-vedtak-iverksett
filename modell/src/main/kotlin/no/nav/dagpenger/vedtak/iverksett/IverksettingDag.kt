package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingDagVisitor
import java.time.LocalDate

class IverksettingDag(private val dato: LocalDate, private val beløp: Beløp) {
    fun accept(visitor: IverksettingDagVisitor) {
        visitor.visitIverksettingDag(dato, beløp)
    }
}