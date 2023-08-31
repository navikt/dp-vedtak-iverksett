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
    fun `motta iverksettingsbehov for rammevedtak, skal ikke kalle dp-iverksett, men løse behovet`() {
        val iverksettDtoSlot = slot<IverksettDto>()
        coEvery { iverksettClient.iverksett(capture(iverksettDtoSlot)) } just Runs

        testRapid.sendTestMessage(behovOmIverksettingAvRammevedtak(ident))

        coVerify(exactly = 0) {
            iverksettClient.iverksett(any())
        }
        iverksettDtoSlot.isCaptured shouldBe false

        testRapid.inspektør.size shouldBe 1
        val iverksattRammevedtak = testRapid.inspektør.message(0)
        iverksattRammevedtak["@løsning"]["Iverksett"].asBoolean() shouldBe true
    }
}
