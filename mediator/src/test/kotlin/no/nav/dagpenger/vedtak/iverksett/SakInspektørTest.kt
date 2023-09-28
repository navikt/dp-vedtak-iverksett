package no.nav.dagpenger.vedtak.iverksett

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SakInspektørTest {
    private val ident = "12345678911".tilPersonIdentfikator()
    private val sakId = SakId("SAKSNUMMER_1")
    private val ukedagIdag = LocalDate.now().dayOfWeek
    private val sak = Sak(ident = ident, sakId = sakId, iverksettinger = mutableListOf())
    private val sakInspektør get() = SakInspektør(sak)

    @Test
    fun `ForrigeBehandlingId er null for første vedtak, ved neste vedtak er forrigeBehandlingId lik første vedtaks behandlingId`() {
        val førsteBehandlingId = UUID.randomUUID()
        val førsteVirkningsdato: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
        val førsteUtbetalingsdager = utbetalingsdager(førsteVirkningsdato, 500.0)

        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                vedtakId = UUID.randomUUID(),
                behandlingId = førsteBehandlingId,
                virkningsdato = førsteVirkningsdato,
                utbetalingsdager = førsteUtbetalingsdager,
            ),
        )
        sakInspektør.forrigeBehandlingId() shouldBe null

        val andreVirkningsdato: LocalDate = førsteVirkningsdato.plusDays(førsteUtbetalingsdager.size.toLong())

        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                vedtakId = UUID.randomUUID(),
                behandlingId = UUID.randomUUID(),
                virkningsdato = andreVirkningsdato,
                utbetalingsdager = utbetalingsdager(andreVirkningsdato, 633.0),
            ),
        )
        sakInspektør.forrigeBehandlingId() shouldBe førsteBehandlingId
    }

    private fun utbetalingsvedtakFattetHendelse(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        utbetalingsdager: List<UtbetalingsvedtakFattetHendelse.Utbetalingsdag>,
    ) =
        UtbetalingsvedtakFattetHendelse(
            meldingsreferanseId = UUID.randomUUID(),
            ident = ident.identifikator(),
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            sakId = sakId.sakId,
            vedtakstidspunkt = LocalDateTime.now(),
            virkningsdato = virkningsdato,
            utbetalingsdager = utbetalingsdager,
            utfall = UtbetalingsvedtakFattetHendelse.Utfall.Innvilget,
        )

    private fun utbetalingsdager(virkningsdato: LocalDate, dagsbeløp: Double) = listOf(
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(13), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(12), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(11), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(10), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(9), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(8), beløp = 0.0),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(7), beløp = 0.0),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(6), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(5), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(4), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(3), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(2), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(1), beløp = 0.0),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato, beløp = 0.0),
    )
}
