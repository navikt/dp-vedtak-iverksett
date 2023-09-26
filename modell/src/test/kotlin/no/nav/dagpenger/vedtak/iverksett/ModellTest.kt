package no.nav.dagpenger.vedtak.iverksett

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse.Utbetalingsdag
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ModellTest {

    private val ident = "12345678911".tilPersonIdentfikator()
    private val vedtakId = UUID.randomUUID()
    private val behandlingId = UUID.randomUUID()
    private val sakId = SakId("SAKSNUMMER_1")
    private val vedtakstidspunkt = LocalDateTime.now()
    private val ukedagIdag = LocalDate.now().dayOfWeek
    private val virkningsdatoErSøndag: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
    private val modellInspektør get() = ModellInspektør(sak)
    private val sak = Sak(ident = ident, sakId = sakId, iverksettinger = mutableListOf())

    @Test
    fun `Utbetalingsvedtak fører til oppdatert iverksettingsmodell`() {
        sak.håndter(utbetalingsvedtakFattetHendelse(Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe vedtakId
            modellInspektør.virkningsdato shouldBe virkningsdatoErSøndag
            modellInspektør.iverksettingsdager.size shouldBe 14
            utbetalingsdager().forEach { utbetalingsdag ->
                modellInspektør.iverksettingsdager[utbetalingsdag.dato]!!.verdi shouldBe utbetalingsdag.beløp
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
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(3), beløp = 250.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(2), beløp = 300.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag.minusDays(1), beløp = 0.0),
        Utbetalingsdag(dato = virkningsdatoErSøndag, beløp = 0.0),
    )
}
