# 고급 평가 및 게시판 시스템 설계

## 📋 개요

**목적:** 전략적 라벨링, 시각적 평가, 지수 기반 분석 시스템  
**작성일:** 2025-11-20  
**범위:** 사용자 라벨링, 평가 시스템, 게시판, 지수 분석, GIS 연동

---

## 1. 전략적 라벨링 시스템

### 1.1 사용자 분류 체계

#### 레벨 1: 역할 기반 분류

```
사용자 역할 분류
├─ 생산자 (Producer)
│  ├─ 농부 (Farmer)
│  ├─ 가공업자 (Processor)
│  ├─ 제조사 (Manufacturer)
│  └─ 수입업자 (Importer)
│
├─ 판매자 (Seller)
│  ├─ 도매상 (Wholesaler)
│  ├─ 소매상 (Retailer)
│  ├─ 온라인 판매자 (Online Seller)
│  └─ 마켓 운영자 (Market Operator)
│
├─ 소비자 (Consumer)
│  ├─ 일반 소비자 (General Consumer)
│  ├─ 프리미엄 소비자 (Premium Consumer)
│  ├─ 대량 구매자 (Bulk Buyer)
│  └─ 기업 구매자 (Corporate Buyer)
│
├─ 크리에이터 (Creator)
│  ├─ 셰프 (Chef)
│  ├─ 푸드 블로거 (Food Blogger)
│  ├─ 영양사 (Nutritionist)
│  └─ 요리 강사 (Cooking Instructor)
│
└─ 전문가 (Expert)
   ├─ 식품 전문가 (Food Expert)
   ├─ 품질 검증자 (Quality Verifier)
   ├─ 컨설턴트 (Consultant)
   └─ 연구원 (Researcher)
```

#### 레벨 2: 활동 기반 라벨링

**활동 지수 계산:**

```typescript
interface ActivityMetrics {
  // 거래 활동
  totalPurchases: number; // 총 구매 횟수
  totalSales: number; // 총 판매 횟수
  totalTransactionValue: number; // 총 거래액

  // 콘텐츠 활동
  postsCreated: number; // 게시글 작성 수
  recipesCreated: number; // 레시피 작성 수
  reviewsWritten: number; // 리뷰 작성 수
  commentsWritten: number; // 댓글 작성 수

  // 참여 활동
  likesGiven: number; // 좋아요 준 수
  likesReceived: number; // 좋아요 받은 수
  sharesGiven: number; // 공유한 수
  sharesReceived: number; // 공유받은 수

  // 품질 지표
  averageRating: number; // 평균 평점
  reportCount: number; // 신고 받은 수
  verifiedContent: number; // 검증된 콘텐츠 수

  // 시간 지표
  accountAge: number; // 계정 나이 (일)
  lastActiveDate: Date; // 마지막 활동일
  loginStreak: number; // 연속 로그인 일수
}

// 활동 지수 계산 (0-1000점)
function calculateActivityScore(metrics: ActivityMetrics): number {
  const weights = {
    transaction: 0.25, // 거래 활동 25%
    content: 0.3, // 콘텐츠 활동 30%
    engagement: 0.2, // 참여 활동 20%
    quality: 0.15, // 품질 지표 15%
    consistency: 0.1, // 일관성 10%
  };

  // 거래 점수 (0-250)
  const transactionScore = Math.min(
    250,
    metrics.totalPurchases * 2 +
      metrics.totalSales * 3 +
      metrics.totalTransactionValue / 10000
  );

  // 콘텐츠 점수 (0-300)
  const contentScore = Math.min(
    300,
    metrics.postsCreated * 5 +
      metrics.recipesCreated * 10 +
      metrics.reviewsWritten * 3 +
      metrics.commentsWritten * 1
  );

  // 참여 점수 (0-200)
  const engagementScore = Math.min(
    200,
    metrics.likesGiven * 0.5 +
      metrics.likesReceived * 2 +
      metrics.sharesGiven * 1 +
      metrics.sharesReceived * 3
  );

  // 품질 점수 (0-150)
  const qualityScore = Math.min(
    150,
    metrics.averageRating * 30 +
      metrics.verifiedContent * 5 -
      metrics.reportCount * 10
  );

  // 일관성 점수 (0-100)
  const consistencyScore = Math.min(
    100,
    metrics.loginStreak * 2 + Math.min(365, metrics.accountAge) / 3.65
  );

  return (
    transactionScore +
    contentScore +
    engagementScore +
    qualityScore +
    consistencyScore
  );
}
```

#### 레벨 3: 활동 등급 라벨

**등급 체계:**

```typescript
enum ActivityTier {
  NEWCOMER = "NEWCOMER", // 신규 (0-100점)
  BEGINNER = "BEGINNER", // 초보 (101-200점)
  ACTIVE = "ACTIVE", // 활동적 (201-400점)
  ENGAGED = "ENGAGED", // 참여적 (401-600점)
  INFLUENTIAL = "INFLUENTIAL", // 영향력 있는 (601-800점)
  EXPERT = "EXPERT", // 전문가 (801-900점)
  MASTER = "MASTER", // 마스터 (901-1000점)
}

// 라벨 아이콘 및 색상
const tierConfig = {
  NEWCOMER: {
    icon: "🌱",
    color: "#A8E6CF",
    badge: "새싹",
    benefits: ["기본 기능 사용"],
  },
  BEGINNER: {
    icon: "🌿",
    color: "#7FCDBB",
    badge: "초보",
    benefits: ["댓글 작성", "좋아요"],
  },
  ACTIVE: {
    icon: "🍀",
    color: "#41B6C4",
    badge: "활동가",
    benefits: ["게시글 작성", "리뷰 작성", "5% 할인"],
  },
  ENGAGED: {
    icon: "🌳",
    color: "##2C7FB8",
    badge: "참여자",
    benefits: ["레시피 NFT 발행", "10% 할인", "우선 알림"],
  },
  INFLUENTIAL: {
    icon: "⭐",
    color: "#FFD700",
    badge: "인플루언서",
    benefits: ["검증자 신청 가능", "15% 할인", "프리미엄 배지"],
  },
  EXPERT: {
    icon: "💎",
    color: "#9B59B6",
    badge: "전문가",
    benefits: ["검증자 권한", "20% 할인", "전용 채널"],
  },
  MASTER: {
    icon: "👑",
    color: "#E74C3C",
    badge: "마스터",
    benefits: ["모든 권한", "30% 할인", "VIP 지원", "수익 공유"],
  },
};
```

#### 레벨 4: 전문 분야 라벨

**전문성 라벨:**

```typescript
interface ExpertiseLabel {
  category: string; // 카테고리
  level: number; // 전문성 레벨 (1-5)
  verifiedBy: string; // 검증자
  earnedDate: Date; // 획득일
}

// 전문 분야 카테고리
const expertiseCategories = {
  // 식품 카테고리
  KOREAN_FOOD: "한식 전문가",
  CHINESE_FOOD: "중식 전문가",
  WESTERN_FOOD: "양식 전문가",
  JAPANESE_FOOD: "일식 전문가",
  FUSION_FOOD: "퓨전 전문가",

  // 재료 전문성
  VEGETABLES: "채소 전문가",
  MEAT: "육류 전문가",
  SEAFOOD: "해산물 전문가",
  GRAINS: "곡물 전문가",
  DAIRY: "유제품 전문가",

  // 조리 기술
  BAKING: "베이킹 전문가",
  GRILLING: "그릴 전문가",
  FERMENTATION: "발효 전문가",
  PRESERVATION: "보존 전문가",

  // 품질 관리
  ORGANIC: "유기농 전문가",
  HACCP: "HACCP 전문가",
  QUALITY_CONTROL: "품질 관리 전문가",
  FOOD_SAFETY: "식품 안전 전문가",

  // 비즈니스
  SALES: "판매 전문가",
  MARKETING: "마케팅 전문가",
  DISTRIBUTION: "유통 전문가",
  EXPORT: "수출 전문가",
};
```

---

## 2. 시각적 평가 시스템

### 2.1 진화된 감정/의도 표현 체계

#### 기존 방식 (단순 선호도)

```
❌ 구식: 매우좋아요 → 좋아요 → 보통 → 별로 → 나빠요
```

#### 새로운 방식 (다차원 의도 표현)

**카테고리 1: 비즈니스 잠재력**

```typescript
enum BusinessPotential {
  HIGH_SALES_POTENTIAL = "HIGH_SALES_POTENTIAL", // 🚀 판매가능성 높아요
  HIGH_GROWTH_POTENTIAL = "HIGH_GROWTH_POTENTIAL", // 📈 성장가능성 높아요
  WANT_TO_TRADE = "WANT_TO_TRADE", // 🤝 거래하고 싶어요
  INVESTMENT_WORTHY = "INVESTMENT_WORTHY", // 💰 투자가치 있어요
  SCALABLE = "SCALABLE", // 📊 확장가능해요
  EXPORT_POTENTIAL = "EXPORT_POTENTIAL", // 🌍 수출가능해요
}
```

**카테고리 2: 정보 요구**

```typescript
enum InformationNeed {
  WANT_MORE_INFO = "WANT_MORE_INFO", // 📋 자세한 정보 알고싶어요
  WANT_SOURCE = "WANT_SOURCE", // 🔍 출처를 알고 싶어요
  WANT_RECIPE = "WANT_RECIPE", // 👨‍🍳 레시피 알고싶어요
  WANT_NUTRITION = "WANT_NUTRITION", // 🥗 영양정보 알고싶어요
  WANT_CERTIFICATION = "WANT_CERTIFICATION", // ✅ 인증정보 알고싶어요
  WANT_PRICE = "WANT_PRICE", // 💵 가격정보 알고싶어요
  WANT_AVAILABILITY = "WANT_AVAILABILITY", // 📦 구매처 알고싶어요
}
```

**카테고리 3: 감정 및 지지**

```typescript
enum EmotionalSupport {
  SUPPORT = "SUPPORT", // 💪 응원해요
  LOVE_IT = "LOVE_IT", // ❤️ 정말 좋아요
  INSPIRING = "INSPIRING", // ✨ 영감을 받았어요
  TRUST = "TRUST", // 🤗 믿을 수 있어요
  RECOMMEND = "RECOMMEND", // 👍 추천해요
  WANT_TO_TRY = "WANT_TO_TRY", // 🍽️ 먹어보고 싶어요
}
```

**카테고리 4: 품질 평가**

```typescript
enum QualityAssessment {
  HIGH_QUALITY = "HIGH_QUALITY", // ⭐ 품질 우수해요
  FRESH = "FRESH", // 🌿 신선해요
  AUTHENTIC = "AUTHENTIC", // 🏆 정통이에요
  INNOVATIVE = "INNOVATIVE", // 💡 혁신적이에요
  GOOD_VALUE = "GOOD_VALUE", // 💎 가성비 좋아요
  ECO_FRIENDLY = "ECO_FRIENDLY", // 🌱 친환경이에요
}
```

**카테고리 5: 우려 사항**

```typescript
enum Concern {
  NEED_IMPROVEMENT = "NEED_IMPROVEMENT", // 🔧 개선 필요해요
  PRICE_HIGH = "PRICE_HIGH", // 💸 가격이 비싸요
  AVAILABILITY_ISSUE = "AVAILABILITY_ISSUE", // ⚠️ 구하기 어려워요
  QUALITY_CONCERN = "QUALITY_CONCERN", // 🤔 품질 확인 필요해요
  SAFETY_CONCERN = "SAFETY_CONCERN", // 🚨 안전성 우려돼요
  AUTHENTICITY_DOUBT = "AUTHENTICITY_DOUBT", // ❓ 진위 의심돼요
}
```

### 2.2 시각적 평가 UI 구조

```typescript
interface VisualReaction {
  id: string;
  userId: number;
  targetType: "POST" | "COMMENT" | "PRODUCT" | "RECIPE";
  targetId: number;

  // 다차원 평가 (여러 개 선택 가능)
  reactions: {
    businessPotential?: BusinessPotential[];
    informationNeed?: InformationNeed[];
    emotionalSupport?: EmotionalSupport[];
    qualityAssessment?: QualityAssessment[];
    concern?: Concern[];
  };

  // 강도 (선택 사항)
  intensity?: number; // 1-5

  createdAt: Date;
}

// UI 표시 예시
const reactionDisplay = {
  // 게시글에 대한 반응 집계
  businessPotential: {
    HIGH_SALES_POTENTIAL: { count: 45, icon: "🚀", label: "판매가능성 높아요" },
    WANT_TO_TRADE: { count: 32, icon: "🤝", label: "거래하고 싶어요" },
  },
  informationNeed: {
    WANT_MORE_INFO: { count: 67, icon: "📋", label: "자세한 정보 알고싶어요" },
    WANT_SOURCE: { count: 23, icon: "🔍", label: "출처를 알고 싶어요" },
  },
  emotionalSupport: {
    SUPPORT: { count: 89, icon: "💪", label: "응원해요" },
    LOVE_IT: { count: 156, icon: "❤️", label: "정말 좋아요" },
  },
};
```

---

## 3. 댓글 관리 시스템

### 3.1 댓글 제한 설정

```typescript
interface CommentSettings {
  // 게시글별 설정
  postId: number;

  // 댓글 허용 설정
  commentsEnabled: boolean; // 댓글 허용 여부
  requireApproval: boolean; // 승인 필요 여부
  allowAnonymous: boolean; // 익명 댓글 허용

  // 사용자 제한
  minActivityScore: number; // 최소 활동 점수
  minAccountAge: number; // 최소 계정 나이 (일)
  allowedRoles: UserRole[]; // 허용된 역할
  allowedTiers: ActivityTier[]; // 허용된 등급

  // 내용 제한
  minLength: number; // 최소 글자 수
  maxLength: number; // 최대 글자 수
  allowLinks: boolean; // 링크 허용
  allowImages: boolean; // 이미지 허용
  allowMentions: boolean; // 멘션 허용

  // 시간 제한
  cooldownPeriod: number; // 댓글 간 대기 시간 (초)
  maxCommentsPerDay: number; // 일일 댓글 수 제한

  // 자동 필터링
  profanityFilter: boolean; // 욕설 필터
  spamFilter: boolean; // 스팸 필터
  aiModeration: boolean; // AI 검토
}
```

### 3.2 비난 vs 비판 구분 시스템

```typescript
interface CommentAnalysis {
  commentId: number;
  content: string;

  // AI 분석 결과
  analysis: {
    type: "CONSTRUCTIVE" | "CRITICISM" | "ABUSE" | "SPAM";
    confidence: number; // 0-1

    // 세부 분석
    sentiment: {
      score: number; // -1 (부정) ~ 1 (긍정)
      magnitude: number; // 0-1 (강도)
    };

    // 비판 vs 비난 구분
    isConstructive: boolean;
    hasSpecificFeedback: boolean;
    hasSolutions: boolean;
    isPersonalAttack: boolean;
    hasAbusiveLanguage: boolean;

    // 카테고리 분류
    categories: {
      PRODUCT_QUALITY: number;
      PRICE: number;
      SERVICE: number;
      DELIVERY: number;
      COMMUNICATION: number;
      PERSONAL_ATTACK: number;
    };
  };

  // 자동 조치
  action: "APPROVE" | "REVIEW" | "HIDE" | "BLOCK";
  reason: string;
}

// 비판 vs 비난 판단 로직
function analyzeComment(content: string): CommentAnalysis {
  // 건설적 비판의 특징
  const constructiveIndicators = [
    "개선",
    "제안",
    "~하면 좋겠",
    "~했으면",
    "대신",
    "차라리",
    "방법",
    "해결",
  ];

  // 비난의 특징
  const abusiveIndicators = [
    "멍청",
    "바보",
    "쓰레기",
    "최악",
    "사기",
    "거짓말",
    "인격",
    "모욕",
  ];

  // AI 모델 호출 (예: OpenAI Moderation API)
  const aiResult = callModerationAPI(content);

  // 종합 판단
  if (aiResult.isAbusive || hasPersonalAttack(content)) {
    return {
      type: "ABUSE",
      action: "HIDE",
      reason: "비난성 댓글로 판단됨",
    };
  } else if (hasConstructiveFeedback(content)) {
    return {
      type: "CRITICISM",
      action: "APPROVE",
      reason: "건설적 비판으로 판단됨",
    };
  }

  // ... 추가 로직
}
```

### 3.3 댓글 관리 UI (관리자)

```typescript
interface AdminCommentManagement {
  // 필터링
  filters: {
    status: "ALL" | "PENDING" | "APPROVED" | "HIDDEN" | "REPORTED";
    type: "ALL" | "CONSTRUCTIVE" | "CRITICISM" | "ABUSE" | "SPAM";
    dateRange: { from: Date; to: Date };
    userId?: number;
    postId?: number;
  };

  // 일괄 작업
  bulkActions: {
    approve: (commentIds: number[]) => void;
    hide: (commentIds: number[]) => void;
    delete: (commentIds: number[]) => void;
    ban: (userIds: number[], duration: number) => void;
  };

  // 통계
  statistics: {
    totalComments: number;
    pendingReview: number;
    hiddenComments: number;
    reportedComments: number;
    averageSentiment: number;
    abusiveRate: number;
  };
}
```

---

## 4. 판매가능지수 (Sales Potential Index)

### 4.1 판매가능지수 계산 알고리즘

```typescript
interface SalesPotentialMetrics {
  // 상품 특성
  product: {
    category: string;
    isOrganic: boolean;
    hasHACCP: boolean;
    hasCertification: boolean;
    originVerified: boolean;
    uniqueness: number; // 1-10 (독창성)
    seasonality: number; // 1-10 (계절성)
  };

  // 시장 데이터
  market: {
    demandTrend: number; // -100 ~ 100 (수요 추세)
    competitorCount: number;
    averagePrice: number;
    priceCompetitiveness: number; // 0-10
    marketSize: number; // 시장 규모 (원)
  };

  // 판매자 신뢰도
  seller: {
    activityScore: number; // 0-1000
    averageRating: number; // 0-5
    totalSales: number;
    returnRate: number; // 0-100%
    responseTime: number; // 시간
    verificationLevel: number; // 1-5
  };

  // 상품 성과
  performance: {
    viewCount: number;
    likeCount: number;
    shareCount: number;
    inquiryCount: number;
    cartAddCount: number;
    purchaseCount: number;
    conversionRate: number; // 0-100%
  };

  // 콘텐츠 품질
  content: {
    hasImages: boolean;
    imageQuality: number; // 0-10
    hasVideo: boolean;
    descriptionLength: number;
    hasNFT: boolean;
    hasRecipe: boolean;
  };
}

function calculateSalesPotentialIndex(metrics: SalesPotentialMetrics): number {
  const weights = {
    product: 0.25,
    market: 0.2,
    seller: 0.2,
    performance: 0.25,
    content: 0.1,
  };

  // 1. 상품 점수 (0-100)
  const productScore =
    (metrics.product.isOrganic ? 15 : 0) +
    (metrics.product.hasHACCP ? 15 : 0) +
    (metrics.product.hasCertification ? 10 : 0) +
    (metrics.product.originVerified ? 10 : 0) +
    metrics.product.uniqueness * 3 +
    metrics.product.seasonality * 2;

  // 2. 시장 점수 (0-100)
  const marketScore =
    ((metrics.market.demandTrend + 100) / 2) * 0.3 +
    Math.max(0, 100 - metrics.market.competitorCount * 2) * 0.2 +
    metrics.market.priceCompetitiveness * 10 * 0.3 +
    Math.min(100, metrics.market.marketSize / 10000000) * 0.2;

  // 3. 판매자 점수 (0-100)
  const sellerScore =
    (metrics.seller.activityScore / 10) * 0.3 +
    metrics.seller.averageRating * 20 * 0.3 +
    Math.min(100, metrics.seller.totalSales / 10) * 0.2 +
    Math.max(0, 100 - metrics.seller.returnRate) * 0.1 +
    metrics.seller.verificationLevel * 20 * 0.1;

  // 4. 성과 점수 (0-100)
  const performanceScore =
    Math.min(100, metrics.performance.viewCount / 100) * 0.15 +
    Math.min(100, metrics.performance.likeCount / 10) * 0.15 +
    Math.min(100, metrics.performance.inquiryCount / 5) * 0.2 +
    Math.min(100, metrics.performance.cartAddCount / 3) * 0.2 +
    metrics.performance.conversionRate * 0.3;

  // 5. 콘텐츠 점수 (0-100)
  const contentScore =
    (metrics.content.hasImages ? 20 : 0) +
    metrics.content.imageQuality * 5 +
    (metrics.content.hasVideo ? 20 : 0) +
    Math.min(30, metrics.content.descriptionLength / 10) +
    (metrics.content.hasNFT ? 10 : 0) +
    (metrics.content.hasRecipe ? 10 : 0);

  // 가중 평균
  const finalScore =
    productScore * weights.product +
    marketScore * weights.market +
    sellerScore * weights.seller +
    performanceScore * weights.performance +
    contentScore * weights.content;

  return Math.round(Math.min(100, Math.max(0, finalScore)));
}
```

### 4.2 판매가능지수 등급

```typescript
enum SalesPotentialGrade {
  EXCELLENT = "EXCELLENT", // 90-100% (🔥 매우 높음)
  VERY_GOOD = "VERY_GOOD", // 80-89%  (⭐ 높음)
  GOOD = "GOOD", // 70-79%  (👍 좋음)
  FAIR = "FAIR", // 60-69%  (✅ 보통)
  MODERATE = "MODERATE", // 50-59%  (⚠️ 중간)
  LOW = "LOW", // 40-49%  (📉 낮음)
  VERY_LOW = "VERY_LOW", // 0-39%   (❌ 매우 낮음)
}

const gradeConfig = {
  EXCELLENT: {
    icon: "🔥",
    color: "#E74C3C",
    label: "매우 높음",
    description: "판매 성공 가능성이 매우 높습니다",
    recommendations: [
      "적극적인 마케팅 추천",
      "프리미엄 가격 책정 가능",
      "대량 생산 고려",
    ],
  },
  VERY_GOOD: {
    icon: "⭐",
    color: "#F39C12",
    label: "높음",
    description: "판매 성공 가능성이 높습니다",
    recommendations: [
      "마케팅 투자 권장",
      "재고 확보 필요",
      "프로모션 진행 추천",
    ],
  },
  // ... 나머지 등급
};
```

---

## 5. 소비가능지수 (Consumption Potential Index)

### 5.1 소비가능지수 계산

```typescript
interface ConsumptionPotentialMetrics {
  // 소비자 수요
  demand: {
    searchVolume: number; // 검색량
    wishlistCount: number; // 위시리스트 추가 수
    inquiryCount: number; // 문의 수
    viewToCartRate: number; // 장바구니 전환율
    repeatPurchaseRate: number; // 재구매율
  };

  // 접근성
  accessibility: {
    availabilityScore: number; // 구매 가능성 (0-10)
    deliverySpeed: number; // 배송 속도 (일)
    deliveryCoverage: number; // 배송 커버리지 (%)
    priceAffordability: number; // 가격 적정성 (0-10)
    easeOfPurchase: number; // 구매 편의성 (0-10)
  };

  // 소비 트렌드
  trend: {
    seasonalDemand: number; // 계절 수요 (0-10)
    trendingScore: number; // 트렌드 점수 (0-10)
    socialMediaMentions: number; // SNS 언급 수
    influencerEndorsement: boolean; // 인플루언서 추천
    mediaExposure: number; // 미디어 노출 (0-10)
  };

  // 소비자 만족도
  satisfaction: {
    averageRating: number; // 평균 평점 (0-5)
    reviewCount: number; // 리뷰 수
    positiveReviewRate: number; // 긍정 리뷰 비율 (%)
    recommendationRate: number; // 추천 비율 (%)
    complaintRate: number; // 불만 비율 (%)
  };

  // 건강/안전성
  healthSafety: {
    nutritionScore: number; // 영양 점수 (0-10)
    allergenInfo: boolean; // 알레르기 정보
    safetyRating: number; // 안전성 평가 (0-10)
    organicCertified: boolean; // 유기농 인증
    haccpCertified: boolean; // HACCP 인증
  };
}

function calculateConsumptionPotentialIndex(
  metrics: ConsumptionPotentialMetrics
): number {
  const weights = {
    demand: 0.3,
    accessibility: 0.25,
    trend: 0.2,
    satisfaction: 0.15,
    healthSafety: 0.1,
  };

  // 1. 수요 점수 (0-100)
  const demandScore =
    Math.min(100, metrics.demand.searchVolume / 100) * 0.25 +
    Math.min(100, metrics.demand.wishlistCount / 50) * 0.2 +
    Math.min(100, metrics.demand.inquiryCount / 20) * 0.15 +
    metrics.demand.viewToCartRate * 0.2 +
    metrics.demand.repeatPurchaseRate * 0.2;

  // 2. 접근성 점수 (0-100)
  const accessibilityScore =
    metrics.accessibility.availabilityScore * 10 * 0.25 +
    Math.max(0, 100 - metrics.accessibility.deliverySpeed * 10) * 0.2 +
    metrics.accessibility.deliveryCoverage * 0.2 +
    metrics.accessibility.priceAffordability * 10 * 0.2 +
    metrics.accessibility.easeOfPurchase * 10 * 0.15;

  // 3. 트렌드 점수 (0-100)
  const trendScore =
    metrics.trend.seasonalDemand * 10 * 0.2 +
    metrics.trend.trendingScore * 10 * 0.25 +
    Math.min(100, metrics.trend.socialMediaMentions / 50) * 0.2 +
    (metrics.trend.influencerEndorsement ? 20 : 0) * 0.15 +
    metrics.trend.mediaExposure * 10 * 0.2;

  // 4. 만족도 점수 (0-100)
  const satisfactionScore =
    metrics.satisfaction.averageRating * 20 * 0.3 +
    Math.min(100, metrics.satisfaction.reviewCount / 10) * 0.2 +
    metrics.satisfaction.positiveReviewRate * 0.25 +
    metrics.satisfaction.recommendationRate * 0.15 +
    Math.max(0, 100 - metrics.satisfaction.complaintRate * 2) * 0.1;

  // 5. 건강/안전 점수 (0-100)
  const healthSafetyScore =
    metrics.healthSafety.nutritionScore * 10 * 0.3 +
    (metrics.healthSafety.allergenInfo ? 15 : 0) +
    metrics.healthSafety.safetyRating * 10 * 0.25 +
    (metrics.healthSafety.organicCertified ? 15 : 0) +
    (metrics.healthSafety.haccpCertified ? 15 : 0);

  // 가중 평균
  const finalScore =
    demandScore * weights.demand +
    accessibilityScore * weights.accessibility +
    trendScore * weights.trend +
    satisfactionScore * weights.satisfaction +
    healthSafetyScore * weights.healthSafety;

  return Math.round(Math.min(100, Math.max(0, finalScore)));
}
```

---

## 6. 수익창출효과 (Revenue Generation Effect)

### 6.1 수익창출 항목

```typescript
interface RevenueGenerationMetrics {
  // 직접 수익
  directRevenue: {
    productSales: number; // 상품 판매 수익
    serviceFees: number; // 서비스 수수료
    premiumMembership: number; // 프리미엄 멤버십
    nftSales: number; // NFT 판매 수익
    advertisingRevenue: number; // 광고 수익
  };

  // 간접 수익
  indirectRevenue: {
    referralBonus: number; // 추천 보너스
    affiliateCommission: number; // 제휴 수수료
    dataLicensing: number; // 데이터 라이선싱
    brandPartnerships: number; // 브랜드 파트너십
  };

  // 미래 가치
  futureValue: {
    customerLifetimeValue: number; // 고객 생애 가치
    networkEffect: number; // 네트워크 효과
    brandValue: number; // 브랜드 가치
    intellectualProperty: number; // 지적 재산권
  };

  // 비용 절감
  costSavings: {
    marketingEfficiency: number; // 마케팅 효율성
    operationalEfficiency: number; // 운영 효율성
    automationSavings: number; // 자동화 절감
  };
}

interface RevenueGenerationEffect {
  // 총 수익 효과
  totalEffect: number;

  // 항목별 기여도
  breakdown: {
    directRevenue: { amount: number; percentage: number };
    indirectRevenue: { amount: number; percentage: number };
    futureValue: { amount: number; percentage: number };
    costSavings: { amount: number; percentage: number };
  };

  // 성장 예측
  projection: {
    monthly: number;
    quarterly: number;
    yearly: number;
    growthRate: number; // %
  };

  // 등급
  grade: "EXCELLENT" | "GOOD" | "MODERATE" | "LOW";

  // 추천 사항
  recommendations: string[];
}
```

### 6.2 수익창출 효과 표시

```typescript
// UI 표시 컴포넌트
const RevenueEffectDisplay = {
  // 간략 표시 (게시글 목록)
  compact: {
    icon: "💰",
    score: 85, // 0-100
    label: "수익창출 효과: 높음",
    color: "#27AE60",
  },

  // 상세 표시 (게시글 상세)
  detailed: {
    totalEffect: "월 250만원 예상",
    breakdown: [
      { label: "직접 수익", value: "180만원", percentage: 72 },
      { label: "간접 수익", value: "40만원", percentage: 16 },
      { label: "미래 가치", value: "20만원", percentage: 8 },
      { label: "비용 절감", value: "10만원", percentage: 4 },
    ],
    projection: {
      monthly: 2500000,
      quarterly: 7800000, // +4% 성장
      yearly: 32500000, // +8% 성장
    },
    recommendations: [
      "프리미엄 멤버십 가입 시 수익 20% 증가 예상",
      "NFT 발행으로 추가 수익 창출 가능",
      "제휴 마케팅 활용 권장",
    ],
  },
};
```

---

## 7. 소비자연결지수 + GIS 시스템

### 7.1 소비자연결지수 계산

```typescript
interface ConsumerConnectionMetrics {
  // 지리적 연결성
  geographic: {
    nearbyConsumers: number; // 반경 내 소비자 수
    deliveryDistance: number; // 평균 배송 거리 (km)
    localMarketCount: number; // 지역 마켓 수
    transportationScore: number; // 교통 접근성 (0-10)
  };

  // 온라인 연결성
  online: {
    platformReach: number; // 플랫폼 도달률 (%)
    socialMediaFollowers: number; // SNS 팔로워
    emailSubscribers: number; // 이메일 구독자
    appUsers: number; // 앱 사용자
  };

  // 커뮤니티 연결성
  community: {
    activeMembers: number; // 활성 회원 수
    engagementRate: number; // 참여율 (%)
    eventParticipation: number; // 이벤트 참여 수
    wordOfMouthScore: number; // 입소문 점수 (0-10)
  };

  // 파트너십
  partnerships: {
    retailPartners: number; // 소매 파트너
    distributionChannels: number; // 유통 채널
    collaborations: number; // 협업 수
  };
}

function calculateConsumerConnectionIndex(
  metrics: ConsumerConnectionMetrics
): number {
  const weights = {
    geographic: 0.3,
    online: 0.3,
    community: 0.25,
    partnerships: 0.15,
  };

  // 1. 지리적 점수 (0-100)
  const geographicScore =
    Math.min(100, metrics.geographic.nearbyConsumers / 1000) * 0.35 +
    Math.max(0, 100 - metrics.geographic.deliveryDistance * 2) * 0.25 +
    Math.min(100, metrics.geographic.localMarketCount * 10) * 0.2 +
    metrics.geographic.transportationScore * 10 * 0.2;

  // 2. 온라인 점수 (0-100)
  const onlineScore =
    metrics.online.platformReach * 0.3 +
    Math.min(100, metrics.online.socialMediaFollowers / 1000) * 0.3 +
    Math.min(100, metrics.online.emailSubscribers / 500) * 0.2 +
    Math.min(100, metrics.online.appUsers / 200) * 0.2;

  // 3. 커뮤니티 점수 (0-100)
  const communityScore =
    Math.min(100, metrics.community.activeMembers / 500) * 0.3 +
    metrics.community.engagementRate * 0.3 +
    Math.min(100, metrics.community.eventParticipation / 10) * 0.2 +
    metrics.community.wordOfMouthScore * 10 * 0.2;

  // 4. 파트너십 점수 (0-100)
  const partnershipScore =
    Math.min(100, metrics.partnerships.retailPartners * 10) * 0.4 +
    Math.min(100, metrics.partnerships.distributionChannels * 15) * 0.35 +
    Math.min(100, metrics.partnerships.collaborations * 5) * 0.25;

  // 가중 평균
  const finalScore =
    geographicScore * weights.geographic +
    onlineScore * weights.online +
    communityScore * weights.community +
    partnershipScore * weights.partnerships;

  return Math.round(Math.min(100, Math.max(0, finalScore)));
}
```

### 7.2 GIS (지리정보시스템) 통합

```typescript
interface GISFeatures {
  // 위치 정보
  location: {
    latitude: number;
    longitude: number;
    address: string;
    region: string;
    district: string;
  };

  // 반경 검색
  nearbySearch: {
    radius: number; // km
    consumers: Consumer[];
    markets: LocalMarket[];
    competitors: Competitor[];
    transportHubs: TransportHub[];
  };

  // 히트맵
  heatmap: {
    demandDensity: number[][]; // 수요 밀도
    competitionLevel: number[][]; // 경쟁 수준
    priceRange: number[][]; // 가격대
  };

  // 경로 최적화
  routing: {
    deliveryRoutes: Route[];
    estimatedTime: number;
    estimatedCost: number;
  };
}

// 로컬 마켓 크롤링
interface LocalMarketCrawler {
  sources: {
    naverMap: boolean;
    kakaoMap: boolean;
    googleMaps: boolean;
    localGov: boolean; // 지자체 데이터
  };

  crawlData: {
    marketName: string;
    address: string;
    coordinates: { lat: number; lng: number };
    category: string;
    operatingHours: string;
    contact: string;
    website?: string;
    rating?: number;
    reviewCount?: number;
  }[];
}

// 웹 크롤링 구현 (Python)
const crawlerScript = `
import requests
from bs4 import BeautifulSoup
import json

def crawl_local_markets(region: str, radius_km: float):
    """
    지역 마켓 정보 크롤링
    """
    markets = []
    
    # 네이버 지도 API
    naver_url = f"https://map.naver.com/v5/api/search"
    params = {
        'query': f'{region} 로컬푸드 직매장',
        'type': 'all'
    }
    
    response = requests.get(naver_url, params=params)
    data = response.json()
    
    for item in data.get('result', {}).get('place', {}).get('list', []):
        markets.append({
            'name': item['name'],
            'address': item['address'],
            'lat': item['y'],
            'lng': item['x'],
            'category': item['category'],
            'phone': item.get('tel', ''),
            'rating': item.get('rating', 0)
        })
    
    return markets
`;
```

### 7.3 GIS 기반 매칭 시스템

```typescript
interface GISMatching {
  // 소비자-생산자 매칭
  matchConsumersToProducers(
    producerId: number,
    maxDistance: number
  ): ConsumerMatch[] {
    // 1. 생산자 위치 조회
    const producer = getProducerLocation(producerId);

    // 2. 반경 내 소비자 검색
    const nearbyConsumers = searchNearbyConsumers(
      producer.latitude,
      producer.longitude,
      maxDistance
    );

    // 3. 선호도 기반 필터링
    const matchedConsumers = nearbyConsumers.filter(consumer => {
      return matchesPreferences(consumer, producer);
    });

    // 4. 우선순위 정렬
    return matchedConsumers.sort((a, b) => {
      const scoreA = calculateMatchScore(a, producer);
      const scoreB = calculateMatchScore(b, producer);
      return scoreB - scoreA;
    });
  },

  // 로컬 마켓 추천
  recommendLocalMarkets(
    productId: number,
    maxDistance: number
  ): LocalMarketRecommendation[] {
    const product = getProduct(productId);
    const nearbyMarkets = searchNearbyMarkets(
      product.location,
      maxDistance
    );

    return nearbyMarkets.map(market => ({
      market,
      matchScore: calculateMarketMatchScore(product, market),
      estimatedSales: estimateSalesVolume(product, market),
      competitionLevel: assessCompetition(product, market),
      recommendations: generateMarketRecommendations(product, market)
    }));
  }
}
```

---

## 8. 데이터베이스 스키마 (추가)

### 8.1 사용자 라벨링 테이블

```sql
-- 사용자 라벨 테이블
CREATE TABLE user_labels (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,

    -- 역할 기반 분류
    primary_role VARCHAR(50) NOT NULL,  -- PRODUCER, SELLER, CONSUMER, CREATOR, EXPERT
    sub_role VARCHAR(50),                -- FARMER, CHEF, WHOLESALER 등

    -- 활동 지수
    activity_score INTEGER DEFAULT 0,    -- 0-1000
    activity_tier VARCHAR(20),           -- NEWCOMER, BEGINNER, ACTIVE, ENGAGED, INFLUENTIAL, EXPERT, MASTER

    -- 활동 메트릭스
    total_purchases INTEGER DEFAULT 0,
    total_sales INTEGER DEFAULT 0,
    total_transaction_value DECIMAL(15, 2) DEFAULT 0,
    posts_created INTEGER DEFAULT 0,
    recipes_created INTEGER DEFAULT 0,
    reviews_written INTEGER DEFAULT 0,
    comments_written INTEGER DEFAULT 0,
    likes_given INTEGER DEFAULT 0,
    likes_received INTEGER DEFAULT 0,
    shares_given INTEGER DEFAULT 0,
    shares_received INTEGER DEFAULT 0,
    average_rating DECIMAL(3, 2) DEFAULT 0,
    report_count INTEGER DEFAULT 0,
    verified_content INTEGER DEFAULT 0,
    login_streak INTEGER DEFAULT 0,

    -- 타임스탬프
    last_calculated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_id (user_id),
    INDEX idx_activity_score (activity_score),
    INDEX idx_activity_tier (activity_tier),
    INDEX idx_primary_role (primary_role)
);

-- 전문성 라벨 테이블
CREATE TABLE user_expertise_labels (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,

    category VARCHAR(50) NOT NULL,      -- KOREAN_FOOD, ORGANIC, BAKING 등
    level INTEGER NOT NULL,             -- 1-5
    verified_by BIGINT,                 -- 검증자 ID
    verification_date TIMESTAMP,

    -- 전문성 증빙
    evidence JSONB,                     -- 자격증, 경력, 포트폴리오 등

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (verified_by) REFERENCES users(id),
    UNIQUE INDEX idx_user_category (user_id, category),
    INDEX idx_category (category),
    INDEX idx_level (level)
);
```

### 8.2 시각적 평가 테이블

```sql
-- 시각적 반응 테이블
CREATE TABLE visual_reactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,

    -- 대상
    target_type VARCHAR(20) NOT NULL,   -- POST, COMMENT, PRODUCT, RECIPE
    target_id BIGINT NOT NULL,

    -- 반응 타입 (다중 선택 가능)
    business_potential JSONB,           -- ["HIGH_SALES_POTENTIAL", "WANT_TO_TRADE"]
    information_need JSONB,             -- ["WANT_MORE_INFO", "WANT_SOURCE"]
    emotional_support JSONB,            -- ["SUPPORT", "LOVE_IT"]
    quality_assessment JSONB,           -- ["HIGH_QUALITY", "FRESH"]
    concern JSONB,                      -- ["NEED_IMPROVEMENT", "PRICE_HIGH"]

    -- 강도 (선택)
    intensity INTEGER,                  -- 1-5

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE INDEX idx_user_target (user_id, target_type, target_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created_at (created_at)
);

-- 반응 집계 테이블 (성능 최적화)
CREATE TABLE visual_reaction_aggregates (
    id BIGSERIAL PRIMARY KEY,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,

    -- 카테고리별 집계
    business_potential_counts JSONB,    -- {"HIGH_SALES_POTENTIAL": 45, "WANT_TO_TRADE": 32}
    information_need_counts JSONB,
    emotional_support_counts JSONB,
    quality_assessment_counts JSONB,
    concern_counts JSONB,

    total_reactions INTEGER DEFAULT 0,

    last_updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    UNIQUE INDEX idx_target_agg (target_type, target_id)
);
```

### 8.3 댓글 관리 테이블

```sql
-- 댓글 설정 테이블
CREATE TABLE comment_settings (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL,

    -- 허용 설정
    comments_enabled BOOLEAN DEFAULT TRUE,
    require_approval BOOLEAN DEFAULT FALSE,
    allow_anonymous BOOLEAN DEFAULT FALSE,

    -- 사용자 제한
    min_activity_score INTEGER DEFAULT 0,
    min_account_age INTEGER DEFAULT 0,
    allowed_roles JSONB,                -- ["CONSUMER", "CREATOR"]
    allowed_tiers JSONB,                -- ["ACTIVE", "ENGAGED"]

    -- 내용 제한
    min_length INTEGER DEFAULT 10,
    max_length INTEGER DEFAULT 1000,
    allow_links BOOLEAN DEFAULT TRUE,
    allow_images BOOLEAN DEFAULT TRUE,
    allow_mentions BOOLEAN DEFAULT TRUE,

    -- 시간 제한
    cooldown_period INTEGER DEFAULT 0,  -- 초
    max_comments_per_day INTEGER DEFAULT 50,

    -- 필터링
    profanity_filter BOOLEAN DEFAULT TRUE,
    spam_filter BOOLEAN DEFAULT TRUE,
    ai_moderation BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (post_id) REFERENCES posts(id),
    INDEX idx_post_id (post_id)
);

-- 댓글 분석 테이블
CREATE TABLE comment_analysis (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL,

    -- AI 분석 결과
    analysis_type VARCHAR(20) NOT NULL, -- CONSTRUCTIVE, CRITICISM, ABUSE, SPAM
    confidence DECIMAL(3, 2),            -- 0-1

    -- 감정 분석
    sentiment_score DECIMAL(3, 2),       -- -1 ~ 1
    sentiment_magnitude DECIMAL(3, 2),   -- 0-1

    -- 특성 분석
    is_constructive BOOLEAN,
    has_specific_feedback BOOLEAN,
    has_solutions BOOLEAN,
    is_personal_attack BOOLEAN,
    has_abusive_language BOOLEAN,

    -- 카테고리 분류
    categories JSONB,                    -- {"PRODUCT_QUALITY": 0.8, "PRICE": 0.3}

    -- 자동 조치
    action VARCHAR(20),                  -- APPROVE, REVIEW, HIDE, BLOCK
    action_reason TEXT,

    analyzed_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (comment_id) REFERENCES comments(id),
    INDEX idx_comment_id (comment_id),
    INDEX idx_analysis_type (analysis_type),
    INDEX idx_action (action)
);
```

### 8.4 지수 계산 테이블

```sql
-- 판매가능지수 테이블
CREATE TABLE sales_potential_index (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,

    -- 지수 점수
    index_score INTEGER NOT NULL,       -- 0-100
    grade VARCHAR(20) NOT NULL,         -- EXCELLENT, VERY_GOOD, GOOD, FAIR, MODERATE, LOW, VERY_LOW

    -- 세부 점수
    product_score DECIMAL(5, 2),
    market_score DECIMAL(5, 2),
    seller_score DECIMAL(5, 2),
    performance_score DECIMAL(5, 2),
    content_score DECIMAL(5, 2),

    -- 메트릭스 (JSON)
    metrics JSONB,

    -- 추천 사항
    recommendations JSONB,

    calculated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_product_id (product_id),
    INDEX idx_index_score (index_score),
    INDEX idx_grade (grade),
    INDEX idx_calculated_at (calculated_at)
);

-- 소비가능지수 테이블
CREATE TABLE consumption_potential_index (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,

    -- 지수 점수
    index_score INTEGER NOT NULL,       -- 0-100
    grade VARCHAR(20) NOT NULL,

    -- 세부 점수
    demand_score DECIMAL(5, 2),
    accessibility_score DECIMAL(5, 2),
    trend_score DECIMAL(5, 2),
    satisfaction_score DECIMAL(5, 2),
    health_safety_score DECIMAL(5, 2),

    -- 메트릭스
    metrics JSONB,

    calculated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_product_id (product_id),
    INDEX idx_index_score (index_score)
);

-- 수익창출효과 테이블
CREATE TABLE revenue_generation_effect (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT,
    post_id BIGINT,

    -- 총 효과
    total_effect DECIMAL(15, 2),

    -- 항목별 기여도
    direct_revenue DECIMAL(15, 2),
    direct_revenue_pct DECIMAL(5, 2),
    indirect_revenue DECIMAL(15, 2),
    indirect_revenue_pct DECIMAL(5, 2),
    future_value DECIMAL(15, 2),
    future_value_pct DECIMAL(5, 2),
    cost_savings DECIMAL(15, 2),
    cost_savings_pct DECIMAL(5, 2),

    -- 성장 예측
    monthly_projection DECIMAL(15, 2),
    quarterly_projection DECIMAL(15, 2),
    yearly_projection DECIMAL(15, 2),
    growth_rate DECIMAL(5, 2),

    -- 등급
    grade VARCHAR(20),

    -- 추천 사항
    recommendations JSONB,

    calculated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (post_id) REFERENCES posts(id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_total_effect (total_effect)
);

-- 소비자연결지수 테이블
CREATE TABLE consumer_connection_index (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    product_id BIGINT,

    -- 지수 점수
    index_score INTEGER NOT NULL,       -- 0-100

    -- 세부 점수
    geographic_score DECIMAL(5, 2),
    online_score DECIMAL(5, 2),
    community_score DECIMAL(5, 2),
    partnership_score DECIMAL(5, 2),

    -- 메트릭스
    metrics JSONB,

    -- GIS 데이터
    nearby_consumers_count INTEGER,
    nearby_markets_count INTEGER,
    average_distance DECIMAL(10, 2),

    calculated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_index_score (index_score)
);
```

### 8.5 GIS 관련 테이블

```sql
-- 지역 정보 테이블
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,

    -- 위치 정보
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    address TEXT NOT NULL,

    -- 행정구역
    country VARCHAR(50) DEFAULT 'KR',
    province VARCHAR(50),               -- 시/도
    city VARCHAR(50),                   -- 시/군/구
    district VARCHAR(50),               -- 읍/면/동
    postal_code VARCHAR(10),

    -- GIS 인덱스 (PostGIS 사용 시)
    geom GEOMETRY(Point, 4326),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    INDEX idx_coordinates (latitude, longitude),
    SPATIAL INDEX idx_geom (geom)
);

-- 로컬 마켓 테이블
CREATE TABLE local_markets (
    id BIGSERIAL PRIMARY KEY,

    -- 기본 정보
    name VARCHAR(255) NOT NULL,
    location_id BIGINT NOT NULL,
    category VARCHAR(50),               -- 전통시장, 로컬푸드 직매장, 농협 하나로마트 등

    -- 연락처
    phone VARCHAR(20),
    website VARCHAR(255),
    email VARCHAR(100),

    -- 운영 정보
    operating_hours JSONB,              -- {"mon": "09:00-18:00", "tue": "09:00-18:00"}
    regular_holiday VARCHAR(50),

    -- 평가
    rating DECIMAL(3, 2),
    review_count INTEGER DEFAULT 0,

    -- 크롤링 정보
    data_source VARCHAR(50),            -- NAVER_MAP, KAKAO_MAP, GOOGLE_MAPS, LOCAL_GOV
    external_id VARCHAR(100),
    last_crawled_at TIMESTAMP,

    -- 상태
    is_active BOOLEAN DEFAULT TRUE,
    verified BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (location_id) REFERENCES locations(id),
    INDEX idx_location_id (location_id),
    INDEX idx_category (category),
    INDEX idx_rating (rating),
    INDEX idx_is_active (is_active)
);

-- 소비자-생산자 매칭 테이블
CREATE TABLE consumer_producer_matches (
    id BIGSERIAL PRIMARY KEY,

    consumer_id BIGINT NOT NULL,
    producer_id BIGINT NOT NULL,

    -- 매칭 점수
    match_score DECIMAL(5, 2),

    -- 거리 정보
    distance_km DECIMAL(10, 2),
    estimated_delivery_time INTEGER,    -- 분

    -- 선호도 매칭
    preference_match JSONB,

    -- 상태
    status VARCHAR(20),                 -- SUGGESTED, CONTACTED, CONNECTED, TRADING

    matched_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (consumer_id) REFERENCES users(id),
    FOREIGN KEY (producer_id) REFERENCES users(id),
    INDEX idx_consumer_id (consumer_id),
    INDEX idx_producer_id (producer_id),
    INDEX idx_match_score (match_score),
    INDEX idx_status (status)
);

-- 로컬 마켓 추천 테이블
CREATE TABLE local_market_recommendations (
    id BIGSERIAL PRIMARY KEY,

    product_id BIGINT NOT NULL,
    market_id BIGINT NOT NULL,

    -- 추천 점수
    match_score DECIMAL(5, 2),
    estimated_sales DECIMAL(15, 2),
    competition_level VARCHAR(20),      -- LOW, MEDIUM, HIGH

    -- 추천 사항
    recommendations JSONB,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (market_id) REFERENCES local_markets(id),
    INDEX idx_product_id (product_id),
    INDEX idx_market_id (market_id),
    INDEX idx_match_score (match_score)
);
```

---

## 9. 관리자 평가관리 기능

### 9.1 관리자 대시보드

```typescript
interface AdminEvaluationDashboard {
  // 개요
  overview: {
    totalReactions: number;
    totalComments: number;
    pendingReviews: number;
    flaggedContent: number;
  };

  // 댓글 관리
  commentManagement: {
    filters: {
      status: "ALL" | "PENDING" | "APPROVED" | "HIDDEN" | "REPORTED";
      type: "ALL" | "CONSTRUCTIVE" | "CRITICISM" | "ABUSE" | "SPAM";
      dateRange: { from: Date; to: Date };
    };

    comments: AdminComment[];

    bulkActions: {
      approve: (ids: number[]) => void;
      hide: (ids: number[]) => void;
      delete: (ids: number[]) => void;
      ban: (userIds: number[], duration: number) => void;
    };
  };

  // 반응 분석
  reactionAnalytics: {
    topReactions: ReactionStat[];
    reactionTrends: TrendData[];
    sentimentAnalysis: SentimentData;
  };

  // 지수 관리
  indexManagement: {
    recalculateAll: () => void;
    adjustWeights: (weights: IndexWeights) => void;
    exportData: () => void;
  };

  // 사용자 라벨 관리
  labelManagement: {
    reviewExpertiseRequests: ExpertiseRequest[];
    adjustActivityScores: (adjustments: ScoreAdjustment[]) => void;
    assignLabels: (userId: number, labels: Label[]) => void;
  };
}
```

### 9.2 댓글 검토 인터페이스

```typescript
interface CommentReviewInterface {
  comment: {
    id: number;
    content: string;
    author: UserSummary;
    post: PostSummary;
    createdAt: Date;
  };

  analysis: {
    type: string;
    confidence: number;
    sentiment: { score: number; magnitude: number };
    flags: string[];
    aiSuggestion: string;
  };

  actions: {
    approve: () => void;
    approveWithEdit: (editedContent: string) => void;
    hide: (reason: string) => void;
    delete: (reason: string) => void;
    warnUser: (message: string) => void;
    banUser: (duration: number, reason: string) => void;
    requestHumanReview: () => void;
  };

  history: {
    previousComments: Comment[];
    userReports: Report[];
    moderationHistory: ModerationAction[];
  };
}
```

### 9.3 지수 조정 도구

```typescript
interface IndexAdjustmentTool {
  // 가중치 조정
  weightAdjustment: {
    salesPotential: {
      product: number; // 0-1
      market: number;
      seller: number;
      performance: number;
      content: number;
    };

    consumptionPotential: {
      demand: number;
      accessibility: number;
      trend: number;
      satisfaction: number;
      healthSafety: number;
    };

    consumerConnection: {
      geographic: number;
      online: number;
      community: number;
      partnerships: number;
    };
  };

  // 일괄 재계산
  batchRecalculation: {
    target: "ALL" | "PRODUCTS" | "USERS" | "POSTS";
    filters: RecalculationFilters;
    schedule: Date | "NOW";
  };

  // A/B 테스트
  abTesting: {
    createExperiment: (config: ExperimentConfig) => void;
    compareResults: (experimentId: string) => ComparisonResult;
  };
}
```

---

## 10. 프론트엔드 UI 컴포넌트

### 10.1 시각적 반응 버튼

```typescript
// React 컴포넌트 예시
const VisualReactionButtons: React.FC<Props> = ({ targetType, targetId }) => {
  const [selectedReactions, setSelectedReactions] = useState<Reaction[]>([]);

  const reactionCategories = [
    {
      name: "비즈니스 잠재력",
      icon: "💼",
      reactions: [
        { id: "HIGH_SALES_POTENTIAL", icon: "🚀", label: "판매가능성 높아요" },
        { id: "WANT_TO_TRADE", icon: "🤝", label: "거래하고 싶어요" },
        { id: "HIGH_GROWTH_POTENTIAL", icon: "📈", label: "성장가능성 높아요" },
      ],
    },
    {
      name: "정보 요구",
      icon: "📋",
      reactions: [
        { id: "WANT_MORE_INFO", icon: "📋", label: "자세한 정보 알고싶어요" },
        { id: "WANT_SOURCE", icon: "🔍", label: "출처를 알고 싶어요" },
        { id: "WANT_RECIPE", icon: "👨‍🍳", label: "레시피 알고싶어요" },
      ],
    },
    {
      name: "감정 및 지지",
      icon: "❤️",
      reactions: [
        { id: "SUPPORT", icon: "💪", label: "응원해요" },
        { id: "LOVE_IT", icon: "❤️", label: "정말 좋아요" },
        { id: "RECOMMEND", icon: "👍", label: "추천해요" },
      ],
    },
  ];

  return (
    <div className="visual-reactions">
      {reactionCategories.map((category) => (
        <div key={category.name} className="reaction-category">
          <h4>
            {category.icon} {category.name}
          </h4>
          <div className="reaction-buttons">
            {category.reactions.map((reaction) => (
              <button
                key={reaction.id}
                className={
                  selectedReactions.includes(reaction.id) ? "selected" : ""
                }
                onClick={() => toggleReaction(reaction.id)}
              >
                <span className="icon">{reaction.icon}</span>
                <span className="label">{reaction.label}</span>
                <span className="count">{getReactionCount(reaction.id)}</span>
              </button>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};
```

### 10.2 지수 표시 카드

```typescript
const IndexDisplayCard: React.FC<{ productId: number }> = ({ productId }) => {
  const indices = useIndices(productId);

  return (
    <div className="index-cards">
      {/* 판매가능지수 */}
      <div className="index-card sales-potential">
        <div className="index-header">
          <span className="icon">🚀</span>
          <h3>판매가능지수</h3>
        </div>
        <div className="index-score">
          <CircularProgress value={indices.salesPotential.score} />
          <span className="percentage">{indices.salesPotential.score}%</span>
        </div>
        <div className="index-grade">
          <span className={`grade ${indices.salesPotential.grade}`}>
            {getGradeLabel(indices.salesPotential.grade)}
          </span>
        </div>
        <div className="index-breakdown">
          <div className="breakdown-item">
            <span>상품</span>
            <progress value={indices.salesPotential.productScore} max="100" />
          </div>
          <div className="breakdown-item">
            <span>시장</span>
            <progress value={indices.salesPotential.marketScore} max="100" />
          </div>
          <div className="breakdown-item">
            <span>판매자</span>
            <progress value={indices.salesPotential.sellerScore} max="100" />
          </div>
        </div>
        <div className="recommendations">
          {indices.salesPotential.recommendations.map((rec, i) => (
            <div key={i} className="recommendation-item">
              💡 {rec}
            </div>
          ))}
        </div>
      </div>

      {/* 소비가능지수 */}
      <div className="index-card consumption-potential">
        {/* 유사한 구조 */}
      </div>

      {/* 소비자연결지수 */}
      <div className="index-card consumer-connection">
        {/* 유사한 구조 + GIS 맵 */}
        <div className="gis-map">
          <MapComponent
            center={product.location}
            markers={nearbyConsumers}
            heatmap={demandHeatmap}
          />
        </div>
      </div>

      {/* 수익창출효과 */}
      <div className="index-card revenue-effect">
        <h3>💰 수익창출효과</h3>
        <div className="revenue-total">
          월 {formatCurrency(indices.revenueEffect.monthly)} 예상
        </div>
        <div className="revenue-breakdown">
          <PieChart data={indices.revenueEffect.breakdown} />
        </div>
        <div className="revenue-projection">
          <LineChart data={indices.revenueEffect.projection} />
        </div>
      </div>
    </div>
  );
};
```

---

## 11. 구현 우선순위 및 로드맵

### Phase 1 (1-2개월): 기본 시스템

**Week 1-2: 라벨링 시스템**

- [ ] 사용자 역할 분류
- [ ] 활동 지수 계산
- [ ] 등급 시스템
- [ ] DB 스키마 구현

**Week 3-4: 시각적 평가**

- [ ] 반응 카테고리 구현
- [ ] UI 컴포넌트
- [ ] 집계 시스템
- [ ] 실시간 업데이트

**Week 5-6: 댓글 관리**

- [ ] 댓글 설정 기능
- [ ] AI 분석 통합
- [ ] 비난/비판 구분
- [ ] 관리자 인터페이스

**Week 7-8: 기본 지수**

- [ ] 판매가능지수 알고리즘
- [ ] 소비가능지수 알고리즘
- [ ] UI 표시
- [ ] 테스트 및 조정

### Phase 2 (3-4개월): 고급 기능

**Week 9-12: 수익창출효과**

- [ ] 수익 추적 시스템
- [ ] 예측 모델
- [ ] 대시보드
- [ ] 리포팅

**Week 13-16: GIS 통합**

- [ ] PostGIS 설정
- [ ] 로컬 마켓 크롤러
- [ ] 소비자연결지수
- [ ] 지도 UI

### Phase 3 (5-6개월): 최적화 및 확장

**Week 17-20: AI 고도화**

- [ ] 머신러닝 모델 훈련
- [ ] 예측 정확도 향상
- [ ] 자동화 개선

**Week 21-24: 통합 및 최적화**

- [ ] 성능 최적화
- [ ] A/B 테스트
- [ ] 사용자 피드백 반영

---

## 12. 예상 비용

### 개발 비용

| 항목          | Phase 1       | Phase 2       | Phase 3       | 총계      |
| ------------- | ------------- | ------------- | ------------- | --------- |
| 라벨링 시스템 | 800만원       | -             | -             | 800만원   |
| 시각적 평가   | 1,000만원     | -             | -             | 1,000만원 |
| 댓글 관리     | 1,200만원     | -             | -             | 1,200만원 |
| 기본 지수     | 1,500만원     | -             | -             | 1,500만원 |
| 수익창출효과  | -             | 1,000만원     | -             | 1,000만원 |
| GIS 시스템    | -             | 2,000만원     | -             | 2,000만원 |
| AI 고도화     | -             | -             | 2,500만원     | 2,500만원 |
| **소계**      | **4,500만원** | **3,000만원** | **2,500만원** | **1억원** |

### 운영 비용 (월간)

| 항목                          | 비용           |
| ----------------------------- | -------------- |
| AI API (OpenAI, Google Cloud) | 50만원         |
| PostGIS 서버                  | 30만원         |
| 크롤링 인프라                 | 20만원         |
| 데이터 스토리지               | 20만원         |
| **총계**                      | **120만원/월** |

---

**작성일:** 2025-11-20  
**작성자:** 장재훈 **상태:** 설계 완료  
**다음 단계:** Phase 1 구현 시작