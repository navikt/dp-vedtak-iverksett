package no.nav.dagpenger.vedtak.iverksett

import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.vedtak.iverksett.client.IverksettClient
import no.nav.dagpenger.vedtak.iverksett.persistens.InMemorySakTestRepository
import no.nav.dagpenger.vedtak.iverksett.utils.utbetalingsvedtakFattet
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SakMediatorTest {
    private val testRapid = TestRapid()
    private val førsteVirkningsdato = LocalDate.now().minusDays(14)
    private val ident = "12345123451"
    private val sakId = "SAK_NUMMER_1"
    private val sakRepository = InMemorySakTestRepository()
    private val iverksettClientMock = mockk<IverksettClient>()

    init {
        val sakMediator =
            SakMediator(
                sakRepository = sakRepository,
                iverksettClient = iverksettClientMock,
            )
        UtbetalingsvedtakFattetMottak(testRapid, sakMediator)
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `Hver hendelse om fattet utbetalingsvedtak fører til en iverksetting`() {
        every { iverksettClientMock.iverksett(any()) }.returns(mockk())

        testRapid.sendTestMessage(
            utbetalingsvedtakFattet(
                ident = ident,
                virkningsdato = førsteVirkningsdato,
                dagsbeløp = 700.0,
                sakId = SakId(sakId),
            ),
        )
        verify(exactly = 1) {
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
        verify(exactly = 2) {
            iverksettClientMock.iverksett(any())
        }
        sakRepository.hent(SakId(sakId)) shouldNotBe null
    }
}
