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

class SakTest {

    private val ident = "12345678911".tilPersonIdentfikator()
    private val vedtakId = UUID.randomUUID()
    private val behandlingId = UUID.randomUUID()
    private val sakId = SakId("SAKSNUMMER_1")
    private val vedtakstidspunkt = LocalDateTime.now()
    private val virkningsdato = LocalDate.now()
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
            virkningsdato = virkningsdato,
            utbetalingsdager = utbetalingsdager(),
            utfall = UtbetalingsvedtakFattetHendelse.Utfall.Innvilget,
            aktivitetslogg = aktivitetslogg,
        )

    private fun utbetalingsdager() = listOf(
        Utbetalingsdag(dato = virkningsdato, beløp = 10.0),
    )
}
