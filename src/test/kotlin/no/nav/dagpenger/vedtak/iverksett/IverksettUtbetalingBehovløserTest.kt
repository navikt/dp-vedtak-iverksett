package no.nav.dagpenger.vedtak.iverksett

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class IverksettUtbetalingBehovløserTest {

    private val testRapid = TestRapid()
    private val iverksettClient: IverksettClient = mockk()

    init {
        IverksettUtbetalingBehovløser(testRapid, iverksettClient)
    }

    @Test
    fun `motta iverksettingsbehov for utbetalingsvedtak uten forrigeBehandlingId, kaller iverksett og løs behovet`() {
        val iverksettDtoSlot = slot<IverksettDto>()
        coEvery { iverksettClient.iverksett(capture(iverksettDtoSlot)) } just Runs

        testRapid.sendTestMessage(behovOmIverksettingAvUtbetalingsvedtakUtenForrigeBehandlingId())

        coVerify(exactly = 1) {
            iverksettClient.iverksett(any())
        }
        iverksettDtoSlot.isCaptured shouldBe true

        testRapid.inspektør.size shouldBe 1
        val iverksattUtbetalingsvedtak = testRapid.inspektør.message(0)
        iverksattUtbetalingsvedtak["@løsning"]["IverksettUtbetaling"].asBoolean() shouldBe true
        iverksattUtbetalingsvedtak["IverksettUtbetaling"]["utbetalingsdager"].size() shouldBe 10
        iverksattUtbetalingsvedtak["IverksettUtbetaling"]["forrigeBehandlingId"] shouldBe null
    }

    @Test
    fun `motta iverksettingsbehov for utbetalingsvedtak med forrigeBehandlingId, kaller iverksett og løs behovet`() {
        val iverksettDtoSlot = slot<IverksettDto>()
        coEvery { iverksettClient.iverksett(capture(iverksettDtoSlot)) } just Runs

        testRapid.sendTestMessage(behovOmIverksettingAvUtbetalingsvedtakMedForrigeBehandlingId())

        coVerify(exactly = 1) {
            iverksettClient.iverksett(any())
        }
        iverksettDtoSlot.isCaptured shouldBe true

        testRapid.inspektør.size shouldBe 1
        val iverksattUtbetalingsvedtak = testRapid.inspektør.message(0)
        iverksattUtbetalingsvedtak["@løsning"]["IverksettUtbetaling"].asBoolean() shouldBe true
        iverksattUtbetalingsvedtak["IverksettUtbetaling"]["utbetalingsdager"].size() shouldBe 10
        iverksattUtbetalingsvedtak["IverksettUtbetaling"]["forrigeBehandlingId"] shouldNotBe null
    }
}
