package no.nav.dagpenger.vedtak.iverksett

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksett.db.InMemoryMeldingRepository
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
    fun `Utbetalingsvedtak fattet hendelse fører til behov om iverksetting`() {
        testRapid.sendTestMessage(utbetalingsvedtakFattet(ident, vedtakId, behandlingId = UUID.randomUUID(), sakId))
        assertSoftly {
            testRapid.inspektør.size shouldBe 1
            val utbetalingsvedtakJson = testRapid.inspektør.message(0)
            utbetalingsvedtakJson["@event_name"].asText() shouldBe "behov"
            utbetalingsvedtakJson["@behov"].map { it.asText() } shouldBe listOf("IverksettUtbetaling")
        }

        testRapid.sendTestMessage(behovOmIverksettingAvUtbetalingsvedtak(vedtakId))

        iverksettingRepository.hent(vedtakId) shouldNotBe null
    }
}
