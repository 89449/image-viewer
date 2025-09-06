package app.iv.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun CopiableText(
    label: String,
    value: String
) {
    val clipboardManager = LocalClipboardManager.current
    
    Row {
        Text(text = label)
        Text(
            text = value,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                clipboardManager.setText(AnnotatedString(value))
            }
        )
    }
}