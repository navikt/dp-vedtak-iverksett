package no.nav.dagpenger.vedtak.iverksett

import org.junit.jupiter.api.BeforeEach
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class IverksettingTest {

    private val ident = "12345678911"
    private val testObservatør = IverksettingObservatør()
    private val vedtakId = UUID.randomUUID()
    private val behandlingId = UUID.randomUUID()
    private val sakId = "SAKSNUMMER_1"
    private val vedtakstidspunkt = LocalDateTime.now()
    private val virkningsdato = LocalDate.now()

    private lateinit var iverksetting: Iverksetting
    private val inspektør get() = IverksettingInspektør(iverksetting)

    @BeforeEach
    fun setup() {
        iverksetting = Iverksetting(vedtakId, ident).also {
            it.addObserver(testObservatør)
        }
    }
}
