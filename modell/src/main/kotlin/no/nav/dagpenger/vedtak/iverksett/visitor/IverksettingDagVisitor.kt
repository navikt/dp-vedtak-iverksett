package no.nav.dagpenger.vedtak.iverksett.visitor

import no.nav.dagpenger.aktivitetslogg.AktivitetsloggVisitor
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import java.time.LocalDate

interface IverksettingDagVisitor : AktivitetsloggVisitor {
    fun visitIverksettingDag(
        dato: LocalDate,
        beløp: Beløp,
    ) {}
}
