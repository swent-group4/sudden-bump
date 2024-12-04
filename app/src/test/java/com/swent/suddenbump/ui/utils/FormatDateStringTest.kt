package com.swent.suddenbump.ui.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatDateStringTest {

  @Test
  fun `test valid date input`() {
    val input = TextFieldValue("12122023", TextRange(8))
    val expected = TextFieldValue("12/12/2023", TextRange(10))
    val result = formatDateString(input)
    assertEquals(expected, result)
  }

  @Test
  fun `test input with extra digits`() {
    val input = TextFieldValue("12122023123", TextRange(11))
    val expected = TextFieldValue("12/12/2023", TextRange(10))
    val result = formatDateString(input)
    assertEquals(expected, result)
  }

  @Test
  fun `test input with non-numeric characters`() {
    val input = TextFieldValue("12ab12cd2023", TextRange(12))
    val expected = TextFieldValue("12/12/2023", TextRange(10))
    val result = formatDateString(input)
    assertEquals(expected, result)
  }

  @Test
  fun `test partial input without slashes`() {
    val input = TextFieldValue("123", TextRange(3))
    val expected = TextFieldValue("12/3", TextRange(4))
    val result = formatDateString(input)
    assertEquals(expected, result)
  }

  @Test
  fun `test empty input`() {
    val input = TextFieldValue("", TextRange(0))
    val expected = TextFieldValue("", TextRange(0))
    val result = formatDateString(input)
    assertEquals(expected, result)
  }

  @Test
  fun `test cursor adjustment`() {
    val input = TextFieldValue("1234", TextRange(3))
    val expected = TextFieldValue("12/34", TextRange(4))
    val result = formatDateString(input)
    assertEquals(expected, result)
  }

  @Test
  fun `test input with slashes already present`() {
    val input = TextFieldValue("12/12/2023", TextRange(10))
    val expected = TextFieldValue("12/12/2023", TextRange(10))
    val result = formatDateString(input)
    assertEquals(expected, result)
  }
}
