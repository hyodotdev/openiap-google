plugins {
    id("com.android.library") version "8.5.0" apply false
    id("com.android.application") version "8.5.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.vanniktech.maven.publish") version "0.29.0" apply false
}

// openiap-versions.json에서 버전 읽기 (간단한 파싱)
import java.io.File

val versionsFile = File(rootDir, "openiap-versions.json")
val jsonText = versionsFile.readText()
val googleVersion = jsonText.substringAfter("\"google\": \"").substringBefore("\"")
val gqlVersion = jsonText.substringAfter("\"gql\": \"").substringBefore("\"")

// 버전을 프로젝트 속성으로 설정
extra["OPENIAP_VERSION"] = googleVersion
extra["GQL_VERSION"] = gqlVersion

// Configure Sonatype (OSSRH) publishing at the root
// Credentials are sourced from env or gradle.properties (OSSRH_USERNAME/OSSRH_PASSWORD)
// Maven Central publishing is configured per-module via Vanniktech plugin.

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

// Print Compose versions on project sync/build so it’s visible in IDE
val composeUiVersion = providers.gradleProperty("COMPOSE_UI_VERSION").orNull ?: "unknown"
val composeCompilerVersion = providers.gradleProperty("COMPOSE_COMPILER_VERSION").orNull ?: "unknown"
logger.lifecycle("Compose UI version: $composeUiVersion (compiler ext: $composeCompilerVersion)")
