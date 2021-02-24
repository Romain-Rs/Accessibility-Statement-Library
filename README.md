Accessibility-Statement-Library
------

Accessibility Statement Library contains a view showing the WCAG compliance status for its Android application.

To use it, you need the XML accessibility result file from the Orange va11ydette.

Gradle
------
```

allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    ...
    implementation 'com.github.Romain-Rs:Accessibility-Statement-Library:0.1.1'
}
```

Usage
-----
```xml
<com.orange.accessibilitystatementlibrary.StatementView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:details_url="@string/url"
        app:declarant="@string/declarant"/>
```

* Add the results XML file from the va11ydette in the Assets folder of your project and rename it "accessibility_result.xml"
* Fill in the attribute "app:details_url" with the complete accessibility statement URL of the app
* Fill in the attribute "app:declarant" with the declarant identity of the accessibility statement
