plugins {
    `java-library`
}

dependencies {
    // Common Core
    api(project(":xlcfi-common:common-core"))
    
    // Spring Data JPA
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // Database
    runtimeOnly("org.postgresql:postgresql")
    
    // Flyway
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-postgresql")
    
    // QueryDSL
    api("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

