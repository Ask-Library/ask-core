plugins {
	alias(libs.plugins.java.library)
	alias(libs.plugins.maven.publish)
	alias(libs.plugins.jacoco)
}

group = "pe.ask"
version = "1.0.1"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

jacoco {
	toolVersion = "0.8.12"
}

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

dependencies {
	api(platform(libs.spring.boot.dependencies))
	implementation(libs.commons.exception.core)
	implementation(libs.spring.beans)
	implementation(libs.project.reactor)

	compileOnly(libs.lombok)
	annotationProcessor(libs.lombok)

	testCompileOnly(libs.lombok)
	testAnnotationProcessor(libs.lombok)
	testRuntimeOnly(libs.junit.platform.launcher)
	testImplementation(libs.project.reactor.test)
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			pom {
				name.set("Ask Core")
				description.set("Core library for reactive microservices")
				url.set("https://github.com/Ask-Library/ask-core")

				licenses {
					license {
						name.set("The Apache License, Version 2.0")
						url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
					}
				}

				developers {
					developer {
						id.set("AllanSagastegui")
						name.set("Allan Sagastegui")
						email.set("allxn.sxh@gmail.com")
					}
				}
			}
		}
	}
}

tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)

	reports {
		xml.required.set(true)
		html.required.set(true)
	}
}
