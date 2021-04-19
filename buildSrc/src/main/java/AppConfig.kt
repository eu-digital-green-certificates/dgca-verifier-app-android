import org.gradle.api.JavaVersion

object Config {
    const val minSdk = 26
    const val compileSdk = 30
    const val targetSdk = 30
    val javaVersion = JavaVersion.VERSION_1_8

    const val versionCode = 1
    const val versionName = "1.0.0"
    const val buildToolsVersion = "30.0.3"

    const val androidTestInstrumentation = "androidx.test.runner.AndroidJUnitRunner"
    const val proguardConsumerRules = "consumer-rules.pro"
}