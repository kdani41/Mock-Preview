package com.kdani.mockpreview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kdani.mockpreview.ui.theme.MockPreviewTheme

class SampleScreen(
    private val analytics: Analytics,
    private val dataSource: DataSource
) {

    @Composable
    fun Render() {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .clickable { analytics.trackClick() }
        ) {
            Text(text = dataSource.title, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = dataSource.address, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
fun PreviewSampleScreen() {
    val analytics = mockPreview<Analytics>()
    val dataSource = mockPreview<DataSource> {
        forThis { title } returns "Mr. John Doe"
        forThis { address } returns "Menlo Park California"
    }
    val sampleScreen = SampleScreen(analytics, dataSource)
    MockPreviewTheme {
        sampleScreen.Render()
    }
}