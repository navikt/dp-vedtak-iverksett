package no.nav.dagpenger.vedtak.iverksett

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.iverksett.client.IverksettClient
import no.nav.dagpenger.vedtak.iverksett.persistens.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.vedtak.iverksett.persistens.PostgresSakRepository
import no.nav.dagpenger.vedtak.iverksett.utils.Postgres.withMigratedDb
import no.nav.dagpenger.vedtak.iverksett.utils.utbetalingsvedtakFattet
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class KomponentTest {
    private val testRapid = TestRapid()
    private val førsteVirkningsdato = LocalDate.now().minusDays(14)
    private val ident = "12345123451"
    private val sakId = "SAK_NUMMER_1"
    private val iverksettClientMock = mockk<IverksettClient>()

    @Test
    fun `lagrer og henter sak`() {
        val sak = Sak(ident = ident.tilPersonIdentfikator(), sakId = SakId(sakId), iverksettinger = mutableListOf())
        every { iverksettClientMock.iverksett(any()) }.returns(mockk())

        testRapid.sendTestMessage(
            utbetalingsvedtakFattet(
                ident = ident,
                virkningsdato = førsteVirkningsdato,
                dagsbeløp = 700.0,
                sakId = SakId(sakId),
            ),
        )

        withMigratedDb {
            val postgresSakRepository = PostgresSakRepository(dataSource)
            postgresSakRepository.lagre(sak)
            shouldNotThrowAny {
                postgresSakRepository.lagre(sak)
            }
            val hentetSak = postgresSakRepository.hent(SakId(sakId)).shouldNotBeNull()
            sak.sakId() shouldBe hentetSak.sakId()

            assertAntallRader("sak", 1)
        }
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
        Assertions.assertEquals(antallRader, faktiskeRader, "Feil antall rader for tabell: $tabell")
    }
}
