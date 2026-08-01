package io.github.zorinontop.twojastokrotka.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 9) text.text.substring(0..8) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 2 || i == 5) out += " "
        }

        val phoneOffsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset + 1
                if (offset <= 9) return offset + 2
                return 11
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 6) return offset - 1
                if (offset <= 11) return offset - 2
                return 9
            }
        }

        return TransformedText(AnnotatedString(out), phoneOffsetMapping)
    }
}

fun formatPhoneNumber(phone: String): String {
    val digits = phone.filter { it.isDigit() }.takeLast(9)
    return if (digits.length == 9) {
        "${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6, 9)}"
    } else {
        digits
    }
}
