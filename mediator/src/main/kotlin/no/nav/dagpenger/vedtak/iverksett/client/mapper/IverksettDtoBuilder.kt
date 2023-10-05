package no.nav.dagpenger.vedtak.iverksett.client.mapper

import no.nav.dagpenger.kontrakter.iverksett.ForrigeIverksettingDto
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.vedtak.iverksett.Iverksetting
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator
import no.nav.dagpenger.vedtak.iverksett.Sak
import no.nav.dagpenger.vedtak.iverksett.SakId
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class IverksettDtoBuilder(sak: Sak) : SakVisitor {
    private lateinit var virkningsdato: LocalDate
    private lateinit var vedtakstidspunkt: LocalDateTime
    private lateinit var utfall: UtbetalingsvedtakFattetHendelse.Utfall
    private lateinit var vedtakId: UUID
    private lateinit var behandlingId: UUID
    private lateinit var ident: PersonIdentifikator
    private lateinit var sakId: SakId
    private val iverksettinger = mutableListOf<Iverksetting>()
    private val iverksettingsdager = mutableListOf<IverksettingDagKopi>()

    init {
        sak.accept(this)
    }

    fun bygg() =
        IverksettDto(
            saksreferanse = sakId.sakId,
            behandlingId = behandlingId,
            personIdent = ident.identifikator(),
            forrigeIverksetting = forrigeIverksettingDto(),
            vedtak =
                VedtaksdetaljerDto(
                    vedtakstype = VedtakType.UTBETALINGSVEDTAK,
                    vedtakstidspunkt = vedtakstidspunkt,
                    resultat =
                        when (utfall) {
                            UtbetalingsvedtakFattetHendelse.Utfall.Innvilget -> Vedtaksresultat.INNVILGET
                            UtbetalingsvedtakFattetHendelse.Utfall.Avslått -> Vedtaksresultat.AVSLÅTT
                        },
                    utbetalinger = finnUtbetalingsdager(),
                    saksbehandlerId = "DIGIDAG",
                    beslutterId = "DIGIDAG",
                ),
        )

    override fun visitSak(
        ident: PersonIdentifikator,
        sakId: SakId,
    ) {
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
        this.iverksettinger.add(
            Iverksetting(
                vedtakId,
                behandlingId,
                vedtakstidspunkt,
                virkningsdato,
                utfall,
                mutableListOf(),
            ),
        )
    }

    override fun visitIverksettingDag(
        dato: LocalDate,
        beløp: Beløp,
    ) {
        iverksettingsdager.add(IverksettingDagKopi(dato, beløp))
    }

    private fun forrigeBehandlingId(): UUID? {
        val forrigeIverksetting = forrigeIverksetting()
        return when {
            forrigeIverksetting != null -> BehandlingIdVisitor(forrigeIverksetting).behandlingId
            else -> null
        }
    }

    private fun forrigeIverksetting() =
        when {
            iverksettinger.size > 1 -> iverksettinger.sortedDescending()[1]
            else -> null
        }

    private fun forrigeIverksettingDto(): ForrigeIverksettingDto? {
        val forrigeBehandlingId = forrigeBehandlingId()
        return when {
            forrigeBehandlingId != null -> ForrigeIverksettingDto(behandlingId = forrigeBehandlingId)
            else -> null
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
        return utbetalingsdager
    }

    data class IverksettingDagKopi(val dato: LocalDate, val beløp: Beløp)
}
