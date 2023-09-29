package no.nav.dagpenger.vedtak.iverksett

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse.Utbetalingsdag
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.absoluteValue

class SakInspektørTest {
    private val ident = "12345678911".tilPersonIdentfikator()
    private val sakId = SakId("SAKSNUMMER_1")
    private val ukedagIdag = LocalDate.now().dayOfWeek
    private val sak = Sak(ident = ident, sakId = sakId, iverksettinger = mutableListOf())
    private val sakInspektør get() = SakInspektør(sak)

    @Test
    fun `ForrigeBehandlingId er null for første vedtak, ved neste vedtak er forrigeBehandlingId lik første vedtaks behandlingId`() {
        val førsteVedtaksBehandlingId = UUID.randomUUID()
        val førsteVedtaksVirkningsdato: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
        val førsteVedtaksUtbetalingsdager = utbetalingsdager(førsteVedtaksVirkningsdato, 500.0)

        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                vedtakId = UUID.randomUUID(),
                behandlingId = førsteVedtaksBehandlingId,
                virkningsdato = førsteVedtaksVirkningsdato,
                utbetalingsdager = førsteVedtaksUtbetalingsdager,
            ),
        )
        sakInspektør.forrigeBehandlingId() shouldBe null

        val andreVedtaksVirkningsdato: LocalDate =
            førsteVedtaksVirkningsdato.plusDays(førsteVedtaksUtbetalingsdager.size.toLong())

        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                vedtakId = UUID.randomUUID(),
                behandlingId = UUID.randomUUID(),
                virkningsdato = andreVedtaksVirkningsdato,
                utbetalingsdager = utbetalingsdager(andreVedtaksVirkningsdato, 633.0),
            ),
        )
        sakInspektør.forrigeBehandlingId() shouldBe førsteVedtaksBehandlingId
    }

    private fun utbetalingsvedtakFattetHendelse(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        utbetalingsdager: List<Utbetalingsdag>,
    ) =
        UtbetalingsvedtakFattetHendelse(
            meldingsreferanseId = UUID.randomUUID(),
            ident = ident.identifikator(),
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            sakId = sakId.sakId,
            vedtakstidspunkt = LocalDateTime.now(),
            virkningsdato = virkningsdato,
            utfall = UtbetalingsvedtakFattetHendelse.Utfall.Innvilget,
            utbetalingsdager = utbetalingsdager,
        )

    private fun utbetalingsdager(virkningsdato: LocalDate, dagsbeløp: Double): MutableList<Utbetalingsdag> {
        val utbetalingsdager = mutableListOf<Utbetalingsdag>()

        for (i in -13..0) {
            utbetalingsdager.add(
                Utbetalingsdag(
                    dato = virkningsdato.minusDays(i.absoluteValue.toLong()),
                    beløp = dagsbeløp,
                ),
            )
        }
        return utbetalingsdager
    }
}
