package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.hendelser.Hendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.IverksettingVisitor
import java.time.LocalDate
import java.util.UUID

class Iverksetting private constructor(
    val id: UUID,
    private val personIdent: PersonIdentifikator,
    val vedtakId: UUID,
    private val iverksettingsdager: MutableList<IverksettingDag>,
) : Aktivitetskontekst {

    constructor(vedtakId: UUID, ident: String, iverksettingsdager: MutableList<IverksettingDag>) : this(
        id = UUID.randomUUID(),
        personIdent = ident.tilPersonIdentfikator(),
        vedtakId = vedtakId,
        iverksettingsdager = iverksettingsdager,
    )

    fun accept(iverksettingVisitor: IverksettingVisitor) {
        iverksettingVisitor.visitIverksetting(id, vedtakId, personIdent)
    }

    fun håndter(utbetalingsvedtakFattetHendelse: UtbetalingsvedtakFattetHendelse) {
        kontekst(utbetalingsvedtakFattetHendelse)
        // val iverksetting = utbetalingsvedtakFattetHendelse.tilIverksetting()
        TODO("Gjør noe fornuftig")
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst(
            "Iverksetting",
            mapOf(
                "iverksettingId" to id.toString(),
                "vedtakId" to vedtakId.toString(),
                "ident" to personIdent.identifikator(),
            ),
        )
    }

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
    }
}

class IverksettingDag(private val dato: LocalDate, private val beløp: Beløp)
