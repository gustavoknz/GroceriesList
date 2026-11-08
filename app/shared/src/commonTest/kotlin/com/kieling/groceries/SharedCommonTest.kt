package com.kieling.groceries

import com.kieling.groceries.data.model.AuthUserDto
import com.kieling.groceries.data.model.GroceryItemDto
import com.kieling.groceries.data.model.toDto
import com.kieling.groceries.domain.model.AuthUser
import com.kieling.groceries.domain.model.GroceryItem
import com.kieling.groceries.domain.model.toGroups
import com.kieling.groceries.presentation.model.toUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.kieling.groceries.data.model.toDomain as dtoToDomain

class SharedCommonTest {
    @Test
    fun testGroceryItemDefaults() {
        val item = GroceryItem(1, "Apples")
        assertEquals(1, item.id)
        assertEquals("Apples", item.name)
        assertEquals("General", item.group)
        assertFalse(item.isChecked)
    }

    @Test
    fun testGroceryItemChecked() {
        val item = GroceryItem(id = 2, name = "Milk", isChecked = true, group = "Dairy")
        assertEquals(2, item.id)
        assertEquals("Milk", item.name)
        assertEquals("Dairy", item.group)
        assertTrue(item.isChecked)
    }

    @Test
    fun testGroceryItemCopy() {
        val item1 = GroceryItem(1, "Bread", group = "Bakery")
        val item2 = item1.copy(isChecked = true)
        assertEquals(1, item2.id)
        assertEquals("Bread", item2.name)
        assertEquals("Bakery", item2.group)
        assertTrue(item2.isChecked)
    }

    @Test
    fun testAuthUserCreation() {
        val user = AuthUser("uid123", "test@example.com", "Test User", "https://example.com/photo.jpg")
        assertEquals("uid123", user.uid)
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.displayName)
        assertEquals("https://example.com/photo.jpg", user.photoUrl)
    }

    @Test
    fun testAuthUserNullableFields() {
        val user = AuthUser("uid456", null, null, null)
        assertEquals("uid456", user.uid)
        assertNull(user.email)
        assertNull(user.displayName)
        assertNull(user.photoUrl)
    }

    @Test
    fun testGroceryItemMappers() {
        val dto = GroceryItemDto(1, "Apples", true, "Fruits")
        val domain = dto.dtoToDomain()
        assertEquals(dto.id, domain.id)
        assertEquals(dto.name, domain.name)
        assertEquals(dto.isChecked, domain.isChecked)
        assertEquals(dto.group, domain.group)

        val backToDto = domain.toDto()
        assertEquals(dto, backToDto)

        val ui = domain.toUi()
        assertEquals(domain.id, ui.id)
        assertEquals(domain.name, ui.name)
        assertEquals(domain.isChecked, ui.isChecked)
        assertEquals(domain.group, ui.group)
    }

    @Test
    fun testGroceryGroupMapping() {
        val items =
            listOf(
                GroceryItem(1, "Apples", group = "Produce"),
                GroceryItem(2, "Milk", group = "Dairy"),
                GroceryItem(3, "Carrots", group = "Produce"),
            )
        val groups = items.toGroups()
        assertEquals(2, groups.size)
        assertEquals("Produce", groups[0].name)
        assertEquals(2, groups[0].items.size)
        assertEquals("Dairy", groups[1].name)
        assertEquals(1, groups[1].items.size)

        val groupUi = groups[0].toUi()
        assertEquals("Produce", groupUi.name)
        assertEquals(2, groupUi.items.size)
    }

    @Test
    fun testAuthUserMappers() {
        val dto = AuthUserDto("123", "a@b.com", "Name", "url")
        val domain = dto.dtoToDomain()
        assertEquals(dto.uid, domain.uid)
        assertEquals(dto.email, domain.email)
        assertEquals(dto.displayName, domain.displayName)
        assertEquals(dto.photoUrl, domain.photoUrl)

        val backToDto = domain.toDto()
        assertEquals(dto, backToDto)

        val ui = domain.toUi()
        assertEquals(domain.uid, ui.uid)
        assertEquals(domain.email, ui.email)
        assertEquals(domain.displayName, ui.displayName)
        assertEquals(domain.photoUrl, ui.photoUrl)
    }
}
