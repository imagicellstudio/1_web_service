dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // AOP
    implementation("org.springframework.boot:spring-boot-starter-aop")
    
    // Security (for annotations)
    implementation("org.springframework.security:spring-security-core")
    
    // Redis (for Rate Limiting)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Swagger/OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}

// Disable Spring Boot bootJar for library module
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}
