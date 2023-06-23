package no.nav.dagpenger.vedtak.iverksett

import io.kotest.matchers.shouldBe
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

class VedtakIverksettingTest {

    private val testRapid: TestRapid = TestRapid()

    private val ident: String = "12345678987"

    init {
        IverksettBehovLøser(testRapid)
    }

    @Test
    fun `motta iverksettingsbehov for rammevedtak, kaller iverksett og løser behovet`() {
        testRapid.sendTestMessage(behovOmIverksettingAvRammevedtak(ident))

        testRapid.inspektør.size shouldBe 1
        val iverksattRammevedtak = testRapid.inspektør.message(0)
        iverksattRammevedtak["@løsning"]["Iverksett"].asBoolean() shouldBe true
        iverksattRammevedtak["Iverksett"]["utbetalingsdager"].size() shouldBe 0
    }

    @Test
    fun `motta iverksettingsbehov for løpendeVedtak, kaller iverksett og løs behovet`() {
    }

    @Test
    fun `motta iverksettingsbehov for rammevedtak, iverksett feiler`() {
    }

    @Test
    fun `motta iverksettingsbehov for løpendeVedtak, iverksett feiler`() {
    }
}
