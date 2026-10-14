package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ARENA X", appName)
  }

  @Test
  fun `verify admin username normalization`() {
    val normalized = com.example.util.AdminAuthUtil.normalizeUsername("𝑴𝒖𝒔𝒌𝒂𝒏 𝑷𝒂𝒏𝒆𝒍 𝑺𝒕𝒐𝒓𝒆")
    assertEquals(com.example.util.AdminAuthUtil.ADMIN_USERNAME_NORMALIZED, normalized)
  }

  @Test
  fun `verify admin password hashing and authentication`() {
    val isValid = com.example.util.AdminAuthUtil.verifyPassword(
      "MuskanBot@2026",
      com.example.util.AdminAuthUtil.ADMIN_SALT_HEX,
      com.example.util.AdminAuthUtil.ADMIN_PASSWORD_HASH_HEX
    )
    assertEquals(true, isValid)

    val isInvalid = com.example.util.AdminAuthUtil.verifyPassword(
      "WrongPassword123",
      com.example.util.AdminAuthUtil.ADMIN_SALT_HEX,
      com.example.util.AdminAuthUtil.ADMIN_PASSWORD_HASH_HEX
    )
    assertEquals(false, isInvalid)
  }
}
