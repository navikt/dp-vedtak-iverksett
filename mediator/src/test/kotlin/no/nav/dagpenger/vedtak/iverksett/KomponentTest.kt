package no.nav.dagpenger.vedtak.iverksett

import io.mockk.every
import io.mockk.mockk
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.vedtak.iverksett.client.IverksettClient
import no.nav.dagpenger.vedtak.iverksett.persistens.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.vedtak.iverksett.persistens.PostgresSakRepository
import no.nav.dagpenger.vedtak.iverksett.utils.Postgres.withMigratedDb
import no.nav.dagpenger.vedtak.iverksett.utils.utbetalingsvedtakFattet
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class KomponentTest {
    private val testRapid = TestRapid()
    private val iverksettClientMock = mockk<IverksettClient>()

    @Test
    fun `hendelse fører til iverksetting og lagring av sak`() =
        withMigratedDb {
            val postgresSakRepository = PostgresSakRepository(dataSource)
            val sakMediator = SakMediator(sakRepository = postgresSakRepository, iverksettClient = iverksettClientMock)
            UtbetalingsvedtakFattetMottak(testRapid, sakMediator)
            every { iverksettClientMock.iverksett(any()) }.returns(mockk())

            testRapid.sendTestMessage(
                utbetalingsvedtakFattet(
                    ident = "12345678901",
                    virkningsdato = LocalDate.now(),
                    dagsbeløp = 800.0,
                    sakId = SakId(UUID.randomUUID().toString()),
                ),
            )

            assertAntallRader("sak", 1)
        }

    private fun assertAntallRader(
        tabell: String,
        antallRader: Int,
    ) {
        val faktiskeRader =
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf("select count(1) from $tabell").map { row ->
                        row.int(1)
                    }.asSingle,
                )
            }
        assertEquals(antallRader, faktiskeRader, "Feil antall rader for tabell: $tabell")
    }
}
