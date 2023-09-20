package no.nav.dagpenger.vedtak.iverksett

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

internal class PersonTest {

    private val ident = "12345678910"
    private val testObservatør = TestObservatør()
    private val person = Person(PersonIdentifikator(ident)).also {
        it.addObserver(testObservatør)
    }

    @Test
    fun `Et utbetalingsvedtak skal bare iverksettes 1 gang, ved nye forsøk, skal det logges en warning i aktivitetsloggen`() {
        val utbetalingsvedtakFattetHendelse = UtbetalingsvedtakFattetHendelse(
            meldingsreferanseId = UUID.randomUUID(),
            ident = ident,
            vedtakId = UUID.randomUUID(),
            behandlingId = UUID.randomUUID(),
            sakId = "SAK_NUMMER_1",
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            virkningsdato = LocalDate.now(),
            utbetalingsdager = emptyList<UtbetalingsvedtakFattetHendelse.Utbetalingsdag>(),
            utfall = UtbetalingsvedtakFattetHendelse.Utfall.Avslått,
        )
        person.håndter(
            utbetalingsvedtakFattetHendelse,
        )
        person.håndter(
            utbetalingsvedtakFattetHendelse,
        )

        testObservatør.iverksettinger.size shouldBe 1
    }

    private class TestObservatør : PersonObserver {

        val iverksettinger = mutableListOf<IverksettingObserver.UtbetalingsvedtakIverksatt>()

        override fun utbetalingsvedtakIverksatt(ident: String, utbetalingsvedtakIverksatt: IverksettingObserver.UtbetalingsvedtakIverksatt) {
            iverksettinger.add(utbetalingsvedtakIverksatt)
        }
    }
}
