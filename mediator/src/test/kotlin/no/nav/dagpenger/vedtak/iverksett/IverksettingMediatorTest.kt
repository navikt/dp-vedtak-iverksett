package no.nav.dagpenger.vedtak.iverksett

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksett.melding.HendelseMediator
import no.nav.dagpenger.vedtak.iverksett.persistens.InMemoryIverksettingRepository
import no.nav.dagpenger.vedtak.iverksett.persistens.InMemoryMeldingRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class IverksettingMediatorTest {

    private val testRapid = TestRapid()
    private val vedtakId = UUID.fromString("408F11D9-4BE8-450A-8B7A-C2F3F9811859")
    private val ident = "12345123451"
    private val sakId = "SAK_NUMMER_1"
    private val iverksettingRepository = InMemoryIverksettingRepository()

    init {
        HendelseMediator(
            rapidsConnection = testRapid,
            hendelseRepository = InMemoryMeldingRepository(),
            iverksettingMediator = IverksettingMediator(
                aktivitetsloggMediator = mockk(relaxed = true),
                iverksettingRepository = iverksettingRepository,
                behovMediator = BehovMediator(testRapid, KotlinLogging.logger {}),
            ),
        )
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `Utbetalingsvedtak fattet hendelse fører til iverksetting, samt hendelse om iverksatt vedtak`() {
        testRapid.sendTestMessage(utbetalingsvedtakFattet(ident, vedtakId, behandlingId = UUID.randomUUID(), sakId))
        // assert at vi får 202 fra iverksetting
        iverksettingRepository.hent(vedtakId) shouldNotBe null
        assertSoftly {
            // TODO:
            // testRapid.inspektør.size shouldBe 1
            // assert på at vi har sendt ut en hendelse om iverksatt vedtak
        }
    }
}
