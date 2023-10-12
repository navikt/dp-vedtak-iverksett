package no.nav.dagpenger.vedtak.iverksett.persistens

import kotliquery.Query
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.vedtak.iverksett.Iverksetting
import no.nav.dagpenger.vedtak.iverksett.IverksettingDag
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator
import no.nav.dagpenger.vedtak.iverksett.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.iverksett.Sak
import no.nav.dagpenger.vedtak.iverksett.SakId
import no.nav.dagpenger.vedtak.iverksett.entitet.Beløp
import no.nav.dagpenger.vedtak.iverksett.hendelser.UtbetalingsvedtakFattetHendelse.Utfall
import no.nav.dagpenger.vedtak.iverksett.visitor.SakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

class PostgresSakRepository(private val dataSource: DataSource) : SakRepository {
    override fun hent(sakId: SakId): Sak? {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement =
                        """
                        SELECT ident
                        FROM   sak
                        WHERE  sak_id = :sak_id
                        """.trimIndent(),
                    paramMap = mapOf("sak_id" to sakId),
                ).map
                    { rad ->
                        Sak.rehydrer(
                            ident = rad.string("ident").tilPersonIdentfikator(),
                            sakId = sakId,
                            iverksettinger = mutableListOf(),
                        ).also { session.hentIverksettinger(sakId = sakId) }
                    }.asSingle,
            )
        }
    }

    override fun lagre(sak: Sak) {
        using(sessionOf(dataSource)) { session ->
            session.transaction { transactionalSession: TransactionalSession ->
                val populerQueries = PopulerQueries(sak = sak, session = transactionalSession)
                populerQueries.queries.forEach {
                    transactionalSession.run(it.asUpdate)
                }
            }
        }
    }
}

private class PopulerQueries(
    sak: Sak,
    private val session: Session,
) : SakVisitor {
    val queries = mutableListOf<Query>()
    private var iverksettingDbId: Long? = null

    init {
        sak.accept(this)
    }

    override fun visitSak(
        ident: PersonIdentifikator,
        sakId: SakId,
    ) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement =
                    """
                    INSERT INTO sak(id, ident)
                    VALUES (:id, :ident)
                    ON CONFLICT DO NOTHING
                    """.trimIndent(),
                paramMap =
                    mapOf(
                        "id" to sakId,
                        "ident" to ident,
                    ),
            ),
        )
    }

    override fun preVisitIverksettingDag(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
        utfall: Utfall,
    ) {
        this.iverksettingDbId = session.hentIverksettingDbId(vedtakId)
            ?: session.opprettIverksetting(
                vedtakId = vedtakId,
                behandlingId = behandlingId,
                vedtakstidspunkt = vedtakstidspunkt,
                virkningsdato = virkningsdato,
                utfall = utfall,
            ) ?: throw RuntimeException("Kunne ikke lagre iverksetting for vedtakId $vedtakId. Noe er veldig galt!")
    }

    override fun visitIverksettingDag(
        dato: LocalDate,
        beløp: Beløp,
    ) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement =
                    """
                    INSERT INTO iverksettingsdag(iverksetting_id, dato, beløp)
                    VALUES (:iverksetting_id, :dato, :belop)
                    ON CONFLICT DO NOTHING
                    """.trimIndent(),
                paramMap =
                    mapOf(
                        "iverksetting_id" to iverksettingDbId,
                        "dato" to dato,
                        "belop" to beløp,
                    ),
            ),
        )
    }

    override fun postVisitIverksettingDag(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
        utfall: Utfall,
    ) {
        this.iverksettingDbId = null
    }
}

private fun Session.hentIverksettinger(sakId: SakId) =
    this.run(
        queryOf(
            //language=PostgreSQL
            statement =
                """
                SELECT id
                     , vedtak_id
                     , behandling_id
                     , vedtakstidspunkt
                     , virkningsdato
                     , utfall
                FROM   iverksetting 
                WHERE  sak_id = :sak_id
                """.trimIndent(),
            paramMap = mapOf("sak_id" to sakId),
        ).map { rad ->
            val iverksettingDbId = rad.long("id")
            Iverksetting(
                vedtakId = rad.uuid("vedtak_id"),
                behandlingId = rad.uuid("behandling_id"),
                vedtakstidspunkt = rad.localDateTime("vedtakstidspunkt"),
                virkningsdato = rad.localDate("virkningsdato"),
                utfall =
                    when (rad.string("utfall")) {
                        Utfall.Innvilget.name -> Utfall.Innvilget
                        Utfall.Avslått.name -> Utfall.Avslått
                        else -> {
                            throw IllegalArgumentException(
                                "Ukjent utfall ${rad.string("utfall")} for iverksetting med id ${
                                    rad.uuid(
                                        "id",
                                    )
                                }",
                            )
                        }
                    },
                iverksettingsdager = this.hentIverksettingsdager(iverksettingDbId),
            )
        }.asList,
    )

private fun Session.hentIverksettingDbId(vedtakId: UUID) =
    this.run(
        queryOf(
            //language=PostgreSQL
            statement =
                """
                SELECT  id 
                FROM    iverksetting 
                WHERE   vedtak_id = :vedtak_id
                """.trimIndent(),
            paramMap = mapOf("uuid" to vedtakId),
        ).map { rad ->
            rad.long("id")
        }.asSingle,
    )

private fun Session.hentIverksettingsdager(iverksettingId: Long) =
    this.run(
        queryOf(
            //language=PostgreSQL
            statement =
                """
                SELECT dato, beløp
                FROM   iverksettingsdag
                WHERE  iverksetting_id = :iverksetting_id
                """.trimIndent(),
            paramMap = mapOf("iverksetting_id" to iverksettingId),
        ).map { rad ->
            IverksettingDag(
                dato = rad.localDate("dato"),
                beløp = Beløp(rad.double("beløp")),
            )
        }.asList,
    )

private fun Session.opprettIverksetting(
    vedtakId: UUID,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    utfall: Utfall,
) = this.run(
    queryOf(
        //language=PostgreSQL
        statement =
            """
            INSERT INTO iverksetting
                (vedtak_id, behandling_id, vedtakstidspunkt, virkningsdato, utfall)
            VALUES 
                (:vedtak_id, :behandling_id, :vedtakstidspunkt, :virkningsdato, :utfall)
            RETURNING id;
            """.trimIndent(),
        paramMap =
            mapOf(
                "vedtak_id" to vedtakId,
                "behandling_id" to behandlingId,
                "vedtakstidspunkt" to vedtakstidspunkt,
                "virkningsdato" to virkningsdato,
                "utfall" to utfall.name,
            ),
    ).map { rad ->
        rad.long("id")
    }.asSingle,
)
