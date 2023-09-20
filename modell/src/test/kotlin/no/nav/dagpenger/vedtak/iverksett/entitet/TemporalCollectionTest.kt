package no.nav.dagpenger.vedtak.iverksett.entitet

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate

internal class TemporalCollectionTest {
    private lateinit var satser: TemporalCollection<BigDecimal>
    private val lavSats = BigDecimal(5)
    private val høySats = BigDecimal(10)
    private val iDag = LocalDate.now()

    @BeforeEach
    fun setUp() {
        satser = TemporalCollection()
        satser.put(iDag.minusDays(9), lavSats)
        satser.put(iDag.minusDays(1), høySats)
    }

    @Test
    fun `får riktig sats til riktig dato`() {
        assertThrows<IllegalArgumentException> {
            satser.get(LocalDate.now().minusDays(10))
        }
        Assertions.assertEquals(lavSats, satser.get(iDag.minusDays(9)))
        Assertions.assertEquals(lavSats, satser.get(iDag.minusDays(2)))
        Assertions.assertEquals(høySats, satser.get(iDag.minusDays(1)))
        Assertions.assertEquals(høySats, satser.get(iDag))
    }

    @Test
    fun `Satt på samme dato`() {
        satser.put(iDag, lavSats)
        satser.put(iDag, høySats)

        Assertions.assertEquals(høySats, satser.get(iDag))
    }
}
