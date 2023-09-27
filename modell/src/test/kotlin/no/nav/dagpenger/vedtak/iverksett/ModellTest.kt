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
        val førsteBehandlingId = UUID.randomUUID()
        val førsteVirkningsdato: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
        val førsteUtbetalingsdager = utbetalingsdager(førsteVirkningsdato, 500.0)

        sak.håndter(utbetalingsvedtakFattetHendelse(førsteVedtakId, førsteBehandlingId, førsteVirkningsdato, førsteUtbetalingsdager, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe førsteVedtakId
            modellInspektør.virkningsdato shouldBe førsteVirkningsdato
            modellInspektør.iverksettingsdager.size shouldBe 14
            for (i in 0 until førsteUtbetalingsdager.size) {
                modellInspektør.iverksettingsdager[i].dato shouldBe førsteUtbetalingsdager[i].dato
                modellInspektør.iverksettingsdager[i].beløp.verdi shouldBe førsteUtbetalingsdager[i].beløp
            }
        }

        val andreVedtakId = UUID.randomUUID()
        val andreBehandlingId = UUID.randomUUID()
        val andreVirkningsdato: LocalDate = førsteVirkningsdato.plusDays(14)
        val andreUtbetalingsdager = utbetalingsdager(andreVirkningsdato, 800.0)

        sak.håndter(utbetalingsvedtakFattetHendelse(andreVedtakId, andreBehandlingId, andreVirkningsdato, andreUtbetalingsdager, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe andreVedtakId
            modellInspektør.virkningsdato shouldBe andreVirkningsdato
            modellInspektør.iverksettingsdager.size shouldBe 28
            for (i in 0 until andreUtbetalingsdager.size) {
                modellInspektør.iverksettingsdager[i + førsteUtbetalingsdager.size].dato shouldBe andreUtbetalingsdager[i].dato
                modellInspektør.iverksettingsdager[i + førsteUtbetalingsdager.size].beløp.verdi shouldBe andreUtbetalingsdager[i].beløp
            }
        }
    }

    @Test
    fun `To utbetalingsvedtak for samme periode fører til oppdatert modell med to iverksettinger`() {
        val førsteVedtakId = UUID.randomUUID()
        val førsteBehandlingId = UUID.randomUUID()
        val førsteVirkningsdato: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
        val førsteUtbetalingsdager = utbetalingsdager(førsteVirkningsdato, 500.0)

        sak.håndter(utbetalingsvedtakFattetHendelse(førsteVedtakId, førsteBehandlingId, førsteVirkningsdato, førsteUtbetalingsdager, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe førsteVedtakId
            modellInspektør.virkningsdato shouldBe førsteVirkningsdato
            modellInspektør.iverksettingsdager.size shouldBe førsteUtbetalingsdager.size

            for (i in 0 until førsteUtbetalingsdager.size) {
                modellInspektør.iverksettingsdager[i].dato shouldBe førsteUtbetalingsdager[i].dato
                modellInspektør.iverksettingsdager[i].beløp.verdi shouldBe førsteUtbetalingsdager[i].beløp
            }
        }

        val andreVedtakId = UUID.randomUUID()
        val andreBehandlingId = UUID.randomUUID()
        val andreVirkningsdato = førsteVirkningsdato
        val andreUtbetalingsdager = utbetalingsdager(andreVirkningsdato, 400.0)

        sak.håndter(utbetalingsvedtakFattetHendelse(andreVedtakId, andreBehandlingId, andreVirkningsdato, andreUtbetalingsdager, Aktivitetslogg()))

        assertSoftly {
            modellInspektør.sakId shouldBe sakId
            modellInspektør.ident shouldBe ident
            modellInspektør.vedtakId shouldBe andreVedtakId
            modellInspektør.virkningsdato shouldBe andreVirkningsdato
            modellInspektør.iverksettingsdager.size shouldBe førsteUtbetalingsdager.size + andreUtbetalingsdager.size

            for (i in 0 until andreUtbetalingsdager.size) {
                modellInspektør.iverksettingsdager[i + førsteUtbetalingsdager.size].dato shouldBe andreUtbetalingsdager[i].dato
                modellInspektør.iverksettingsdager[i + førsteUtbetalingsdager.size].beløp.verdi shouldBe andreUtbetalingsdager[i].beløp
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
