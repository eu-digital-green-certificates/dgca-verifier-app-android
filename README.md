<h1 align="center">
    EU Digital COVID Certificate Verifier App - Android
</h1>

<p align="center">
    <a href="/../../commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/eu-digital-green-certificates/dgca-verifier-app-android?style=flat"></a>
    <a href="/../../issues" title="Open Issues"><img src="https://img.shields.io/github/issues/eu-digital-green-certificates/dgca-verifier-app-android?style=flat"></a>
    <a href="./LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#about">About</a> •
  <a href="#development">Development</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#support-and-feedback">Support</a> •
  <a href="#how-to-contribute">Contribute</a> •
  <a href="#contributors">Contributors</a> •
  <a href="#licensing">Licensing</a>
</p>

## About

This repository contains the source code of the EU Digital COVID Certificate Verifier App for Android.

The DGC Verifier Apps are responsible for scanning and verifying DGCs using public keys from national backend servers. Offline verification is supported, if the latest public keys are present in the app's key store. Consequently, once up-to-date keys have been downloaded, the verification works without active internet connection. 

**A note on using the apps and released APK files found in this GitHub organization**: The apps are reference implementations that cannot be used in production environments as-is, but rather need to be configured by EU member states to access their national backends. The released APK files are configured to work with the test environments and will not report correct results on "live" DCCs.

## Development

### Prerequisites

- For development, the latest version of Android Studio is required. The latest version can be downloaded from [here](https://developer.android.com/studio/).
- Android SDK version 26+

### Build

Whether you cloned or downloaded the 'zipped' sources you will either find the sources in the chosen checkout-directory or get a zip file with the source code, which you can expand to a folder of your choice.

In order to successfully build and run the project, you must have also downloaded the corresponding core repository from [here](https://github.com/eu-digital-green-certificates/dgca-app-core-android) and the certlogic repository from [here](https://github.com/eu-digital-green-certificates/dgc-certlogic-android). All projects should be at the same folder level which would look something like

```
android-app
|___dgca-verifier-app-android
|___dgca-app-core-android
|___dgc-certlogic-android
```

- To build project - it's required to add config json file to application assets folder. Structure of the file should be similar to:
'app/src/acc/assets/verifier-context.jsonc' or 'app/src/tst/assets/verifier-context.jsonc', depending on chosen flavor.
After related file has been added - it's name should be passed via gradle properties:
gradlew -PCONFIG_FILE_NAME="config.json"

#### Android Studio based build

This project uses the Gradle build system. To build this project, use the `gradlew build` command or use "Run" in Android Studio.

## Documentation  

- [App architecture](/docs/architecture.md)
- [DCC Revocation](/docs/revocation.md)

## Support and feedback

The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **Issues**    | <a href="/../../issues" title="Open Issues"><img src="https://img.shields.io/github/issues/eu-digital-green-certificates/dgca-verifier-app-android?style=flat"></a>  |
| **Other requests**    | <a href="mailto:opensource@telekom.de" title="Email DGC Team"><img src="https://img.shields.io/badge/email-DGC%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## How to contribute  

Contribution and feedback is encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors  

Our commitment to open source means that we are enabling -in fact encouraging- all interested parties to contribute and become part of its developer community.

## Licensing

Copyright (C) 2021 T-Systems International GmbH and all other contributors

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.
