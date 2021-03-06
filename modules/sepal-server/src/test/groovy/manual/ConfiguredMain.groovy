package manual

import groovy.sql.Sql
import org.flywaydb.core.Flyway
import org.openforis.sepal.Main
import org.openforis.sepal.SepalConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class ConfiguredMain {
    static Logger LOG = LoggerFactory.getLogger(this)

    static void main(String[] args) {
        def config = new SepalConfiguration()
        LOG.info("Migrating database")
        new Sql(config.dataSource).execute('CREATE SCHEMA IF NOT EXISTS "public"')
        new Flyway(
                locations: ["filesystem:modules/mysql/docker/script/sqlScripts"],
                dataSource: config.dataSource,
                schemas: ['PUBLIC']
        ).migrate()
        LOG.info("Starting Sepal")
        Main.main()
    }
}
