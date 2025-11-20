dependencies {
    // Common modules
    implementation(project(":xlcfi-common:common-core"))
    implementation(project(":xlcfi-common:common-security"))
    implementation(project(":xlcfi-common:common-data"))
    
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Database
    runtimeOnly("org.postgresql:postgresql")
    
    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Hypersistence Utils (JSONB support)
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.6.1")
    
    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    
    // Elasticsearch
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    
    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // Test
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
}
