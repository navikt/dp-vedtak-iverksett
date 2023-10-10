package no.nav.dagpenger.vedtak.iverksett.persistens

import no.nav.dagpenger.vedtak.iverksett.Sak
import no.nav.dagpenger.vedtak.iverksett.SakId
import javax.sql.DataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using

class PostgresSakRepository(private val dataSource: DataSource) : SakRepository {
    override fun hent(sakId: SakId): Sak? {

        var personDbId: Long
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement = """
                        SELECT *
                        FROM   sak
                        WHERE  sak_id = :sak_id
                    """.trimIndent(),
                    paramMap = mapOf("sak_id" to sakId),
                ).map { row ->
                    personDbId = row.long("id")
                    Sak.rehydrer(
                        ident = row.string("ident").tilPersonIdentfikator(),
                        saker = mutableListOf(),
                        vedtak = session.hentVedtak(personDbId),
                        perioder = session.hentRapporteringsperioder(personDbId),

                        ).also { person ->
                        session.hentSaker(person, personDbId)
                    }
                }.asSingle,

                )

        }
    }

    override fun lagre(sak: Sak) {
        TODO("Not yet implemented")
    }
}
