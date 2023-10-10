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
import no.nav.dagpenger.vedtak.iverksett.persistens.Postgres.withMigratedDb
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PostgresSakRepositoryTest {
    val ident = PersonIdentifikator("12345678901")
    val sakId = SakId("SAK_NR_123")

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
}
