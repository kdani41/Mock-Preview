# Mock Preview
[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.kdani41/mock-preview)](https://central.sonatype.com/artifact/io.github.kdani41/mock-preview/)

### Description
Light weight library based of [mockk](https://mockk.io/) for supporting mocks in [compose preview](https://developer.android.com/develop/ui/compose/tooling/previews).
Special thanks to contributors at [mockk](https://mockk.io/)

### Features
- Allows to mock objects for compose previews.

### Installation
```kotlin 

dependencies {
    debugImplementation("io.github.kdani41:mock-preview:[version]")
    releaseImplementation("io.github.kdani41:mock-preview-no-op:[version]")
}

```

### [Usage](https://github.com/kdani41/mock-preview/tree/main/sample/src/main/java/com/kdani/mockpreview)
#### SampleScreen.kt
```kotlin 

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
fun PreviewSampleScreen() {
    val analytics = mockPreview<Analytics>()
    val dataSource = mockPreview<DataSource> {
        forThis { title } returns "JohDoe"
        forThis { address } returns "Menlo Park California"
    }
    val sampleScreen = SampleScreen(analytics, dataSource)
    MockPreviewTheme {
        sampleScreen.Render()
    }
}

```
![sample-demo.png](sample-demo.png "Compose preview")
