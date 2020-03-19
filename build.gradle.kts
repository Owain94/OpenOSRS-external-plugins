import ProjectVersions.openosrsVersion

buildscript {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    checkstyle
    java
    kotlin("jvm") version ProjectVersions.kotlin
}

apply<BootstrapPlugin>()
apply<VersionPlugin>()

allprojects {
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
}

subprojects {
    group = "com.owain.externals"

    project.extra["PluginProvider"] = "Owain94"
    project.extra["ProjectUrl"] = "https://discord.gg/HVjnT6R"
    project.extra["PluginLicense"] = "3-Clause BSD License"

    apply<JavaPlugin>()
    apply(plugin = "checkstyle")
    apply(plugin = "kotlin")

    dependencies {
        compileOnly("com.openosrs:http-api:$openosrsVersion+")
        compileOnly("com.openosrs:runelite-api:$openosrsVersion+")
        compileOnly("com.openosrs:runelite-client:$openosrsVersion+")

        compileOnly(Libraries.apacheCommonsText)
        compileOnly(Libraries.guice)
        compileOnly(Libraries.lombok)
        compileOnly(Libraries.pf4j)

        // kotlin
        compileOnly(kotlin("stdlib"))
    }

    checkstyle {
        maxWarnings = 0
        toolVersion = "8.25"
        isShowViolations = true
        isIgnoreFailures = false
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
    }
}
