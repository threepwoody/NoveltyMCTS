plugins {
	application
}

sourceSets {
	main {
		java {
			setSrcDirs(listOf("src"))
		}
	}
}

application {
	mainClass.set("experiments.Experiment")
//	mainClass.set("experiments.BanditTuning.BanditTuningExperiment")
//	mainClass.set("experiments.NTBO.NTBOExperiment")
}

repositories {
	mavenCentral()
	maven {
		url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
	}
}

dependencies {
	implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.12.1")
	implementation("ai.djl:api:0.31.1")
	implementation("org.apache.commons:commons-lang3:3.6")
	implementation("commons-cli:commons-cli:1.5.0")
	implementation("tech.tablesaw:tablesaw-jsplot:0.43.1")
	implementation("junit:junit:4.13.1")
	implementation("com.edwardraff:JSAT:0.0.9")
	implementation("ca.umontreal.iro.simul:ssj:3.3.1")
	implementation("colt:colt:1.2.0")
	implementation("io.pinecone:pinecone-client:0.2.2")
	implementation(files("$projectDir/lib/org.tweetyproject.tweety-full-1.25-with-dependencies.jar"))
	implementation ("org.tribuo:tribuo-all:4.3.1@pom") {
		//transitive = true      // for build.gradle (Groovy)
		isTransitive = true // for build.gradle.kts (Kotlin)
	}

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")

	runtimeOnly("ai.djl.pytorch:pytorch-engine:0.31.1")
	runtimeOnly("ai.djl.pytorch:pytorch-jni:2.5.1-0.31.1")
//	runtimeOnly("ai.djl.pytorch:pytorch-native-cpu:2.1.1:linux-x86_64") //linux
//	runtimeOnly("ai.djl.pytorch:pytorch-native-cpu:2.1.1:win-x86_64") //windows
	runtimeOnly("ai.djl.pytorch:pytorch-native-cu121:2.1.1:linux-x86_64")
}


tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
}

tasks.withType<Javadoc>{
	options.encoding = "UTF-8"
}