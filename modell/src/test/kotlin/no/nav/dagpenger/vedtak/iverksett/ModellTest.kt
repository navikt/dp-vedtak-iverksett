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
    private val vedtakId3 = UUID.randomUUID()
    private val behandlingId1 = UUID.randomUUID()
    private val behandlingId2 = UUID.randomUUID()
    private val behandlingId3 = UUID.randomUUID()
    private val sakId = SakId("SAKSNUMMER_1")
    private val ukedagIdag = LocalDate.now().dayOfWeek
    private val virkningsdato1: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
    private val virkningsdato2: LocalDate = virkningsdato1.plusDays(14)
    private val virkningsdato3 = virkningsdato1
    private val utbetalingsdager1 = utbetalingsdager(virkningsdato1, 500.0)
    private val utbetalingsdager2 = utbetalingsdager(virkningsdato2, 800.0)
    private val utbetalingsdager3 = utbetalingsdager(virkningsdato3, 400.0)
    private val modellInspektør get() = ModellInspektør(sak)
    private val sak = Sak(ident = ident, sakId = sakId, iverksettinger = mutableListOf())

    @Test
    fun `Flere utbetalingsvedtak fører til oppdatert iverksettingsmodell for hver iverksetting`() {
        sak.håndter(utbetalingsvedtakFattetHendelse(vedtakId1, behandlingId1, virkningsdato1, utbetalingsdager1, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe vedtakId1
            modellInspektør.virkningsdato shouldBe virkningsdato1
            modellInspektør.iverksettingsdager.size shouldBe 14
            utbetalingsdager1.forEach { utbetalingsdag ->
                modellInspektør.iverksettingsdager[utbetalingsdag.dato]!!.verdi shouldBe utbetalingsdag.beløp
            }
        }
        sak.håndter(utbetalingsvedtakFattetHendelse(vedtakId2, behandlingId2, virkningsdato2, utbetalingsdager2, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe vedtakId2
            modellInspektør.virkningsdato shouldBe virkningsdato2
            modellInspektør.iverksettingsdager.size shouldBe 28
            utbetalingsdager2.forEach { utbetalingsdag ->
                modellInspektør.iverksettingsdager[utbetalingsdag.dato]!!.verdi shouldBe utbetalingsdag.beløp
            }
        }

        sak.håndter(utbetalingsvedtakFattetHendelse(vedtakId3, behandlingId3, virkningsdato3, utbetalingsdager3, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe vedtakId3
            modellInspektør.virkningsdato shouldBe virkningsdato3
            modellInspektør.iverksettingsdager.size shouldBe 28
            utbetalingsdager3.forEach { utbetalingsdag ->
                modellInspektør.iverksettingsdager[utbetalingsdag.dato]!!.verdi shouldBe utbetalingsdag.beløp
            }
        }
    }

    private fun utbetalingsvedtakFattetHendelse(vedtakId: UUID, behandlingId: UUID, virkningsdato: LocalDate, utbetalingsdager: List<Utbetalingsdag>, aktivitetslogg: Aktivitetslogg) =
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
            aktivitetslogg = aktivitetslogg,
        )

    private fun utbetalingsdager(virkningsdato: LocalDate, dagsbeløp: Double) = listOf(
        Utbetalingsdag(dato = virkningsdato.minusDays(13), beløp = dagsbeløp),
        Utbetalingsdag(dato = virkningsdato.minusDays(12), beløp = dagsbeløp),
        Utbetalingsdag(dato = virkningsdato.minusDays(11), beløp = dagsbeløp),
        Utbetalingsdag(dato = virkningsdato.minusDays(10), beløp = dagsbeløp),
        Utbetalingsdag(dato = virkningsdato.minusDays(9), beløp = dagsbeløp),
        Utbetalingsdag(dato = virkningsdato.minusDays(8), beløp = 0.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(7), beløp = 0.0),
        Utbetalingsdag(dato = virkningsdato.minusDays(6), beløp = dagsbeløp),
        Utbetalingsdag(dato = virkningsdato.minusDays(5), beløp = dagsbeløp),
        Utbetalingsdag(dato = virkningsdato.minusDays(4), beløp = dagsbeløp),
        Utbetalingsdag(dato = virkningsdato.minusDays(3), beløp = dagsbeløp),
        Utbetalingsdag(dato = virkningsdato.minusDays(2), beløp = dagsbeløp),
        Utbetalingsdag(dato = virkningsdato.minusDays(1), beløp = 0.0),
        Utbetalingsdag(dato = virkningsdato, beløp = 0.0),
    )
}
