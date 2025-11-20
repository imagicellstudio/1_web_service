rootProject.name = "xlcfi-platform"

// 공통 모듈
include("xlcfi-common:common-core")
include("xlcfi-common:common-security")
include("xlcfi-common:common-data")

// 마이크로서비스
include("xlcfi-api-gateway")
include("xlcfi-auth-service")
include("xlcfi-product-service")
include("xlcfi-order-service")
include("xlcfi-payment-service")
include("xlcfi-review-service")
include("xlcfi-admin-service")
include("xlcfi-blockchain-service")

// 배치
include("xlcfi-batch")

