package no.nav.dagpenger.vedtak.iverksett

import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import no.nav.dagpenger.vedtak.iverksett.melding.HendelseMediator
import no.nav.dagpenger.vedtak.iverksett.persistens.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.iverksett.persistens.InMemorySakRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SakMediatorTest {

    private val testRapid = TestRapid()
    private val førsteVirkningsdato = LocalDate.now().minusDays(14)
    private val andreVirkningsdato = LocalDate.now()
    private val ident = "12345123451"
    private val sakId = "SAK_NUMMER_1"
    private val sakRepository = InMemorySakRepository()

    init {
        HendelseMediator(
            rapidsConnection = testRapid,
            hendelseRepository = InMemoryMeldingRepository(),
            sakMediator = SakMediator(sakRepository, mockk()),
        )
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `Utbetalingsvedtak fattet hendelse fører til iverksetting`() {
        testRapid.sendTestMessage(utbetalingsvedtakFattet(ident = ident, virkningsdato = førsteVirkningsdato, dagsbeløp = 700.0, sakId = SakId(sakId)))
        sakRepository.hent(SakId(sakId)) shouldNotBe null

        testRapid.sendTestMessage(utbetalingsvedtakFattet(ident = ident, virkningsdato = andreVirkningsdato, dagsbeløp = 800.0, sakId = SakId(sakId)))
        sakRepository.hent(SakId(sakId)) shouldNotBe null

        testRapid.sendTestMessage(utbetalingsvedtakFattet(ident = ident, virkningsdato = førsteVirkningsdato, dagsbeløp = 1490.0, sakId = SakId(sakId)))
        sakRepository.hent(SakId(sakId)) shouldNotBe null
    }
}
