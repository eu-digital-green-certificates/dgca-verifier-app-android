<h1 align="center">
    EU Digital COVID Certificate Verifier App - Android
</h1>

<p align="center">
    <a href="/../../commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/ministero-salute/dgca-verifier-app-android?style=flat"></a>
    <a href="/../../issues" title="Open Issues"><img src="https://img.shields.io/github/issues/ministero-salute/dgca-verifier-app-android?style=flat"></a>
    <a href="./LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#about">About</a> •
  <a href="#development">Development</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#dependencies">Dependencies</a> •
  <a href="#support-and-feedback">Support</a> •
  <a href="#how-to-contribute">Contribute</a> •
  <a href="#contributors">Contributors</a> •
  <a href="#licensing">Licensing</a>
</p>

## About

This repository contains the source code of *VerificaC19*, the Italian customization of the EU Digital COVID Certificate Verifier App for the Android Operating System. The repository is forked from the [official EU Digital COVID Certificate Verifier App - Android](https://github.com/eu-digital-green-certificates/dgca-verifier-app-android)

The DGC Verifier Apps are responsible for scanning and verifying DCCs using public keys from national backend servers. Offline verification is supported, if the latest public keys are present in the app's key store. Consequently, once up-to-date keys have been downloaded, the verification works without active internet connection.
The Italian version adds some medical rules to the validation of the DCCs, defined by rules downloaded from national backend servers.

## Development

### Prerequisites

- For development, the latest version of Android Studio is required. The latest version can be downloaded from [here](https://developer.android.com/studio/).
- Android SDK version 26+

### Build

Whether you cloned or downloaded the 'zipped' sources you will either find the sources in the chosen checkout-directory or get a zip file with the source code, which you can expand to a folder of your choice.

In order to successfully build and run the project, you must have also downloaded the corresponding core repository from [here](https://github.com/eu-digital-green-certificates/dgca-app-core-android). Both projects should be at the same folder level as eachother which would look something like

```
android-app
|___dgca-verifier-app-android
|___dgca-app-core-android
```

#### Android Studio based build 

Modifiy `app\build.gradle` file, changing `BASE_URL`, `SERVER_HOST` and `CERTIFICATE_SHA` `debug` config values with `release` config values. [Here](https://github.com/ministero-salute/it-dgc-documentation/blob/master/openapi.yaml) you can find more info on app's endpoints.

This project uses the Gradle build system. To build this project, use the `gradlew build` command or use `"Run"` in Android Studio.

## Documentation  

- [High level documentation](https://github.com/ministero-salute/it-dgc-documentation)

## Dependencies

The following libraries are used in the project by the verifier app and the core app and are imported as Gradle dependencies:
- **[decoder](https://github.com/eu-digital-green-certificates/dgca-app-core-android).** European core library that contains business logic to decode data from QR code payload and performs technical validations (i.e. correct signature verification, signature expiration verification, correct payload format etc).
- **[zxing](https://github.com/zxing/zxing).** Library used QR code scanning.
- **[retrofit2](https://github.com/square/retrofit).** Library used for networking.
- **[gson](https://github.com/google/gson).** Library used by core module for JSON serialization/deserialization.
All listed dependencies are already included in the [official EU Digital COVID Certificate Verifier App for Android](https://github.com/eu-digital-green-certificates/dgca-verifier-app-android). No new dependencies have been included in the Italian customization of the app.

## Support and feedback

The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **Issues**    | <a href="/../../issues" title="Open Issues"><img src="https://img.shields.io/github/issues/ministero-salute/dgca-verifier-app-android?style=flat"></a>  |

## How to contribute  

Contribution and feedback is encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors  

Our commitment to open source means that we are enabling -in fact encouraging- all interested parties to contribute and become part of its developer community.

## Licensing

See the [NOTICE](./NOTICE) for all copyright and licensing details.

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.
