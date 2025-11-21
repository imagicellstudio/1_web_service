# XLCfi Platform - Frontend Implementation Milestone

## 📌 문서 목적

이 문서는 **프론트엔드 구현 시작 시점(2025-11-21)**의 기점(Milestone)을 명확히 하기 위해 작성되었습니다.

**목적:**
1. 프론트엔드 구현 계획의 완전한 기록
2. 백엔드와의 명확한 구분점 설정
3. 단계별 구현 로드맵 제시
4. 독립적인 프론트엔드 시스템으로서의 문서화

**프론트엔드 구현 시작:** 2025-11-21  
**프론트엔드 구현 상태:** 🚧 진행 중 (0%)  
**예상 구현 기간:** 4-6주

---

## 📋 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [기술 스택](#기술-스택)
3. [프로젝트 구조](#프로젝트-구조)
4. [I.A (Information Architecture)](#ia-information-architecture)
5. [화면 설계](#화면-설계)
6. [구현 로드맵](#구현-로드맵)
7. [백엔드 API 연동](#백엔드-api-연동)
8. [상태 관리](#상태-관리)
9. [스타일링 전략](#스타일링-전략)
10. [테스트 전략](#테스트-전략)

---

## 프로젝트 개요

### 서비스 설명

K-Food 원료, 원산지, 음식, 요리방법, 레시피 등을 소개하고 거래할 수 있는 플랫폼의 프론트엔드 시스템.

### 주요 특징

**✅ 반응형 디자인**
- Web (Desktop)
- Tablet
- Mobile

**✅ 다국어 지원**
- 한국어 (KO)
- 영어 (EN)
- 일본어 (JA)
- 중국어 (ZH)

**✅ 사용자 경험**
- Mobile First 접근
- Progressive Web App (PWA)
- 빠른 로딩 속도
- 직관적인 네비게이션

**✅ 고급 기능**
- 실시간 알림
- 지수 시각화 (차트)
- GIS 지도 통합
- NFT 갤러리
- 블록체인 지갑 연동

---

## 기술 스택

### Core Framework
```
Framework: Next.js 14+ (App Router)
Language: TypeScript 5+
Package Manager: pnpm
Node Version: 18 LTS
```

### UI & Styling
```
CSS Framework: Tailwind CSS 3+
UI Components: shadcn/ui
Icons: Lucide React
Animations: Framer Motion
```

### State Management
```
Global State: Zustand
Server State: TanStack Query (React Query)
Form State: React Hook Form
Validation: Zod
```

### Data Fetching
```
HTTP Client: Axios
API Integration: TanStack Query
WebSocket: Socket.io Client
```

### Blockchain Integration
```
Web3 Library: ethers.js v6
Wallet Connect: RainbowKit
IPFS: ipfs-http-client
```

### Maps & Visualization
```
Maps: Leaflet.js + React Leaflet
Charts: Recharts
Data Visualization: D3.js (필요시)
```

### Development Tools
```
Linting: ESLint
Formatting: Prettier
Type Checking: TypeScript
Testing: Vitest + React Testing Library
E2E Testing: Playwright
```

### Build & Deployment
```
Build Tool: Next.js (Turbopack)
Deployment: Vercel
CDN: Vercel Edge Network
Analytics: Vercel Analytics
```

---

## 프로젝트 구조

```
frontend/
├── public/                      # 정적 파일
│   ├── images/
│   ├── icons/
│   ├── fonts/
│   └── locales/                 # 다국어 JSON
│
├── src/
│   ├── app/                     # Next.js App Router
│   │   ├── (auth)/              # 인증 그룹
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   └── layout.tsx
│   │   │
│   │   ├── (main)/              # 메인 그룹
│   │   │   ├── page.tsx         # 홈
│   │   │   ├── products/
│   │   │   │   ├── page.tsx
│   │   │   │   └── [id]/
│   │   │   │       └── page.tsx
│   │   │   ├── cart/
│   │   │   ├── checkout/
│   │   │   ├── mypage/
│   │   │   └── layout.tsx
│   │   │
│   │   ├── (seller)/            # 판매자 그룹
│   │   │   ├── seller/
│   │   │   │   ├── dashboard/
│   │   │   │   ├── products/
│   │   │   │   ├── orders/
│   │   │   │   └── nfts/
│   │   │   └── layout.tsx
│   │   │
│   │   ├── (admin)/             # 관리자 그룹
│   │   │   ├── admin/
│   │   │   │   ├── dashboard/
│   │   │   │   ├── users/
│   │   │   │   ├── products/
│   │   │   │   └── settings/
│   │   │   └── layout.tsx
│   │   │
│   │   ├── blockchain/          # 블록체인
│   │   │   ├── marketplace/
│   │   │   ├── p2p/
│   │   │   └── token/
│   │   │
│   │   ├── api/                 # API Routes
│   │   │   └── auth/
│   │   │
│   │   ├── layout.tsx           # Root Layout
│   │   ├── loading.tsx
│   │   ├── error.tsx
│   │   └── not-found.tsx
│   │
│   ├── components/              # 컴포넌트
│   │   ├── ui/                  # shadcn/ui 컴포넌트
│   │   │   ├── button.tsx
│   │   │   ├── input.tsx
│   │   │   ├── card.tsx
│   │   │   └── ...
│   │   │
│   │   ├── layout/              # 레이아웃 컴포넌트
│   │   │   ├── Header.tsx
│   │   │   ├── Footer.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   └── Navigation.tsx
│   │   │
│   │   ├── product/             # 상품 관련
│   │   │   ├── ProductCard.tsx
│   │   │   ├── ProductList.tsx
│   │   │   ├── ProductDetail.tsx
│   │   │   └── ProductFilter.tsx
│   │   │
│   │   ├── order/               # 주문 관련
│   │   │   ├── CartItem.tsx
│   │   │   ├── OrderSummary.tsx
│   │   │   └── OrderHistory.tsx
│   │   │
│   │   ├── payment/             # 결제 관련
│   │   │   ├── PaymentMethod.tsx
│   │   │   ├── TossPayment.tsx
│   │   │   ├── NicePayment.tsx
│   │   │   └── StripePayment.tsx
│   │   │
│   │   ├── review/              # 리뷰 관련
│   │   │   ├── ReviewCard.tsx
│   │   │   ├── ReviewForm.tsx
│   │   │   ├── ReactionButtons.tsx
│   │   │   └── RatingStars.tsx
│   │   │
│   │   ├── nft/                 # NFT 관련
│   │   │   ├── NFTCard.tsx
│   │   │   ├── NFTGallery.tsx
│   │   │   ├── NFTDetail.tsx
│   │   │   └── MintNFTForm.tsx
│   │   │
│   │   ├── blockchain/          # 블록체인 관련
│   │   │   ├── WalletConnect.tsx
│   │   │   ├── TokenBalance.tsx
│   │   │   └── TransactionHistory.tsx
│   │   │
│   │   ├── chart/               # 차트 관련
│   │   │   ├── IndexChart.tsx
│   │   │   ├── SalesChart.tsx
│   │   │   └── RevenueChart.tsx
│   │   │
│   │   ├── map/                 # 지도 관련
│   │   │   ├── GISMap.tsx
│   │   │   ├── MarkerCluster.tsx
│   │   │   └── LocationSearch.tsx
│   │   │
│   │   └── common/              # 공통 컴포넌트
│   │       ├── Loading.tsx
│   │       ├── ErrorBoundary.tsx
│   │       ├── Pagination.tsx
│   │       ├── SearchBar.tsx
│   │       └── LanguageSelector.tsx
│   │
│   ├── lib/                     # 유틸리티
│   │   ├── api/                 # API 클라이언트
│   │   │   ├── client.ts
│   │   │   ├── auth.ts
│   │   │   ├── product.ts
│   │   │   ├── order.ts
│   │   │   ├── payment.ts
│   │   │   └── nft.ts
│   │   │
│   │   ├── blockchain/          # 블록체인 유틸
│   │   │   ├── contracts.ts
│   │   │   ├── wallet.ts
│   │   │   └── ipfs.ts
│   │   │
│   │   ├── utils/               # 공통 유틸
│   │   │   ├── format.ts
│   │   │   ├── validation.ts
│   │   │   └── helpers.ts
│   │   │
│   │   └── constants/           # 상수
│   │       ├── routes.ts
│   │       ├── api.ts
│   │       └── config.ts
│   │
│   ├── hooks/                   # Custom Hooks
│   │   ├── useAuth.ts
│   │   ├── useCart.ts
│   │   ├── useProducts.ts
│   │   ├── useOrders.ts
│   │   ├── usePayment.ts
│   │   ├── useNFT.ts
│   │   ├── useWallet.ts
│   │   └── useI18n.ts
│   │
│   ├── store/                   # Zustand Store
│   │   ├── authStore.ts
│   │   ├── cartStore.ts
│   │   ├── uiStore.ts
│   │   └── walletStore.ts
│   │
│   ├── types/                   # TypeScript 타입
│   │   ├── api.ts
│   │   ├── models.ts
│   │   ├── blockchain.ts
│   │   └── common.ts
│   │
│   ├── styles/                  # 스타일
│   │   ├── globals.css
│   │   └── variables.css
│   │
│   └── middleware.ts            # Next.js Middleware
│
├── tests/                       # 테스트
│   ├── unit/
│   ├── integration/
│   └── e2e/
│
├── .env.local                   # 환경 변수
├── .env.example
├── next.config.js
├── tailwind.config.ts
├── tsconfig.json
├── package.json
└── README.md
```

---

## I.A (Information Architecture)

### 완료 사항
✅ **I.A 문서 작성 완료** - `01_information_architecture.md`

**주요 내용:**
- 사이트맵 (3단계 계층)
- 네비게이션 구조 (Global, Mobile, Footer)
- 사용자 여정 맵 (3가지 시나리오)
- 화면 흐름도 (Mermaid 다이어그램)
- 콘텐츠 계층 구조
- 역할별 접근 권한 매트릭스

**참조:** [01_information_architecture.md](./01_information_architecture.md)

---

## 화면 설계

### Phase 1: 핵심 화면 (우선순위 높음)

#### 1. 인증 화면
- [ ] 로그인 (`/login`)
- [ ] 회원가입 (`/register`)
- [ ] 비밀번호 찾기 (`/forgot-password`)

#### 2. 홈 & 상품
- [ ] 홈페이지 (`/`)
- [ ] 상품 목록 (`/products`)
- [ ] 상품 상세 (`/products/:id`)
- [ ] 검색 결과 (`/search`)

#### 3. 주문 & 결제
- [ ] 장바구니 (`/cart`)
- [ ] 주문하기 (`/checkout`)
- [ ] 결제 (`/payment`)
- [ ] 주문 완료 (`/payment/success`)

#### 4. 마이페이지
- [ ] 마이페이지 대시보드 (`/mypage`)
- [ ] 프로필 (`/mypage/profile`)
- [ ] 주문 내역 (`/mypage/orders`)
- [ ] 리뷰 관리 (`/mypage/reviews`)

### Phase 2: 고급 기능 (우선순위 중간)

#### 5. 판매자 센터
- [ ] 판매자 대시보드 (`/seller/dashboard`)
- [ ] 상품 관리 (`/seller/products`)
- [ ] 주문 관리 (`/seller/orders`)
- [ ] 정산 관리 (`/seller/settlements`)

#### 6. NFT & 블록체인
- [ ] NFT 마켓플레이스 (`/blockchain/marketplace`)
- [ ] NFT 상세 (`/blockchain/nfts/:id`)
- [ ] P2P 거래 (`/blockchain/p2p`)
- [ ] 토큰 지갑 (`/mypage/wallet`)

#### 7. 평가 시스템
- [ ] 시각적 반응 UI
- [ ] 지수 표시 (4가지)
- [ ] 차트 시각화
- [ ] GIS 지도

### Phase 3: 관리자 & 추가 기능 (우선순위 낮음)

#### 8. 관리자
- [ ] 관리자 대시보드 (`/admin/dashboard`)
- [ ] 회원 관리 (`/admin/users`)
- [ ] 상품 관리 (`/admin/products`)
- [ ] 주문 관리 (`/admin/orders`)
- [ ] 리뷰 관리 (`/admin/reviews`)
- [ ] 라벨링 관리 (`/admin/labels`)
- [ ] 지수 관리 (`/admin/indices`)

#### 9. 커뮤니티
- [ ] 레시피 게시판 (`/community/recipes`)
- [ ] 평가 대시보드 (`/community/reviews`)
- [ ] 지수 랭킹 (`/community/rankings`)

---

## 구현 로드맵

### Week 1-2: 프로젝트 초기 설정 & 기본 화면

**목표:** 프로젝트 세팅 및 인증/홈 화면 구현

**작업 항목:**
1. ✅ I.A 문서 작성
2. [ ] Next.js 프로젝트 초기화
3. [ ] 기술 스택 설치 및 설정
   - Tailwind CSS
   - shadcn/ui
   - Zustand
   - TanStack Query
   - Axios
4. [ ] 프로젝트 구조 생성
5. [ ] 공통 컴포넌트 구현
   - Header
   - Footer
   - Navigation
   - Loading
   - ErrorBoundary
6. [ ] 인증 화면 구현
   - 로그인
   - 회원가입
   - JWT 토큰 관리
7. [ ] 홈페이지 구현
   - Hero Section
   - Featured Products
   - NFT Showcase

**예상 산출물:**
- 프로젝트 초기 구조
- 공통 레이아웃
- 인증 시스템
- 홈페이지

### Week 3-4: 상품 & 주문 화면

**목표:** 핵심 비즈니스 로직 구현

**작업 항목:**
1. [ ] 상품 화면 구현
   - 상품 목록 (필터링, 정렬, 페이지네이션)
   - 상품 상세 (이미지 갤러리, 정보, 리뷰)
   - 검색 기능
2. [ ] 주문 화면 구현
   - 장바구니 (CRUD)
   - 주문하기 (배송지 입력)
   - 주문 확인
3. [ ] 결제 연동
   - TossPayments
   - NicePay
   - Stripe
4. [ ] 마이페이지 구현
   - 프로필
   - 주문 내역
   - 리뷰 관리

**예상 산출물:**
- 상품 조회 시스템
- 주문/결제 시스템
- 마이페이지

### Week 5-6: 고급 기능 & 최적화

**목표:** NFT, 블록체인, 평가 시스템 구현

**작업 항목:**
1. [ ] 판매자 센터 구현
   - 대시보드
   - 상품 관리
   - 주문 관리
2. [ ] NFT & 블록체인 구현
   - 지갑 연동 (RainbowKit)
   - NFT 갤러리
   - P2P 거래
   - 토큰 관리
3. [ ] 평가 시스템 구현
   - 시각적 반응 UI
   - 지수 차트 (Recharts)
   - GIS 지도 (Leaflet)
4. [ ] 성능 최적화
   - 이미지 최적화
   - 코드 스플리팅
   - 캐싱 전략
5. [ ] 테스트 작성
   - Unit Tests
   - Integration Tests
   - E2E Tests

**예상 산출물:**
- 판매자 시스템
- NFT/블록체인 기능
- 고급 평가 시스템
- 테스트 커버리지 80%+

### Week 7-8: 관리자 & 배포 (선택)

**목표:** 관리자 기능 및 프로덕션 배포

**작업 항목:**
1. [ ] 관리자 화면 구현
   - 대시보드
   - 회원/상품/주문 관리
   - 라벨링/지수 관리
2. [ ] PWA 설정
   - Service Worker
   - Manifest
   - Offline 지원
3. [ ] 다국어 완성
   - 모든 화면 번역
   - 언어 전환 테스트
4. [ ] 배포 준비
   - 환경 변수 설정
   - Vercel 배포
   - CDN 설정
5. [ ] 문서 작성
   - README
   - API 문서
   - 컴포넌트 문서

**예상 산출물:**
- 관리자 시스템
- PWA
- 프로덕션 배포
- 완전한 문서

---

## 백엔드 API 연동

### API Base URLs

```typescript
// src/lib/constants/api.ts
export const API_BASE_URLS = {
  auth: 'http://localhost:8081',
  product: 'http://localhost:8082',
  order: 'http://localhost:8083',
  payment: 'http://localhost:8084',
  review: 'http://localhost:8085',
} as const;
```

### Axios 인스턴스

```typescript
// src/lib/api/client.ts
import axios from 'axios';
import { API_BASE_URLS } from '@/lib/constants/api';

export const authApi = axios.create({
  baseURL: API_BASE_URLS.auth,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor
authApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor
authApi.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post(
          `${API_BASE_URLS.auth}/api/auth/refresh`,
          {},
          { headers: { 'Refresh-Token': refreshToken } }
        );
        
        const { accessToken, refreshToken: newRefreshToken } = response.data.data;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
        
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return authApi(originalRequest);
      } catch (refreshError) {
        // 로그아웃 처리
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);
```

### TanStack Query 설정

```typescript
// src/app/providers.tsx
'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { useState } from 'react';

export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 60 * 1000, // 1분
            refetchOnWindowFocus: false,
          },
        },
      })
  );

  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}
```

### API 사용 예시

```typescript
// src/hooks/useProducts.ts
import { useQuery } from '@tanstack/react-query';
import { productApi } from '@/lib/api/product';

export function useProducts(params?: ProductListParams) {
  return useQuery({
    queryKey: ['products', params],
    queryFn: () => productApi.getProducts(params),
  });
}

export function useProduct(id: string) {
  return useQuery({
    queryKey: ['product', id],
    queryFn: () => productApi.getProduct(id),
    enabled: !!id,
  });
}
```

---

## 상태 관리

### Zustand Store 예시

```typescript
// src/store/authStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  id: number;
  email: string;
  name: string;
  role: 'BUYER' | 'SELLER' | 'ADMIN';
}

interface AuthState {
  user: User | null;
  accessToken: string | null;
  refreshToken: string | null;
  setUser: (user: User) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      setUser: (user) => set({ user }),
      setTokens: (accessToken, refreshToken) => 
        set({ accessToken, refreshToken }),
      logout: () => 
        set({ user: null, accessToken: null, refreshToken: null }),
    }),
    {
      name: 'auth-storage',
    }
  )
);
```

```typescript
// src/store/cartStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface CartItem {
  productId: number;
  name: string;
  price: number;
  quantity: number;
  image: string;
}

interface CartState {
  items: CartItem[];
  addItem: (item: CartItem) => void;
  removeItem: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;
  getTotalPrice: () => number;
  getTotalItems: () => number;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],
      addItem: (item) =>
        set((state) => {
          const existingItem = state.items.find(
            (i) => i.productId === item.productId
          );
          if (existingItem) {
            return {
              items: state.items.map((i) =>
                i.productId === item.productId
                  ? { ...i, quantity: i.quantity + item.quantity }
                  : i
              ),
            };
          }
          return { items: [...state.items, item] };
        }),
      removeItem: (productId) =>
        set((state) => ({
          items: state.items.filter((i) => i.productId !== productId),
        })),
      updateQuantity: (productId, quantity) =>
        set((state) => ({
          items: state.items.map((i) =>
            i.productId === productId ? { ...i, quantity } : i
          ),
        })),
      clearCart: () => set({ items: [] }),
      getTotalPrice: () =>
        get().items.reduce((sum, item) => sum + item.price * item.quantity, 0),
      getTotalItems: () =>
        get().items.reduce((sum, item) => sum + item.quantity, 0),
    }),
    {
      name: 'cart-storage',
    }
  )
);
```

---

## 스타일링 전략

### Tailwind CSS 설정

```typescript
// tailwind.config.ts
import type { Config } from 'tailwindcss';

const config: Config = {
  darkMode: ['class'],
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#fff7ed',
          100: '#ffedd5',
          500: '#f97316',
          600: '#ea580c',
          700: '#c2410c',
        },
        secondary: {
          50: '#f0fdf4',
          100: '#dcfce7',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
        },
      },
      fontFamily: {
        sans: ['var(--font-pretendard)'],
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
};

export default config;
```

### 반응형 Breakpoints

```css
/* Mobile First */
/* xs: 0-639px (default) */
/* sm: 640px+ */
/* md: 768px+ */
/* lg: 1024px+ */
/* xl: 1280px+ */
/* 2xl: 1536px+ */
```

---

## 테스트 전략

### Unit Tests (Vitest)

```typescript
// src/components/ProductCard.test.tsx
import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { ProductCard } from './ProductCard';

describe('ProductCard', () => {
  it('renders product information', () => {
    const product = {
      id: 1,
      name: '유기농 배추',
      price: 5000,
      image: '/images/product1.jpg',
    };

    render(<ProductCard product={product} />);

    expect(screen.getByText('유기농 배추')).toBeInTheDocument();
    expect(screen.getByText('₩5,000')).toBeInTheDocument();
  });
});
```

### E2E Tests (Playwright)

```typescript
// tests/e2e/purchase.spec.ts
import { test, expect } from '@playwright/test';

test('complete purchase flow', async ({ page }) => {
  // 1. 홈페이지 방문
  await page.goto('/');

  // 2. 상품 검색
  await page.fill('[data-testid="search-input"]', '배추');
  await page.click('[data-testid="search-button"]');

  // 3. 상품 선택
  await page.click('[data-testid="product-card"]:first-child');

  // 4. 장바구니 담기
  await page.click('[data-testid="add-to-cart"]');

  // 5. 장바구니 확인
  await page.click('[data-testid="cart-icon"]');
  await expect(page.locator('[data-testid="cart-item"]')).toBeVisible();

  // 6. 주문하기
  await page.click('[data-testid="checkout-button"]');

  // 7. 배송지 입력
  await page.fill('[name="address"]', '서울시 강남구 테헤란로 123');

  // 8. 결제
  await page.click('[data-testid="payment-button"]');

  // 9. 주문 완료 확인
  await expect(page).toHaveURL(/\/payment\/success/);
});
```

---

## 다음 단계

### 즉시 시작 가능
1. ✅ **I.A 문서 완료**
2. ⏭️ **프로젝트 초기화** - Next.js 프로젝트 생성
3. ⏭️ **기술 스택 설치** - 의존성 설치
4. ⏭️ **프로젝트 구조 생성** - 폴더 구조 생성
5. ⏭️ **공통 컴포넌트 구현** - Header, Footer, Navigation

### 향후 계획
- Week 1-2: 프로젝트 초기 설정 & 기본 화면
- Week 3-4: 상품 & 주문 화면
- Week 5-6: 고급 기능 & 최적화
- Week 7-8: 관리자 & 배포 (선택)

---

## 참고 문서

### 프론트엔드 설계 문서
- [01_information_architecture.md](./01_information_architecture.md) - I.A 문서
- [01_frontend_responsive_design.md](./01_frontend_responsive_design.md) - 반응형 설계

### 백엔드 문서
- [Backend Implementation Milestone](../../backend/BACKEND_IMPLEMENTATION_MILESTONE.md)
- [Java API Specs](../../004.architecture/11_java_api_specs_detailed.md)
- [Advanced Evaluation System](../../004.architecture/14_advanced_evaluation_system.md)

---

**문서 버전:** 1.0.0  
**최종 업데이트:** 2025-11-21  
**상태:** 🚧 프론트엔드 구현 시작

