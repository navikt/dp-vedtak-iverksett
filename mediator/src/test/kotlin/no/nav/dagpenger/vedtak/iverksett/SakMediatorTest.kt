package no.nav.dagpenger.vedtak.iverksett

import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import no.nav.dagpenger.vedtak.iverksett.client.IverksettClient
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
    private val ident = "12345123451"
    private val sakId = "SAK_NUMMER_1"
    private val sakRepository = InMemorySakRepository()
    private val iverksettClientMock = mockk<IverksettClient>()

    init {
        HendelseMediator(
            rapidsConnection = testRapid,
            hendelseRepository = InMemoryMeldingRepository(),
            sakMediator = SakMediator(sakRepository, iverksettClientMock),
        )
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `Hver hendelse om fattet utbetalingsvedtak fører til en iverksetting`() {
        coEvery { iverksettClientMock.iverksett(any()) } just Runs

        testRapid.sendTestMessage(
            utbetalingsvedtakFattet(
                ident = ident,
                virkningsdato = førsteVirkningsdato,
                dagsbeløp = 700.0,
                sakId = SakId(sakId),
            ),
        )

        coVerify(exactly = 1) {
            iverksettClientMock.iverksett(any())
        }

        sakRepository.hent(SakId(sakId)) shouldNotBe null

        testRapid.sendTestMessage(
            utbetalingsvedtakFattet(
                ident = ident,
                virkningsdato = førsteVirkningsdato.plusDays(14),
                dagsbeløp = 800.0,
                sakId = SakId(sakId),
            ),
        )

        coVerify(exactly = 2) {
            iverksettClientMock.iverksett(any())
        }

        sakRepository.hent(SakId(sakId)) shouldNotBe null
    }
}
