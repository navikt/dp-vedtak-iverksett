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
                        WHERE  id = :sak_id
                        """.trimIndent(),
                    paramMap = mapOf("sak_id" to sakId.sakId),
                ).map
                    { rad ->
                        Sak.rehydrer(
                            ident = rad.string("ident").tilPersonIdentfikator(),
                            sakId = sakId,
                            iverksettinger = hentSakensIverksettinger(sakId = sakId, session = session),
                        )
                    }.asSingle,
            )
        }
    }

    override fun lagre(sak: Sak) {
        using(sessionOf(dataSource)) { session ->
            session.transaction { transactionalSession: TransactionalSession ->
                val populerQueries = PopulerQueries(sak = sak)
                populerQueries.queries.forEach {
                    transactionalSession.run(it.asUpdate)
                }
            }
        }
    }
}

private class PopulerQueries(
    sak: Sak,
) : SakVisitor {
    val queries = mutableListOf<Query>()
    private var sakId: SakId? = null
    private var vedtakId: UUID? = null

    init {
        sak.accept(this)
    }

    override fun visitSak(
        ident: PersonIdentifikator,
        sakId: SakId,
    ) {
        this.sakId = sakId
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement =
                    """
                    INSERT INTO sak
                        (id, ident, endret)
                    VALUES
                        (:id, :ident, now())
                    ON CONFLICT DO NOTHING
                    """.trimIndent(),
                paramMap =
                    mapOf(
                        "id" to sakId.sakId,
                        "ident" to ident.identifikator(),
                    ),
            ),
        )
    }

    override fun visitIverksetting(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        virkningsdato: LocalDate,
        utfall: Utfall,
    ) {
        this.vedtakId = vedtakId
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement =
                    """
                    INSERT INTO iverksetting
                        (sak_id, vedtak_id, behandling_id, vedtakstidspunkt, virkningsdato, utfall)
                    VALUES 
                        (:sak_id, :vedtak_id, :behandling_id, :vedtakstidspunkt, :virkningsdato, :utfall)
                    ON CONFLICT DO NOTHING
                    """.trimIndent(),
                paramMap =
                    mapOf(
                        "sak_id" to this.sakId!!.sakId,
                        "vedtak_id" to vedtakId,
                        "behandling_id" to behandlingId,
                        "vedtakstidspunkt" to vedtakstidspunkt,
                        "virkningsdato" to virkningsdato,
                        "utfall" to utfall.name,
                    ),
            ),
        )
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
                    INSERT INTO iverksettingsdag
                        (vedtak_id, dato, beløp)
                    VALUES
                        (:vedtak_id, :dato, :belop)
                    ON CONFLICT DO NOTHING
                    """.trimIndent(),
                paramMap =
                    mapOf(
                        "vedtak_id" to this.vedtakId,
                        "dato" to dato,
                        "belop" to beløp.verdi,
                    ),
            ),
        )
    }
}

private fun hentSakensIverksettinger(
    sakId: SakId,
    session: Session,
): MutableList<Iverksetting> {
    val iverksettinger = mutableListOf<Iverksetting>()
    iverksettinger.addAll(session.hentIverksettinger(sakId))
    return iverksettinger
}

private fun Session.hentIverksettinger(sakId: SakId) =
    this.run(
        queryOf(
            //language=PostgreSQL
            statement =
                """
                SELECT vedtak_id
                     , behandling_id
                     , vedtakstidspunkt
                     , virkningsdato
                     , utfall
                FROM   iverksetting 
                WHERE  sak_id = :sak_id
                """.trimIndent(),
            paramMap = mapOf("sak_id" to sakId.sakId),
        ).map { rad ->
            val vedtakId = rad.uuid("vedtak_id")
            Iverksetting(
                vedtakId = vedtakId,
                behandlingId = rad.uuid("behandling_id"),
                vedtakstidspunkt = rad.localDateTime("vedtakstidspunkt"),
                virkningsdato = rad.localDate("virkningsdato"),
                utfall =
                    when (rad.string("utfall")) {
                        Utfall.Innvilget.name -> Utfall.Innvilget
                        Utfall.Avslått.name -> Utfall.Avslått
                        else -> {
                            throw IllegalArgumentException(
                                "Ukjent utfall ${rad.string("utfall")} ved iverksetting av vedtakId $vedtakId",
                            )
                        }
                    },
                iverksettingsdager = this.hentSorterteIverksettingsdager(vedtakId),
            )
        }.asList,
    )

private fun Session.hentSorterteIverksettingsdager(vedtakId: UUID): List<IverksettingDag> {
    val iverksettingsdager =
        this.run(
            queryOf(
                //language=PostgreSQL
                statement =
                    """
                    SELECT dato, beløp
                    FROM   iverksettingsdag
                    WHERE  vedtak_id = :vedtak_id
                    """.trimIndent(),
                paramMap = mapOf("vedtak_id" to vedtakId),
            ).map { rad ->
                IverksettingDag(
                    dato = rad.localDate("dato"),
                    beløp = Beløp(rad.double("beløp")),
                )
            }.asList,
        )
    return iverksettingsdager.sorted()
}
