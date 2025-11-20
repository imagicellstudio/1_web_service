plugins {
    `java-library`
}

dependencies {
    // Spring Boot
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-validation")
    
    // Jackson
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // Apache Commons
    api("org.apache.commons:commons-lang3")
    api("org.apache.commons:commons-collections4:4.4")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

