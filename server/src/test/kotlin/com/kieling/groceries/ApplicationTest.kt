package com.kieling.groceries

import com.kieling.groceries.model.AddItemRequest
import com.kieling.groceries.model.RenameGroupRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun testRoot() =
        testApplication {
            application {
                module()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
            val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("GroceriesList"))
        }

    @Test
    fun testHealth() =
        testApplication {
            application {
                module()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
            val response = client.get("/health")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Hello, Ktor!", response.bodyAsText())
        }

    @Test
    fun testNotFound() =
        testApplication {
            application {
                module()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
            val response = client.get("/nonexistent")
            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun testGetItems() =
        testApplication {
            application {
                module()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
            val response = client.get("/items")
            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun testUserSpecificLists() =
        testApplication {
            application {
                module()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
            // Add item for User A
            val addResponse =
                client.post("/items") {
                    header("X-User-Id", "user_a")
                    contentType(ContentType.Application.Json)
                    setBody(AddItemRequest("User A Item"))
                }
            assertEquals(HttpStatusCode.OK, addResponse.status)
            val bodyA = addResponse.bodyAsText()
            assertTrue(bodyA.contains("User A Item"))

            // Get items for User B should not have User A Item (starts with default items)
            val getResponseB =
                client.get("/items") {
                    header("X-User-Id", "user_b")
                }
            assertEquals(HttpStatusCode.OK, getResponseB.status)
            val bodyB = getResponseB.bodyAsText()
            assertFalse(bodyB.contains("User A Item"))
        }

    @Test
    fun testRenameGroup() =
        testApplication {
            application {
                module()
            }
            val client =
                createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }
            val renameResponse =
                client.put("/groups/rename") {
                    header("X-User-Id", "user_rename_test")
                    contentType(ContentType.Application.Json)
                    setBody(RenameGroupRequest(oldName = "Dairy", newName = "Dairy & Eggs"))
                }
            assertEquals(HttpStatusCode.OK, renameResponse.status)
            val body = renameResponse.bodyAsText()
            assertTrue(body.contains("Dairy & Eggs"))
            assertFalse(body.contains("\"group\":\"Dairy\""))
        }
}
