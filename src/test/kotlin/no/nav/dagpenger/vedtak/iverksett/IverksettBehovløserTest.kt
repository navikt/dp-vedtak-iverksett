package no.nav.dagpenger.vedtak.iverksett

import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class IverksettBehovløserTest {

    private val testRapid = TestRapid()
    private val ident = "12345678987"
    private val iverksettClient: IverksettClient = mockk()

    init {
        IverksettBehovløser(testRapid, iverksettClient)
    }

    @Test
    fun `motta iverksettingsbehov for rammevedtak, kaller iverksett og løser behovet`() {
        val iverksettDtoSlot = slot<IverksettDto>()
        coEvery { iverksettClient.iverksett(capture(iverksettDtoSlot)) } just Runs

        testRapid.sendTestMessage(behovOmIverksettingAvRammevedtak(ident))

        coVerify(exactly = 1) {
            iverksettClient.iverksett(any())
        }
        iverksettDtoSlot.isCaptured shouldBe true

        testRapid.inspektør.size shouldBe 1
        val iverksattRammevedtak = testRapid.inspektør.message(0)
        iverksattRammevedtak["@løsning"]["Iverksett"].asBoolean() shouldBe true
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
        val iverksattUbetalingsvedtak = testRapid.inspektør.message(0)
        iverksattUbetalingsvedtak["@løsning"]["Iverksett"].asBoolean() shouldBe true
        iverksattUbetalingsvedtak["Iverksett"]["utbetalingsdager"].size() shouldBe 10
    }

    @Test
    fun `motta iverksettingsbehov for utbetalingsvedtak Med forrigeBehandlingId, kaller iverksett og løs behovet`() {
        val iverksettDtoSlot = slot<IverksettDto>()
        coEvery { iverksettClient.iverksett(capture(iverksettDtoSlot)) } just Runs

        testRapid.sendTestMessage(behovOmIverksettingAvUtbetalingsvedtakMedForrigeBehandlingId())

        coVerify(exactly = 1) {
            iverksettClient.iverksett(any())
        }
        iverksettDtoSlot.isCaptured shouldBe true

        testRapid.inspektør.size shouldBe 1
        val iverksattUbetalingsvedtak = testRapid.inspektør.message(0)
        iverksattUbetalingsvedtak["@løsning"]["Iverksett"].asBoolean() shouldBe true
        iverksattUbetalingsvedtak["Iverksett"]["utbetalingsdager"].size() shouldBe 10
    }
}
