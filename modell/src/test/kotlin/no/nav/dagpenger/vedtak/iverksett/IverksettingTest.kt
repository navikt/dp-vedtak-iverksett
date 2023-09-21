package no.nav.dagpenger.vedtak.iverksett

import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class IverksettingTest {

    private val ident = "12345678911"
    private val vedtakId = UUID.randomUUID()
    private val behandlingId = UUID.randomUUID()
    private val sakId = "SAKSNUMMER_1"
    private val vedtakstidspunkt = LocalDateTime.now()
    private val virkningsdato = LocalDate.now()

    private lateinit var iverksetting: Iverksetting
    private val inspektør get() = IverksettingInspektør(iverksetting)

    @BeforeEach
    fun setup() {
        iverksetting = Iverksetting(vedtakId, ident, mutableListOf())
    }

    @Test
    fun `Skal starte iverksetting når utbetalingsvedtak fattes`() {
        val aktivitetslogg = Aktivitetslogg()

        iverksetting.håndter(
            UtbetalingsvedtakFattetHendelse(
                meldingsreferanseId = UUID.randomUUID(),
                ident = ident,
                vedtakId = vedtakId,
                behandlingId = behandlingId,
                sakId = sakId,
                vedtakstidspunkt = vedtakstidspunkt,
                virkningsdato = virkningsdato,
                utbetalingsdager = utbetalingsdager(),
                utfall = UtbetalingsvedtakFattetHendelse.Utfall.Innvilget,
                aktivitetslogg = aktivitetslogg,
            ),
        )
    }

    private fun utbetalingsdager() = listOf(
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato, beløp = 10.0),
    )
}
