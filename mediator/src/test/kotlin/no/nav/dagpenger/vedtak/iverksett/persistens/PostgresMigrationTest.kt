package no.nav.dagpenger.vedtak.iverksett.persistens

import no.nav.dagpenger.vedtak.iverksett.persistens.Postgres.withCleanDb
import no.nav.dagpenger.vedtak.iverksett.persistens.PostgresDataSourceBuilder.runMigration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PostgresMigrationTest {
    @Test
    fun `Migration scripts are applied successfully`() {
        withCleanDb {
            val migrations = runMigration()
            Assertions.assertEquals(0, migrations)
        }
    }
}
