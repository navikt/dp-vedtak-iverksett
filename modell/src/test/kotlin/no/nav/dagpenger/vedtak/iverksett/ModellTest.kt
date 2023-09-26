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
    private val vedtakId1 = UUID.randomUUID()
    private val vedtakId2 = UUID.randomUUID()
    private val behandlingId1 = UUID.randomUUID()
    private val behandlingId2 = UUID.randomUUID()
    private val sakId = SakId("SAKSNUMMER_1")
    private val ukedagIdag = LocalDate.now().dayOfWeek
    private val virkningsdato1: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
    private val virkningsdato2: LocalDate = virkningsdato1.plusDays(14)
    private val modellInspektør get() = ModellInspektør(sak)
    private val sak = Sak(ident = ident, sakId = sakId, iverksettinger = mutableListOf())

    @Test
    fun `Utbetalingsvedtak fører til oppdatert iverksettingsmodell`() {
        sak.håndter(utbetalingsvedtakFattetHendelse(vedtakId1, behandlingId1, virkningsdato1, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe vedtakId1
            modellInspektør.virkningsdato shouldBe virkningsdato1
            modellInspektør.iverksettingsdager.size shouldBe 14
            utbetalingsdager(virkningsdato1).forEach { utbetalingsdag ->
                modellInspektør.iverksettingsdager[utbetalingsdag.dato]!!.verdi shouldBe utbetalingsdag.beløp
            }
        }
        sak.håndter(utbetalingsvedtakFattetHendelse(vedtakId2, behandlingId2, virkningsdato2, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe vedtakId2
            modellInspektør.virkningsdato shouldBe virkningsdato2
            modellInspektør.iverksettingsdager.size shouldBe 28
            utbetalingsdager(virkningsdato2).forEach { utbetalingsdag ->
                modellInspektør.iverksettingsdager[utbetalingsdag.dato]!!.verdi shouldBe utbetalingsdag.beløp
            }
        }
    }

    private fun utbetalingsvedtakFattetHendelse(vedtakId: UUID, behandlingId: UUID, virkningsdato: LocalDate, aktivitetslogg: Aktivitetslogg) =
        UtbetalingsvedtakFattetHendelse(
            meldingsreferanseId = UUID.randomUUID(),
            ident = ident.identifikator(),
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            sakId = sakId.sakId,
            vedtakstidspunkt = LocalDateTime.now(),
            virkningsdato = virkningsdato,
            utbetalingsdager = utbetalingsdager(virkningsdato),
            utfall = UtbetalingsvedtakFattetHendelse.Utfall.Innvilget,
            aktivitetslogg = aktivitetslogg,
        )

    private fun utbetalingsdager(virkningsdato: LocalDate) = listOf(
        Utbetalingsdag(dato = virkningsdato.minusDays(13), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(12), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(11), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(10), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(9), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(8), beløp = 0.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(7), beløp = 0.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(6), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(5), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(4), beløp = 500.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(3), beløp = 250.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(2), beløp = 300.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(1), beløp = 0.0),
        Utbetalingsdag(dato = virkningsdato, beløp = 0.0),
    )
}
