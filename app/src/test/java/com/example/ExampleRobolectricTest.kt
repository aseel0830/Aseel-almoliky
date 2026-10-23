package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ItemEntity
import com.example.data.local.ProjectEntity
import com.example.util.SummaryFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    assertEquals("أصيل المليكي", appName)
  }

  @Test
  fun `verify summary formatting with client only and per-box colored wire lines`() {
    val project = ProjectEntity(name = "عمارة الشروق بالرياض", clientName = "أبو فهد")
    val itemsByStage = mapOf(
      "تأسيس السقف" to listOf(
        ItemEntity(stageName = "تأسيس السقف", name = "خراطيم سقف 16مم", quantity = 5, unit = "لفة")
      ),
      "الأسلاك" to listOf(
        ItemEntity(
          stageName = "الأسلاك",
          name = "سلك 2.5 مم",
          quantityMode = "TRIPLE_COLOR",
          q1 = 3, q2 = 2, q3 = 1,
          c1 = "أحمر", c2 = "أزرق", c3 = "أخضر مخطط بأصفر",
          unit = "لفة"
        )
      )
    )

    val summary = SummaryFormatter.generateFormattedSummary(
      project = project,
      technicianName = "أصيل المليكي",
      itemsByStage = itemsByStage
    )

    // Verify project name is omitted and only client name is included
    assertFalse(summary.contains("عمارة الشروق بالرياض"))
    assertTrue(summary.contains("العميل: أبو فهد"))

    // Verify items format without stage names
    assertFalse(summary.contains("[تأسيس السقف]"))
    assertTrue(summary.contains("- 5 لفة = خراطيم سقف 16مم"))

    // Verify colored items format
    assertTrue(summary.contains("- 3 لفة = سلك 2.5 مم (أحمر)"))
    assertTrue(summary.contains("- 2 لفة = سلك 2.5 مم (أزرق)"))
    assertTrue(summary.contains("- 1 لفة = سلك 2.5 مم (أخضر مخطط بأصفر)"))

    // Verify signature
    assertTrue(summary.contains("تحياتي، [أصيل المليكي]"))
  }

  @Test
  fun `verify custom technician name signature`() {
    val project = ProjectEntity(name = "مشروع 1", clientName = "سالم الدوسري")
    val summary = SummaryFormatter.generateFormattedSummary(
      project = project,
      technicianName = "مهندس علي",
      technicianPhone = "0555555555",
      itemsByStage = emptyMap()
    )
    assertTrue(summary.contains("العميل: سالم الدوسري"))
    assertTrue(summary.contains("تحياتي، [مهندس علي - 0555555555]"))
  }

  @Test
  fun `verify sms plain format strictly follows quantity unit equal name`() {
    val itemsByStage = mapOf(
      "مفاتيح وأفياش" to listOf(
        ItemEntity(stageName = "مفاتيح وأفياش", name = "مفتاح انارة خط ١", quantity = 2, unit = "حبة")
      )
    )
    val sms = SummaryFormatter.generateSmsPlainList(itemsByStage)
    assertEquals("2 حبة= مفتاح انارة خط ١", sms)
  }
}
