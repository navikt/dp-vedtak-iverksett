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
    private val id: UUID,
    private val personIdent: PersonIdentifikator,
    private val vedtakId: UUID,
    private val virkningsdato: LocalDate,
    internal val iverksettingsdager: MutableList<IverksettingDag>,
) : Aktivitetskontekst {

    constructor(vedtakId: UUID, ident: String, virkningsdato: LocalDate, iverksettingsdager: MutableList<IverksettingDag>) : this(
        id = UUID.randomUUID(),
        personIdent = ident.tilPersonIdentfikator(),
        vedtakId = vedtakId,
        virkningsdato = virkningsdato,
        iverksettingsdager = iverksettingsdager,
    )

    fun accept(iverksettingVisitor: IverksettingVisitor) {
        iverksettingVisitor.visitIverksetting(id, vedtakId, personIdent, virkningsdato)
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
                "virkningsdato" to virkningsdato.toString(),
                "ident" to personIdent.identifikator(),
            ),
        )
    }

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
    }
}

// TODO set private og bruk visitor??
class IverksettingDag(internal val dato: LocalDate, internal val beløp: Beløp)
