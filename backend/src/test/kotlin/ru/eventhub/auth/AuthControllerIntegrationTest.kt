package ru.eventhub.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.resttestclient.exchange
import org.springframework.boot.resttestclient.postForEntity
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import ru.eventhub.auth.dto.AuthResponse
import ru.eventhub.auth.dto.CurrentUserResponse
import ru.eventhub.auth.dto.LoginRequest
import ru.eventhub.auth.dto.RegisterRequest
import ru.eventhub.support.PostgresTestContainer
import ru.eventhub.user.model.RoleName
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ContextConfiguration(initializers = [PostgresTestContainer.Initializer::class])
class AuthControllerIntegrationTest : PostgresTestContainer() {
    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `register login and current user work with jwt`() {
        val email = "student-${UUID.randomUUID()}@example.com"
        val password = "password123"

        val registerResponse = restTemplate.postForEntity<AuthResponse>(
            "/api/auth/register",
            RegisterRequest(
                email = email,
                password = password,
                firstName = "Ivan",
                lastName = "Petrov",
                patronymic = "Sergeevich",
            ),
        )

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        val registered = requireNotNull(registerResponse.body)
        assertThat(registered.accessToken).isNotBlank()
        assertThat(registered.tokenType).isEqualTo("Bearer")
        assertThat(registered.user.email).isEqualTo(email)
        assertThat(registered.roles).containsExactly(RoleName.STUDENT)

        val loginResponse = restTemplate.postForEntity<AuthResponse>(
            "/api/auth/login",
            LoginRequest(
                email = email,
                password = password,
            ),
        )

        assertThat(loginResponse.statusCode).isEqualTo(HttpStatus.OK)

        val loggedIn = requireNotNull(loginResponse.body)
        assertThat(loggedIn.accessToken).isNotBlank()
        assertThat(loggedIn.user.id).isEqualTo(registered.user.id)
        assertThat(loggedIn.roles).containsExactly(RoleName.STUDENT)

        val headers = HttpHeaders()
        headers.setBearerAuth(loggedIn.accessToken)
        headers.set("X-Active-Role", RoleName.STUDENT.name)

        val currentUserResponse = restTemplate.exchange<CurrentUserResponse>(
            "/api/auth/me",
            HttpMethod.GET,
            HttpEntity<Any>(headers),
        )

        assertThat(currentUserResponse.statusCode).isEqualTo(HttpStatus.OK)

        val currentUser = requireNotNull(currentUserResponse.body)
        assertThat(currentUser.user.email).isEqualTo(email)
        assertThat(currentUser.roles).containsExactly(RoleName.STUDENT)
        assertThat(currentUser.activeRole).isEqualTo(RoleName.STUDENT)
    }

    @Test
    fun `student active role cannot access admin endpoint`() {
        val email = "student-${UUID.randomUUID()}@example.com"
        val password = "password123"

        val registerResponse = restTemplate.postForEntity<AuthResponse>(
            "/api/auth/register",
            RegisterRequest(
                email = email,
                password = password,
                firstName = "Ivan",
                lastName = "Petrov",
                patronymic = null,
            ),
        )

        assertThat(registerResponse.statusCode).isEqualTo(HttpStatus.CREATED)

        val registered = requireNotNull(registerResponse.body)

        val headers = HttpHeaders()
        headers.setBearerAuth(registered.accessToken)
        headers.set("X-Active-Role", RoleName.STUDENT.name)

        val adminResponse = restTemplate.exchange<String>(
            "/api/admin/users",
            HttpMethod.GET,
            HttpEntity<Any>(headers),
        )

        assertThat(adminResponse.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}