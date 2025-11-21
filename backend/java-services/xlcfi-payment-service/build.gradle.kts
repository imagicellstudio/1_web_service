dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // HTTP Client for PG Integration
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // Common modules
    implementation(project(":xlcfi-common:common-core"))
    implementation(project(":xlcfi-common:common-data"))
    
    // Dependencies for entity relationships
    implementation(project(":xlcfi-order-service"))
    
    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    
    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // Stripe SDK
    implementation("com.stripe:stripe-java:24.3.0")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // Test
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}

// Disable Spring Boot bootJar for this module if it's a library
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}

