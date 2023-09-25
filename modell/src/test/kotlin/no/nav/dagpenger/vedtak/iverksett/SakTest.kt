package no.nav.dagpenger.vedtak.iverksett

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse.Utbetalingsdag
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SakTest {

    private val ident = "12345678911".tilPersonIdentfikator()
    private val vedtakId = UUID.randomUUID()
    private val behandlingId = UUID.randomUUID()
    private val sakId = SakId("SAKSNUMMER_1")
    private val vedtakstidspunkt = LocalDateTime.now()
    private val ukedagIdag = LocalDate.now().dayOfWeek
    private val virkningsdatoErSøndag: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
    private val sakInspektør get() = SakTestInspektør(sak)
    private val sak = Sak(ident = ident, sakId = sakId, iverksettingHistorikk = IverksettingHistorikk())

    @Test
    fun `Utbetalingsvedtak fører til oppdatert iverksettingshistorikk`() {
        val aktivitetslogg = Aktivitetslogg()

        sak.håndter(utbetalingsvedtakFattetHendelse(aktivitetslogg))

        assertSoftly {
            sakInspektør.sakId shouldBe sakId
            sakInspektør.ident shouldBe ident
            sakInspektør.iverksettinger.size shouldBe 1
            sakInspektør.iverksettinger.forEach { iverksetting ->
                iverksetting.iverksettingsdager.size shouldBe 14
            }
            sakInspektør.iverksettinger.first().iverksettingsdager.forEach { iverksettingDag ->
                when (iverksettingDag.dato.dayOfWeek) {

                    DayOfWeek.MONDAY -> sakInspektør.iverksettingHistorikk.beløpTilUtbetalingForDag(iverksettingDag.dato) shouldBe 500.beløp
                    DayOfWeek.TUESDAY -> sakInspektør.iverksettingHistorikk.beløpTilUtbetalingForDag(iverksettingDag.dato) shouldBe 500.beløp
                    DayOfWeek.WEDNESDAY -> sakInspektør.iverksettingHistorikk.beløpTilUtbetalingForDag(iverksettingDag.dato) shouldBe 500.beløp
                    DayOfWeek.THURSDAY -> sakInspektør.iverksettingHistorikk.beløpTilUtbetalingForDag(iverksettingDag.dato) shouldBe 500.beløp
                    DayOfWeek.FRIDAY -> sakInspektør.iverksettingHistorikk.beløpTilUtbetalingForDag(iverksettingDag.dato) shouldBe 500.beløp
                    DayOfWeek.SATURDAY -> sakInspektør.iverksettingHistorikk.beløpTilUtbetalingForDag(iverksettingDag.dato) shouldBe 0.beløp
                    DayOfWeek.SUNDAY -> sakInspektør.iverksettingHistorikk.beløpTilUtbetalingForDag(iverksettingDag.dato) shouldBe 0.beløp
                }
                shouldThrow<IllegalArgumentException> {
                    sakInspektør.iverksettingHistorikk.beløpTilUtbetalingForDag(virkningsdatoErSøndag.plusDays(1))
                }
            }
        }
    }

    private fun utbetalingsvedtakFattetHendelse(aktivitetslogg: Aktivitetslogg) =
        UtbetalingsvedtakFattetHendelse(
            meldingsreferanseId = UUID.randomUUID(),
            ident = ident.identifikator(),
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            sakId = sakId.sakId,
            vedtakstidspunkt = vedtakstidspunkt,
            virkningsdato = virkningsdatoErSøndag,
            utbetalingsdager = utbetalingsdager(),
            utfall = UtbetalingsvedtakFattetHendelse.Utfall.Innvilget,
            aktivitetslogg = aktivitetslogg,
        )

    private fun utbetalingsdager() = listOf(
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(13), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(12), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(11), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(10), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(9), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(8), beløp = 0.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(7), beløp = 0.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(6), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(5), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(4), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(3), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(2), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(1), beløp = 0.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag, beløp = 0.0),
    )
}
