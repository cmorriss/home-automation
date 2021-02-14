package io.morrissey.iot.server.persistence

import com.google.inject.Guice
import com.google.inject.util.Modules
import dev.misfitlabs.kotlinguice4.KotlinModule
import dev.misfitlabs.kotlinguice4.getBinding
import io.mockk.mockk
import io.morrissey.iot.server.aws.AutomationSynchronizer
import io.morrissey.iot.server.modules.AwsModule
import org.jetbrains.exposed.sql.Database
import software.amazon.awssdk.services.eventbridge.EventBridgeClient
import java.io.File
import java.nio.file.Files
import java.util.Random

class TestDb : ApplicationDatabase {
    override fun initialize() {
        val dbStaticInjectionModule = Modules.override(AwsModule()).with(DbInjectionOverrides())
        Guice.createInjector(dbStaticInjectionModule)

        val TEST_DB_DIR = "/tmp/test-dbs"
        val dbDir = File(TEST_DB_DIR)

        if (dbDir.exists()) {
            deleteDir(dbDir)
        }
        dbDir.mkdirs()

        val dir = File("$TEST_DB_DIR/tmp_${Random().nextInt(100000)}")
        //Database.connect(url = "jdbc:h2:mem:test", driver = "org.h2.Driver")
        Database.connect(url = "jdbc:h2:file:${dir.canonicalFile.absolutePath}", driver = "org.h2.Driver")
        createProdDbTables()
    }

    private fun deleteDir(file: File) {
        val contents = file.listFiles()
        if (contents != null) {
            for (f in contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    deleteDir(f)
                }
            }
        }
        file.delete()
    }

    private class DbInjectionOverrides : KotlinModule() {
        override fun configure() {
            bind<EventBridgeClient>().toInstance(mockk())
            bind<AutomationSynchronizer>().toInstance(mockk())
        }
    }
}
