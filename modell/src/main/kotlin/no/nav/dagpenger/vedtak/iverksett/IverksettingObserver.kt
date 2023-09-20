package no.nav.dagpenger.vedtak.iverksett

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface IverksettingObserver {
    fun iverksettingTilstandEndret(event: IverksettingEndretTilstandEvent) {}

    enum class Utfall {
        Innvilget,
        Avslått,
    }

    data class UtbetalingsvedtakIverksatt(
        val vedtakId: UUID,
        val sakId: SakId,
        val behandlingId: UUID,
        val vedtakstidspunkt: LocalDateTime,
        val virkningsdato: LocalDate,
        val iverksettingsdager: List<IverksettingsdagDto> = emptyList(),
        val utfall: Utfall,
        // @todo: Type rettighet? Ordinær, Permittering etc
    )

    data class IverksettingEndretTilstandEvent(
        val iversettingId: UUID,
        val vedtakId: UUID,
        val gjeldendeTilstand: Iverksetting.Tilstand.TilstandNavn,
        val forrigeTilstand: Iverksetting.Tilstand.TilstandNavn,
    )

    data class IverksettingsdagDto(
        val dato: LocalDate,
        val beløp: Double,
    ) // TODO: Avventer avrundsregler: https://favro.com/organization/98c34fb974ce445eac854de0/696529a0ddfa866861cfa6b6?card=NAV-13898
}
