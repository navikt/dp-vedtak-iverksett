package no.nav.dagpenger.vedtak.iverksett.persistens

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator
import no.nav.dagpenger.vedtak.iverksett.Sak
import no.nav.dagpenger.vedtak.iverksett.SakId
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksett.utils.Postgres.withMigratedDb
import no.nav.dagpenger.vedtak.iverksett.utils.assertDeepEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.absoluteValue

class PostgresSakRepositoryTest {
    private val ident = PersonIdentifikator("12345678901")
    private val sakId = SakId("SAK_NR_123")
    private val ukedagIdag = LocalDate.now().dayOfWeek

    @Test
    fun `lagrer og henter sak`() {
        val sak = Sak(ident = ident, sakId = sakId, iverksettinger = mutableListOf())

        withMigratedDb {
            val postgresSakRepository = PostgresSakRepository(PostgresDataSourceBuilder.dataSource)
            postgresSakRepository.lagre(sak)
            shouldNotThrowAny {
                postgresSakRepository.lagre(sak)
            }
            val hentetSak = postgresSakRepository.hent(sakId).shouldNotBeNull()
            sak.sakId() shouldBe hentetSak.sakId()

            assertAntallRader("sak", 1)
        }
    }

    @Test
    fun `lagrer og henter komplett sak med to iverksettinger`() {
        val sak = Sak(ident = ident, sakId = sakId, iverksettinger = mutableListOf())
        val førsteVirkningsdato = LocalDate.now().minusDays(ukedagIdag.value.toLong())
        val førsteUtbetalingsdager = utbetalingsdager(førsteVirkningsdato, 500.0)
        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                vedtakId = UUID.randomUUID(),
                behandlingId = UUID.randomUUID(),
                virkningsdato = førsteVirkningsdato,
                utbetalingsdager = førsteUtbetalingsdager,
            ),
        )
        val andreVirkningsdato = førsteVirkningsdato.plusDays(14)
        val andreUtbetalingsdager = utbetalingsdager(andreVirkningsdato, 600.0)
        sak.håndter(
            utbetalingsvedtakFattetHendelse(
                vedtakId = UUID.randomUUID(),
                behandlingId = UUID.randomUUID(),
                virkningsdato = andreVirkningsdato,
                utbetalingsdager = andreUtbetalingsdager,
            ),
        )

        withMigratedDb {
            val postgresSakRepository = PostgresSakRepository(PostgresDataSourceBuilder.dataSource)
            postgresSakRepository.lagre(sak)

            val rehydrertSak = postgresSakRepository.hent(sakId).shouldNotBeNull()

            assertDeepEquals(sak, rehydrertSak)
        }
    }

    private fun assertAntallRader(
        tabell: String,
        antallRader: Int,
    ) {
        val faktiskeRader =
            using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
                session.run(
                    queryOf("select count(1) from $tabell").map { row ->
                        row.int(1)
                    }.asSingle,
                )
            }
        Assertions.assertEquals(antallRader, faktiskeRader, "Feil antall rader for tabell: $tabell")
    }

    private fun utbetalingsvedtakFattetHendelse(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        utbetalingsdager: List<UtbetalingsvedtakFattetHendelse.Utbetalingsdag>,
    ) = UtbetalingsvedtakFattetHendelse(
        meldingsreferanseId = UUID.randomUUID(),
        ident = ident.identifikator(),
        vedtakId = vedtakId,
        behandlingId = behandlingId,
        sakId = sakId.sakId,
        vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
        virkningsdato = virkningsdato,
        utfall = UtbetalingsvedtakFattetHendelse.Utfall.Innvilget,
        utbetalingsdager = utbetalingsdager,
    )

    private fun utbetalingsdager(
        virkningsdato: LocalDate,
        dagsbeløp: Double,
    ): MutableList<UtbetalingsvedtakFattetHendelse.Utbetalingsdag> {
        val utbetalingsdager = mutableListOf<UtbetalingsvedtakFattetHendelse.Utbetalingsdag>()

        for (i in -13..0) {
            val dato = virkningsdato.minusDays(i.absoluteValue.toLong())
            val beløp =
                if (dato.dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
                    0.0
                } else {
                    dagsbeløp
                }

            utbetalingsdager.add(
                UtbetalingsvedtakFattetHendelse.Utbetalingsdag(
                    dato = dato,
                    beløp = beløp,
                ),
            )
        }
        return utbetalingsdager
    }
}
