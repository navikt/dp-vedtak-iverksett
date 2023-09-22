package no.nav.dagpenger.vedtak.iverksett

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldNotBe
import no.nav.dagpenger.vedtak.iverksett.melding.HendelseMediator
import no.nav.dagpenger.vedtak.iverksett.persistens.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.iverksett.persistens.InMemorySakRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class SakMediatorTest {

    private val testRapid = TestRapid()
    private val vedtakId = UUID.fromString("408F11D9-4BE8-450A-8B7A-C2F3F9811859")
    private val ident = "12345123451"
    private val sakId = "SAK_NUMMER_1"
    private val sakRepository = InMemorySakRepository()

    init {
        HendelseMediator(
            rapidsConnection = testRapid,
            hendelseRepository = InMemoryMeldingRepository(),
            sakMediator = SakMediator(sakRepository),
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
        sakRepository.hent(sakId) shouldNotBe null
        assertSoftly {
            // TODO:
            // testRapid.inspektør.size shouldBe 1
            // assert på at vi har sendt ut en hendelse om iverksatt vedtak
        }
    }
}
