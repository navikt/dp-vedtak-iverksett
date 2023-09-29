package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class IverksettDtoBuilder(sak: Sak) : SakVisitor {

    lateinit var virkningsdato: LocalDate
    lateinit var vedtakstidspunkt: LocalDateTime
    lateinit var utfall: UtbetalingsvedtakFattetHendelse.Utfall
    lateinit var vedtakId: UUID
    lateinit var behandlingId: UUID
    lateinit var ident: PersonIdentifikator
    lateinit var sakId: SakId
    val iverksettinger = mutableListOf<Iverksetting>()
    val iverksettingsdager = mutableListOf<IverksettingDagKopi>()

    init {
        sak.accept(this)
    }

    fun bygg(): IverksettDto {
        // println("SakInspektør: Bygger IverksettDto for iverksetting av vedtakId $vedtakId - behandlingId $behandlingId")
        return IverksettDto(
            saksreferanse = sakId.sakId,
            behandlingId = behandlingId,
            personIdent = ident.identifikator(),
            forrigeIverksetting = forrigeIverksettingDto(),
            vedtak = VedtaksdetaljerDto(
                vedtakstype = VedtakType.UTBETALINGSVEDTAK,
                vedtakstidspunkt = vedtakstidspunkt,
                resultat = when (utfall) {
                    UtbetalingsvedtakFattetHendelse.Utfall.Innvilget -> Vedtaksresultat.INNVILGET
                    UtbetalingsvedtakFattetHendelse.Utfall.Avslått -> Vedtaksresultat.AVSLÅTT
                },
                utbetalinger = finnUtbetalingsdager(),
                saksbehandlerId = "DIGIDAG",
                beslutterId = "DIGIDAG",
                vedtaksperioder = listOf(
                    VedtaksperiodeDto(
                        fraOgMedDato = virkningsdato,
                    ),
                ),
            ),
        )
    }

    private fun forrigeBehandlingId(): UUID? {
        val forrigeIverksetting = forrigeIverksetting()
        return if (forrigeIverksetting != null) {
            BehandlingIdVisitor(forrigeIverksetting).behandlingId
        } else {
            null
        }
    }

    override fun visitSak(ident: PersonIdentifikator, sakId: SakId) {
        this.ident = ident
        this.sakId = sakId
    }

    override fun visitIverksetting(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
        utfall: UtbetalingsvedtakFattetHendelse.Utfall,
    ) {
        this.vedtakId = vedtakId
        this.behandlingId = behandlingId
        this.vedtakstidspunkt = vedtakstidspunkt
        this.virkningsdato = virkningsdato
        this.utfall = utfall
        this.iverksettinger.add(Iverksetting(vedtakId, behandlingId, vedtakstidspunkt, virkningsdato, utfall, mutableListOf()))
    }

    override fun visitIverksettingDag(dato: LocalDate, beløp: Beløp) {
        iverksettingsdager.add(IverksettingDagKopi(dato, beløp))
    }

    private fun forrigeIverksettingDto(): ForrigeIverksettingDto? {
        val forrigeBehandlingId = forrigeBehandlingId()
        return if (forrigeBehandlingId != null) {
            ForrigeIverksettingDto(behandlingId = forrigeBehandlingId)
        } else {
            null
        }
    }
    private fun finnUtbetalingsdager(): MutableList<UtbetalingDto> {
        val utbetalingsdagerMap = mutableMapOf<LocalDate, Double>()
        for (i in 0 until iverksettingsdager.size) {
            utbetalingsdagerMap.put(
                iverksettingsdager[i].dato,
                iverksettingsdager[i].beløp.verdi,
            )
        }
        val utbetalingsdager = mutableListOf<UtbetalingDto>()
        utbetalingsdagerMap.forEach { entry ->
            utbetalingsdager.add(
                UtbetalingDto(
                    belopPerDag = entry.value.toInt(),
                    fraOgMedDato = entry.key,
                    tilOgMedDato = entry.key,
                ),
            )
        }
        // utbetalingsdager.forEach { utbetalingsdag -> println("dato=${utbetalingsdag.fraOgMedDato} beløp=${utbetalingsdag.belopPerDag}") }
        return utbetalingsdager
    }

    private fun forrigeIverksetting() =
        if (iverksettinger.size > 1) {
            iverksettinger.sortedDescending()[1]
        } else {
            null
        }

    data class IverksettingDagKopi(val dato: LocalDate, val beløp: Beløp)
}
