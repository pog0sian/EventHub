package ru.eventhub

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import ru.eventhub.support.PostgresTestContainer

@SpringBootTest
@ContextConfiguration(initializers = [PostgresTestContainer.Initializer::class])
class EventhubApplicationTests : PostgresTestContainer() {

	@Test
	fun contextLoads() {
	}

}
