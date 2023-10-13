package no.nav.dagpenger.vedtak.iverksett

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.iverksett.client.mapper.IverksettDtoBuilder
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse.Utbetalingsdag
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.absoluteValue

class IverksettDtoBuilderTest {
    private val ident = "12345678911".tilPersonIdentfikator()
    private val sakId = SakId("SAKSNUMMER_1")
    private val ukedagIdag = LocalDate.now().dayOfWeek
    private val sak = Sak(ident = ident, sakId = sakId, iverksettinger = mutableListOf())
    private val iverksettDtoBuilder get() = IverksettDtoBuilder(sak)

    @Test
    fun `Tester innholdet av iverksettingsobjektet når man får flere hendelser om fattet utbetalingsvedtak`() {
        // første utbetalingsvedtak
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
        val førsteIverksettDto = iverksettDtoBuilder.bygg()
        førsteIverksettDto shouldNotBe null
        førsteIverksettDto.forrigeIverksetting shouldBe null
        førsteIverksettDto.vedtak.utbetalinger.size shouldBe 10

        // andre utbetalingsvedtak
        val andreVedtaksVirkningsdato: LocalDate =
            førsteVedtaksVirkningsdato.plusDays(førsteVedtaksUtbetalingsdager.size.toLong())
        val andreVedtaksBehandlingId = UUID.randomUUID()
        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                vedtakId = UUID.randomUUID(),
                behandlingId = andreVedtaksBehandlingId,
                virkningsdato = andreVedtaksVirkningsdato,
                utbetalingsdager = utbetalingsdager(andreVedtaksVirkningsdato, 633.0),
            ),
        )
        val andreIverksettDto = iverksettDtoBuilder.bygg()
        andreIverksettDto shouldNotBe null
        andreIverksettDto.forrigeIverksetting!!.behandlingId shouldBe førsteVedtaksBehandlingId
        andreIverksettDto.vedtak.utbetalinger.size shouldBe 20

        // tredje utbetalingsvedtak korrigerer det første
        val tredjeVedtaksUtbetalingsdager = utbetalingsdager(førsteVedtaksVirkningsdato, 380.0)
        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                vedtakId = UUID.randomUUID(),
                behandlingId = andreVedtaksBehandlingId,
                virkningsdato = førsteVedtaksVirkningsdato,
                utbetalingsdager = tredjeVedtaksUtbetalingsdager,
            ),
        )
        val tredjeIverksettDto = iverksettDtoBuilder.bygg()
        tredjeIverksettDto shouldNotBe null
        tredjeIverksettDto.forrigeIverksetting!!.behandlingId shouldBe andreVedtaksBehandlingId
        tredjeIverksettDto.vedtak.utbetalinger.size shouldBe 20
    }

    private fun utbetalingsvedtakFattetHendelse(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        utbetalingsdager: List<Utbetalingsdag>,
    ) = UtbetalingsvedtakFattetHendelse(
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

    private fun utbetalingsdager(
        virkningsdato: LocalDate,
        dagsbeløp: Double,
    ): MutableList<Utbetalingsdag> {
        val utbetalingsdager = mutableListOf<Utbetalingsdag>()

        for (i in -13..0) {
            val dato = virkningsdato.minusDays(i.absoluteValue.toLong())
            val beløp =
                if (dato.dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                    0.0
                } else {
                    dagsbeløp
                }

            utbetalingsdager.add(
                Utbetalingsdag(
                    dato = dato,
                    beløp = beløp,
                ),
            )
        }
        return utbetalingsdager
    }
}
