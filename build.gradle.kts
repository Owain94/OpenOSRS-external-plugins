buildscript {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    checkstyle
    java
    kotlin("jvm") version "1.3.71"
    id("com.simonharrer.modernizer") version "2.1.0-1" apply false
}

apply<BootstrapPlugin>()
apply<VersionPlugin>()

allprojects {
    group = "com.owain"
    apply<MavenPublishPlugin>()
}

allprojects {
    apply<MavenPublishPlugin>()

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }
}

subprojects {
    group = "com.owain.externals"

    project.extra["PluginProvider"] = "Owain94"
    project.extra["ProjectUrl"] = "https://discord.gg/HVjnT6R"
    project.extra["PluginLicense"] = "3-Clause BSD License"

    repositories {
        mavenCentral {
            content {
                excludeGroupByRegex("com\\.openosrs.*")
            }
        }

        jcenter {
            content {
                excludeGroupByRegex("com\\.openosrs.*")
            }
        }

        exclusiveContent {
            forRepository {
                mavenLocal()
            }
            filter {
                includeGroupByRegex("com\\.openosrs.*")
            }
        }
    }

    apply<JavaPlugin>()
    apply(plugin = "checkstyle")
    apply(plugin = "kotlin")
    apply(plugin = "com.simonharrer.modernizer")

    dependencies {
        compileOnly(group = "com.openosrs", name = "http-api", version = "4.0.0")
        compileOnly(group = "com.openosrs", name = "runelite-api", version = "4.0.0")
        compileOnly(group = "com.openosrs", name = "runelite-client", version = "4.0.0")

        compileOnly(group = "org.jetbrains", name = "annotations", version = "20.1.0")
        compileOnly(group = "org.apache.commons", name = "commons-text", version = "1.2")
        compileOnly(group = "com.google.inject", name = "guice", version = "4.1.0", classifier = "no_aop")
        compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.4")
        compileOnly(group = "org.pf4j", name = "pf4j", version = "3.6.0")
        compileOnly(group = "io.reactivex.rxjava3", name = "rxjava", version = "3.0.10")
    }

    checkstyle {
        maxWarnings = 0
        toolVersion = "8.25"
        isShowViolations = true
        isIgnoreFailures = false
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                url = uri("$buildDir/repo")
            }
        }
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])
            }
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }

        compileKotlin {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs = listOf("-Xjvm-default=enable")
            }
            sourceCompatibility = "11"
        }

        withType<AbstractArchiveTask> {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
            dirMode = 493
            fileMode = 420
        }

        withType<Checkstyle> {
            group = "verification"
        }

        register<Copy>("copyDeps") {
            into("./build/deps/")
            from(configurations["runtimeClasspath"])
        }
    }
}
