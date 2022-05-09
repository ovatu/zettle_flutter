# zettle

A new flutter plugin project.

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter, view our
[online documentation](https://flutter.dev/docs), which offers tutorials,
samples, guidance on mobile development, and a full API reference.



add to gradle.properties

zettle.githubAccessToken=""

add to build.gradle

android {
    packagingOptions {
        exclude 'META-INF/*.kotlin_module'
    }
}


add to manifest

<activity
    android:name="com.izettle.android.auth.OAuthActivity"
    android:launchMode="singleTask"
    android:taskAffinity="@string/oauth_activity_task_affinity">
    <intent-filter>
        <data
            android:host="[redirect url host]"
            android:scheme="[redirect url scheme]" />
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
    </intent-filter>
</activity>





ios:

add

<key>UISupportedExternalAccessoryProtocols</key>
<array>
    <string>com.izettle.cardreader-one</string>
</array>





<key>UIBackgroundModes</key>
<array>
    <string>bluetooth-central</string>
    <string>external-accessory</string>
</array>

<key>NSBluetoothAlwaysUsageDescription</key>
<string>Our app uses bluetooth to find, connect and transfer data with Zettle card reader devices.</string>

<key>NSBluetoothPeripheralUsageDescription</key>
<string>Our app uses bluetooth to find, connect and transfer data with Zettle card reader devices.</string>

<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleTypeRole</key>
        <string>Editor</string>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>"The scheme of your OAuth Redirect URI *"</string>
        </array>
    </dict>
</array>



<key>NSLocationWhenInUseUsageDescription</key>
<string>You need to allow this to be able to accept card payments</string>
