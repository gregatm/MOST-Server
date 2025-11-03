package de.muenchen.mostserver.config

import de.muenchen.mostserver.data.dao.DatastreamDao
import de.muenchen.mostserver.data.dao.SensorDao
import de.muenchen.mostserver.data.dao.ThingDao
import de.muenchen.mostserver.odata.EdmEntityProviderGenerated
import de.muenchen.mostserver.odata.createFromClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EdmProvider {
    @Bean
    fun edmProvider(): EdmEntityProviderGenerated {
        return createFromClass(listOf(ThingDao::class.java, SensorDao::class.java, DatastreamDao::class.java))
    }
}