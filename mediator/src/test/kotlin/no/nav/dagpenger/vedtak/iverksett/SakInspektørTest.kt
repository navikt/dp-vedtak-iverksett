package no.nav.dagpenger.vedtak.iverksett

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.kontrakter.iverksett.UtbetalingDto
import no.nav.dagpenger.kontrakter.iverksett.VedtakType
import no.nav.dagpenger.kontrakter.iverksett.VedtaksdetaljerDto
import no.nav.dagpenger.kontrakter.iverksett.VedtaksperiodeDto
import no.nav.dagpenger.kontrakter.iverksett.Vedtaksresultat
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SakInspektørTest {
    private val ident = "12345678911".tilPersonIdentfikator()
    private val sakId = SakId("SAKSNUMMER_1")

    private val ukedagIdag = LocalDate.now().dayOfWeek

    val sak = Sak(ident = ident, sakId = sakId, iverksettinger = mutableListOf())
    val sakInspektør get() = SakInspektør(sak)

    @Test
    fun `Utbetalingsvedtak for to etterfølgende rapporteringer fører til to iverksettinger der dager aggregeres`() {
        val førsteVedtakId = UUID.randomUUID()
        val førsteBehandlingId = UUID.randomUUID()
        val førsteVirkningsdato: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
        val førsteUtbetalingsdager = utbetalingsdager(førsteVirkningsdato, 500.0)

        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                førsteVedtakId,
                førsteBehandlingId,
                førsteVirkningsdato,
                førsteUtbetalingsdager,
                Aktivitetslogg(),
            ),
        )

        assertSoftly {
            sakInspektør.sakId shouldBe sakId
            sakInspektør.ident shouldBe ident
            sakInspektør.vedtakId shouldBe førsteVedtakId
            sakInspektør.behandlingId shouldBe førsteBehandlingId
            sakInspektør.virkningsdato shouldBe førsteVirkningsdato
            sakInspektør.iverksettingsdager.size shouldBe førsteUtbetalingsdager.size

            for (i in 0 until førsteUtbetalingsdager.size) {
                sakInspektør.iverksettingsdager[i].dato shouldBe førsteUtbetalingsdager[i].dato
                sakInspektør.iverksettingsdager[i].beløp.verdi shouldBe førsteUtbetalingsdager[i].beløp
            }
        }

        val andreVedtakId = UUID.randomUUID()
        val andreBehandlingId = UUID.randomUUID()
        val andreVirkningsdato: LocalDate = førsteVirkningsdato.plusDays(førsteUtbetalingsdager.size.toLong())
        val andreUtbetalingsdager = utbetalingsdager(andreVirkningsdato, 633.0)

        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                andreVedtakId,
                andreBehandlingId,
                andreVirkningsdato,
                andreUtbetalingsdager,
                Aktivitetslogg(),
            ),
        )

        assertSoftly {
            sakInspektør.sakId shouldBe sakId
            sakInspektør.ident shouldBe ident
            sakInspektør.vedtakId shouldBe andreVedtakId
            sakInspektør.behandlingId shouldBe andreBehandlingId
            sakInspektør.virkningsdato shouldBe andreVirkningsdato
            sakInspektør.iverksettingsdager.size shouldBe førsteUtbetalingsdager.size + andreUtbetalingsdager.size

            for (i in 0 until andreUtbetalingsdager.size) {
                sakInspektør.iverksettingsdager[i + førsteUtbetalingsdager.size].dato shouldBe andreUtbetalingsdager[i].dato
                sakInspektør.iverksettingsdager[i + førsteUtbetalingsdager.size].beløp.verdi shouldBe andreUtbetalingsdager[i].beløp
            }
        }

        sakInspektør.forrigeBehandlingId() shouldBe førsteBehandlingId
    }

    @Test
    fun `Utbetalingsvedtak for to rapporteringer for samme periode fører til to iverksettinger med de samme dagene`() {
        val førsteVedtakId = UUID.randomUUID()
        val førsteBehandlingId = UUID.randomUUID()
        val førsteVirkningsdato: LocalDate = LocalDate.now().minusDays(ukedagIdag.value.toLong())
        val førsteUtbetalingsdager = utbetalingsdager(førsteVirkningsdato, 500.0)

        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                førsteVedtakId,
                førsteBehandlingId,
                førsteVirkningsdato,
                førsteUtbetalingsdager,
                Aktivitetslogg(),
            ),
        )

        assertSoftly {
            sakInspektør.sakId shouldBe sakId
            sakInspektør.ident shouldBe ident
            sakInspektør.vedtakId shouldBe førsteVedtakId
            sakInspektør.behandlingId shouldBe førsteBehandlingId
            sakInspektør.virkningsdato shouldBe førsteVirkningsdato
            sakInspektør.iverksettingsdager.size shouldBe førsteUtbetalingsdager.size

            for (i in 0 until førsteUtbetalingsdager.size) {
                sakInspektør.iverksettingsdager[i].dato shouldBe førsteUtbetalingsdager[i].dato
                sakInspektør.iverksettingsdager[i].beløp.verdi shouldBe førsteUtbetalingsdager[i].beløp
            }
        }
        byggIverksettDto(vedtakIdFilter = førsteVedtakId)

        val andreVedtakId = UUID.randomUUID()
        val andreBehandlingId = UUID.randomUUID()
        val andreVirkningsdato = førsteVirkningsdato
        val andreUtbetalingsdager = utbetalingsdager(andreVirkningsdato, 633.0)

        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                andreVedtakId,
                andreBehandlingId,
                andreVirkningsdato,
                andreUtbetalingsdager,
                Aktivitetslogg(),
            ),
        )

        assertSoftly {
            sakInspektør.sakId shouldBe sakId
            sakInspektør.ident shouldBe ident
            sakInspektør.vedtakId shouldBe andreVedtakId
            sakInspektør.behandlingId shouldBe andreBehandlingId
            sakInspektør.virkningsdato shouldBe andreVirkningsdato
            sakInspektør.iverksettingsdager.size shouldBe førsteUtbetalingsdager.size + andreUtbetalingsdager.size

            for (i in 0 until andreUtbetalingsdager.size) {
                sakInspektør.iverksettingsdager[i + førsteUtbetalingsdager.size].dato shouldBe andreUtbetalingsdager[i].dato
                sakInspektør.iverksettingsdager[i + førsteUtbetalingsdager.size].beløp.verdi shouldBe andreUtbetalingsdager[i].beløp
            }
        }
        byggIverksettDto(vedtakIdFilter = andreVedtakId)
    }

    private fun utbetalingsvedtakFattetHendelse(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        utbetalingsdager: List<UtbetalingsvedtakFattetHendelse.Utbetalingsdag>,
        aktivitetslogg: Aktivitetslogg,
    ) =
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
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(13), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(12), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(11), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(10), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(9), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(8), beløp = 0.0),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(7), beløp = 0.0),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(6), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(5), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(4), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(3), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(2), beløp = dagsbeløp),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato.minusDays(1), beløp = 0.0),
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(dato = virkningsdato, beløp = 0.0),
    )

    private fun byggIverksettDto(vedtakIdFilter: UUID) {
        if (sakInspektør.vedtakId == vedtakIdFilter) {
            println("Bygger IverksettDto for iverksetting av vedtakId $vedtakIdFilter - behandlingId ${sakInspektør.behandlingId}")
            val sakIdIverksett: String = sakId.sakId
            val iverksettDto = IverksettDto(
                saksreferanse = sakIdIverksett,
                behandlingId = sakInspektør.behandlingId,
                personIdent = ident.identifikator(),
                vedtak = VedtaksdetaljerDto(
                    vedtakstype = VedtakType.UTBETALINGSVEDTAK,
                    vedtakstidspunkt = LocalDateTime.now(), // TODO: Må hentes ut med visitor
                    resultat = Vedtaksresultat.INNVILGET, // TODO: Må hentes ut med visitor
                    utbetalinger = finnUtbetalingsdager(),
                    saksbehandlerId = "DIGIDAG",
                    beslutterId = "DIGIDAG",
                    vedtaksperioder = listOf(
                        VedtaksperiodeDto(
                            fraOgMedDato = sakInspektør.virkningsdato,
                        ),
                    ),
                ),
            )
            println("IverksettDto: " + iverksettDto)
        } else {
            println("******filter****** $vedtakIdFilter er ulik ${sakInspektør.vedtakId}")
        }
    }

    private fun finnUtbetalingsdager(): List<UtbetalingDto> {
        val utbetalingerMutable = mutableListOf<UtbetalingDto>()
        val alleUtbetalingsdagerMap = mutableMapOf<LocalDate, Double>()

        for (i in 0 until sakInspektør.iverksettingsdager.size) {
            alleUtbetalingsdagerMap.put(sakInspektør.iverksettingsdager[i].dato, sakInspektør.iverksettingsdager[i].beløp.verdi)
        }

        alleUtbetalingsdagerMap.forEach { entry ->
            utbetalingerMutable.add(UtbetalingDto(belopPerDag = entry.value.toInt(), fraOgMedDato = entry.key, tilOgMedDato = entry.key))
        }

        val utbetalinger: List<UtbetalingDto> = utbetalingerMutable

        utbetalinger.forEach { utbetaling -> println("fom=${utbetaling.fraOgMedDato} beløp=${utbetaling.belopPerDag}") }
        return utbetalinger
    }
}
