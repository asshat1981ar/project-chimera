buildscript {
    ext {
        compose_bom_version = '2023.10.01'
        kotlin_version = '1.9.10'
        hilt_version = '2.48'
    }
}

plugins {
    id 'com.android.application' version '8.1.2' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.10' apply false
    id 'com.google.dagger.hilt.android' version '2.48' apply false
}