package com.chimera.tools

/**
 * Represents the GitHub Actions CI pipeline configuration for the Android project.
 * This class provides a representation of the workflow that automates building and testing.
 */
class CICiPipeline {
    /**
     * Returns the YAML definition of the GitHub Actions workflow.
     */
    fun getWorkflowYaml(): String = """
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission for gradlew
        run: chmod +w gradlew

      - name: Build with Gradle
        run: ./gradlew build

      - name: Run tests
        run: ./gradlew testDebugUnitTest

      - name: Lint checks
        run: ./gradlew lint

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: test-reports
          path: app/build/reports/tests/testDebugUnitTest/
    """.trimIndent()
}

/**
 * Main entry point that prints the CI workflow YAML.
 */
fun main() {
    val pipeline = CICiPipeline()
    println(pipeline.getWorkflowYaml())
}
