# Zettle POS SDK for Flutter

[![pub package](https://img.shields.io/pub/v/zettle.svg)](https://pub.dev/packages/zettle) [![likes](https://badges.bar/zettle/likes)](https://pub.dev/packages/zettle/score) [![popularity](https://badges.bar/zettle/popularity)](https://pub.dev/packages/zettle/score)  [![pub points](https://badges.bar/zettle/pub%20points)](https://pub.dev/packages/zettle/score)

A Flutter wrapper to use the Zettle POS SDK.

With this plugin, your app can easily request payment via the Zettle readers on Android and iOS.

## Note ⚠️
You Must Call the init function Only once a time in your app

```dart
Zettle.init(iosClientId, androidClientId, redirectUrl);
```

## Prerequisites

1) Registered for a Zettle developer account via [Zettle](https://developer.zettle.com/).
2) Deployment Target iOS 12.0 or higher.
3) Android minSdkVersion 21 or higher.

## Android

Add to build.gradle (as per https://github.com/iZettle/sdk-android)
Add your personal access token to build.gradle (as per https://github.com/iZettle/sdk-android), this is used to access the Zettle maven repo on github

```
android {
    packagingOptions {
        exclude 'META-INF/*.kotlin_module'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/iZettle/sdk-android")
            credentials(HttpHeaderCredentials) {
                name "Authorization"
                value "Bearer <YOUR TOKEN HERE>"
            }
            authentication {
                header(HttpHeaderAuthentication)
            }
        }
    }
}
```

Add to manifest (as per https://github.com/iZettle/sdk-android)

```
<activity
    android:name="com.izettle.android.auth.OAuthActivity"
    android:launchMode="singleTask"
    android:taskAffinity="@string/oauth_activity_task_affinity"
    android:exported="true">
    <intent-filter>
        <data
            android:host="[redirect url host]"
            android:scheme="[redirect url scheme]" />
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
    </intent-filter>
</activity>
```



## iOS

Add reader protocol to info.plist (https://github.com/iZettle/sdk-ios)

```
<key>UISupportedExternalAccessoryProtocols</key>
<array>
    <string>com.izettle.cardreader-one</string>
</array>
```

Add elements to info.plist (https://github.com/iZettle/sdk-ios)

```
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
```


## Installing

Add zettle to your pubspec.yaml:

```yaml
dependencies:
  zettle:
```

Import zettle:

```dart
import 'package:zettle/zettle.dart';
```

## Getting Started

Init Zettle SDK:

```dart
Zettle.init(iosClientId, androidClientId, redirectUrl);
```

Complete a transaction:

```dart
var request = ZettlePaymentRequest(
        amount: 100,
        reference: reference,
        enableLogin: true,
        enableTipping: false,
        enableInstalments: false);
        
Zettle.requestPayment(request);
```

## Available APIs

```dart
Zettle.init(iosClientId, androidClientId, redirectUrl);

Zettle.requestPayment(request);
Zettle.requestRefund(request);
```
