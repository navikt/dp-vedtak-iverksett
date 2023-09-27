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
    private val sakId = SakId("SAKSNUMMER_1")
    private val ukedagIdag = LocalDate.now().dayOfWeek

    private val modellInspektør get() = ModellInspektør(sak)
    private val sak = Sak(ident = ident, sakId = sakId, iverksettinger = mutableListOf())

    @Test
    fun `To utbetalingsvedtak for ulike perioder fører til oppdatert modell med to iverksettinger`() {
        val førsteVedtakId = UUID.randomUUID()
        val andreVedtakId = UUID.randomUUID()
        val behandlingId1 = UUID.randomUUID()
        val behandlingId2 = UUID.randomUUID()
        val virkningsdato1: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
        val virkningsdato2: LocalDate = virkningsdato1.plusDays(14)
        val utbetalingsdager1 = utbetalingsdager(virkningsdato1, 500.0)
        val utbetalingsdager2 = utbetalingsdager(virkningsdato2, 800.0)

        sak.håndter(utbetalingsvedtakFattetHendelse(førsteVedtakId, behandlingId1, virkningsdato1, utbetalingsdager1, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe førsteVedtakId
            modellInspektør.virkningsdato shouldBe virkningsdato1
            modellInspektør.iverksettingsdager.size shouldBe 14
            for (i in 0..utbetalingsdager1.size - 1) {
                modellInspektør.iverksettingsdager[i].dato shouldBe utbetalingsdager1[i].dato
                modellInspektør.iverksettingsdager[i].beløp.verdi shouldBe utbetalingsdager1[i].beløp
            }
        }
        sak.håndter(utbetalingsvedtakFattetHendelse(andreVedtakId, behandlingId2, virkningsdato2, utbetalingsdager2, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe andreVedtakId
            modellInspektør.virkningsdato shouldBe virkningsdato2
            modellInspektør.iverksettingsdager.size shouldBe 28
            for (i in 0..utbetalingsdager2.size - 1) {
                modellInspektør.iverksettingsdager[i + 14].dato shouldBe utbetalingsdager2[i].dato
                modellInspektør.iverksettingsdager[i + 14].beløp.verdi shouldBe utbetalingsdager2[i].beløp
            }
        }
    }

    @Test
    fun `To utbetalingsvedtak for samme periode fører til oppdatert modell med to iverksettinger`() {
        val vedtakId1 = UUID.randomUUID()
        val vedtakId2 = UUID.randomUUID()
        val behandlingId1 = UUID.randomUUID()
        val behandlingId2 = UUID.randomUUID()
        val virkningsdato1: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
        val virkningsdato2 = virkningsdato1
        val utbetalingsdager1 = utbetalingsdager(virkningsdato1, 500.0)
        val utbetalingsdager2 = utbetalingsdager(virkningsdato2, 400.0)
        sak.håndter(utbetalingsvedtakFattetHendelse(vedtakId1, behandlingId1, virkningsdato1, utbetalingsdager1, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe vedtakId1
            modellInspektør.virkningsdato shouldBe virkningsdato1
            modellInspektør.iverksettingsdager.size shouldBe 14
            for (i in 0..utbetalingsdager1.size - 1) {
                modellInspektør.iverksettingsdager[i].dato shouldBe utbetalingsdager1[i].dato
                modellInspektør.iverksettingsdager[i].beløp.verdi shouldBe utbetalingsdager1[i].beløp
            }
        }

        sak.håndter(utbetalingsvedtakFattetHendelse(vedtakId2, behandlingId2, virkningsdato2, utbetalingsdager2, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe vedtakId2
            modellInspektør.virkningsdato shouldBe virkningsdato2
            modellInspektør.iverksettingsdager.size shouldBe 28
            for (i in 0..utbetalingsdager2.size - 1) {
                modellInspektør.iverksettingsdager[i + 14].dato shouldBe utbetalingsdager2[i].dato
                modellInspektør.iverksettingsdager[i + 14].beløp.verdi shouldBe utbetalingsdager2[i].beløp
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
