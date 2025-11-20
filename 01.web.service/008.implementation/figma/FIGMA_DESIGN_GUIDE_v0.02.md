# SpicyJump Figma Design Guide v0.02

## 버전 정보
- **버전**: v0.02
- **타입**: Figma Design
- **기반**: v0.01 HTML Prototype
- **작성일**: 2025-11-19
- **목적**: Figma로 고품질 UI 디자인 작성

---

## 1. Figma 파일 구조

### 추천 파일명
```
SpicyJump_v0.02_Homepage_Design.fig
```

### 페이지 구성
```
📄 SpicyJump v0.02
├── 📐 Design System (컴포넌트 라이브러리)
├── 🖥️ Desktop - Homepage (1920x1080)
├── 📱 Mobile - Homepage (375x812)
└── 📋 Specs & Handoff (개발자용)
```

---

## 2. Design System (컴포넌트 라이브러리)

### 2.1 Colors (컬러 팔레트)

#### Primary Colors
```
Primary/Red
└─ Default: #E63946
└─ Hover:   #D62839
└─ Active:  #C71F2E

Secondary/Light
└─ Default: #F1FAEE
└─ Tint:    #F8FCFB

Accent/Blue
└─ Default: #A8DADC
└─ Light:   #C4E6E8
```

#### Neutral Colors
```
Dark/Navy
└─ Default: #1D3557
└─ Light:   #2E4A6F

Gray
└─ Gray-900: #1D3557
└─ Gray-700: #457B9D
└─ Gray-500: #6C757D
└─ Gray-300: #ADB5BD
└─ Gray-100: #E9ECEF
└─ Gray-50:  #F8F9FA
```

#### Semantic Colors
```
Success: #52B788
Warning: #F77F00
Danger:  #D62828
Info:    #457B9D
```

### 2.2 Typography

#### Font Families
```
Korean:  Noto Sans KR
English: Inter

Weight:
- Regular: 400
- Medium:  500
- SemiBold: 600
- Bold:    700
```

#### Text Styles
```
Heading 1 (Hero Title)
├─ Font: Inter/Bold
├─ Size: 48px
├─ Line: 58px (120%)
├─ Color: Dark/Navy
└─ Letter: -0.5px

Heading 2 (Section Title)
├─ Font: Noto Sans KR/Bold
├─ Size: 32px
├─ Line: 42px (130%)
└─ Color: Dark/Navy

Heading 3 (Product Name)
├─ Font: Noto Sans KR/SemiBold
├─ Size: 16px
├─ Line: 24px (150%)
└─ Color: Dark/Navy

Body/Regular
├─ Font: Noto Sans KR/Regular
├─ Size: 16px
├─ Line: 26px (160%)
└─ Color: Gray-700

Body/Small
├─ Font: Noto Sans KR/Regular
├─ Size: 14px
├─ Line: 22px (160%)
└─ Color: Gray-500

Caption
├─ Font: Inter/Regular
├─ Size: 12px
├─ Line: 18px (150%)
└─ Color: Gray-500
```

### 2.3 Spacing System

```
Space-xs:   4px   (Tight spacing)
Space-sm:   8px   (Small gaps)
Space-md:   16px  (Standard spacing)
Space-lg:   24px  (Section spacing)
Space-xl:   32px  (Large gaps)
Space-xxl:  48px  (Section margins)
Space-3xl:  64px  (Page sections)
```

### 2.4 Border Radius

```
Radius-sm:  4px   (Buttons, Badges)
Radius-md:  8px   (Cards, Inputs)
Radius-lg:  12px  (Large Cards)
Radius-xl:  16px  (Hero elements)
Radius-full: 9999px (Pills, Avatars)
```

### 2.5 Shadows

```
Shadow-sm:  0 2px 4px rgba(0,0,0,0.1)
Shadow-md:  0 4px 12px rgba(0,0,0,0.1)
Shadow-lg:  0 8px 24px rgba(0,0,0,0.15)
Shadow-xl:  0 16px 48px rgba(0,0,0,0.2)
```

---

## 3. 컴포넌트 디자인 가이드

### 3.1 Buttons

#### Primary Button
```
Component: Button/Primary
├─ Width: Auto (min 120px)
├─ Height: 40px
├─ Padding: 8px 24px
├─ Background: Primary/Red (#E63946)
├─ Text: White, 14px, SemiBold
├─ Border Radius: 8px
├─ Hover: Background → #D62839, Lift 2px
└─ Active: Background → #C71F2E
```

#### Secondary Button
```
Component: Button/Secondary
├─ Background: White
├─ Border: 2px solid Primary/Red
├─ Text: Primary/Red, 14px, SemiBold
└─ Hover: Background → Secondary/Light
```

#### Large Button (Hero CTA)
```
Component: Button/Large
├─ Height: 56px
├─ Padding: 16px 32px
├─ Font Size: 16px
└─ Border Radius: 12px
```

### 3.2 Product Card

#### 구조
```
Component: Product Card
├─ Width: 100% (Grid: 4 columns)
├─ Height: Auto
├─ Background: White
├─ Border Radius: 12px
├─ Shadow: shadow-sm
├─ Hover: Transform Y(-8px), shadow-lg
└─ Parts:
    ├─ Product Image (1:1 ratio)
    │   ├─ Badge (NEW/BEST)
    │   └─ Placeholder or Real Image
    ├─ Product Info (padding: 16px)
    │   ├─ Product Name (Korean)
    │   ├─ Product Name (English)
    │   ├─ Rating (Stars + Count)
    │   ├─ Price (Primary/Red, 20px, Bold)
    │   └─ CTA Button
```

#### Badge
```
Component: Badge/New
├─ Background: Success (#52B788)
├─ Text: White, 12px, Bold
├─ Padding: 4px 12px
├─ Border Radius: 4px
└─ Position: Absolute, Top-right

Component: Badge/Best
├─ Background: Warning (#F77F00)
```

### 3.3 Category Card

```
Component: Category Card
├─ Width: 100% (Grid: 6 columns)
├─ Height: 180px
├─ Background: White
├─ Border: 2px solid Gray-100
├─ Border Radius: 12px
├─ Padding: 32px
├─ Hover: Border → Primary/Red, Transform Y(-4px)
└─ Content:
    ├─ Icon (Emoji or SVG, 48px)
    ├─ Name (16px, SemiBold)
    └─ Count (12px, Gray-500)
```

### 3.4 Header

```
Component: Header
├─ Height: 72px (Desktop), 64px (Mobile)
├─ Background: White
├─ Shadow: shadow-sm
├─ Position: Sticky top
└─ Parts:
    ├─ Logo (Left)
    ├─ Navigation (Center)
    ├─ Search Bar (Center-right)
    └─ Actions (Right)
        ├─ Cart Icon (with Badge)
        ├─ Language Icon
        └─ Auth Buttons
```

### 3.5 Footer

```
Component: Footer
├─ Background: Dark/Navy (#1D3557)
├─ Text: White/70% opacity
├─ Padding: 48px 0 24px
└─ Grid: 4 columns
    ├─ Column 1: Logo + Description + Social
    ├─ Column 2: Customer Support Links
    ├─ Column 3: Company Info Links
    └─ Column 4: Newsletter Form
```

---

## 4. 화면별 디자인 가이드

### 4.1 Desktop Homepage (1920x1080)

#### 섹션 구조
```
📄 Desktop Homepage
├─ Header (Sticky, 72px)
├─ Hero Banner (600px height)
│   ├─ Left: Title + Subtitle + CTA (50%)
│   └─ Right: Image/Illustration (50%)
├─ Categories (padding: 64px 0)
│   ├─ Section Title
│   └─ 6-column Grid
├─ New Arrivals (padding: 64px 0, bg: Secondary/Light)
│   ├─ Section Header (Title + View All)
│   └─ 4-column Grid (8 products)
├─ Best Sellers (padding: 64px 0)
│   ├─ Section Header
│   └─ 4-column Grid (4 products)
└─ Footer
```

#### 콘텐츠 영역
```
Container Max Width: 1200px
Padding: 0 24px
Margin: 0 auto
```

### 4.2 Mobile Homepage (375x812)

#### 섹션 구조
```
📱 Mobile Homepage
├─ Header (Sticky, 64px + 56px search)
├─ Hero Banner (500px height)
│   ├─ Title (32px)
│   ├─ Subtitle (16px)
│   ├─ CTA Buttons (stacked)
│   └─ Image (300px)
├─ Categories (padding: 48px 16px)
│   └─ 2-column Grid
├─ New Arrivals (padding: 48px 16px)
│   └─ 1-column Grid (Scroll horizontal)
├─ Best Sellers (padding: 48px 16px)
│   └─ 1-column Grid
└─ Footer (1-column)
```

---

## 5. 이미지 가이드

### 5.1 권장 이미지 소스

#### 무료 이미지
```
- Unsplash: https://unsplash.com/s/photos/korean-food
- Pexels: https://www.pexels.com/search/korean%20food/
```

#### 추천 검색 키워드
```
- Korean food
- Kimchi
- Bibimbap
- Korean noodles
- Korean market
- Asian grocery
```

### 5.2 이미지 규격

```
Product Images:
├─ Size: 800x800px (1:1 ratio)
├─ Format: JPG (80% quality)
└─ File size: < 200KB

Hero Image:
├─ Size: 1200x800px
├─ Format: JPG or PNG
└─ File size: < 500KB

Category Icons:
├─ Size: 128x128px
├─ Format: PNG or SVG
└─ Style: Flat, Colorful
```

---

## 6. Figma 작업 순서

### Step 1: Setup (15분)
1. 새 Figma 파일 생성: "SpicyJump_v0.02_Homepage_Design"
2. 페이지 4개 생성 (Design System, Desktop, Mobile, Specs)
3. Frame 생성:
   - Desktop: 1920x1080
   - Mobile: 375x812

### Step 2: Design System (30분)
1. Color Styles 등록 (위 컬러 팔레트)
2. Text Styles 등록 (위 타이포그래피)
3. Components 생성:
   - Buttons (Primary, Secondary, Large)
   - Product Card
   - Category Card
   - Badge (New, Best)

### Step 3: Desktop Design (60분)
1. Header 디자인
2. Hero Banner 디자인
3. Categories 섹션 (6개 카드)
4. New Arrivals 섹션 (8개 상품)
5. Best Sellers 섹션 (4개 상품)
6. Footer 디자인

### Step 4: Mobile Design (45분)
1. Mobile Header (검색바 포함)
2. Hero Banner (세로 레이아웃)
3. Categories (2-column)
4. Products (1-column 또는 horizontal scroll)
5. Footer (1-column)

### Step 5: Prototype (30분)
1. Desktop 화면 간 링크 연결
2. Mobile 화면 간 링크 연결
3. 버튼 Hover 효과
4. 간단한 인터랙션 (클릭 → 페이지 이동)

### Step 6: Handoff (30분)
1. Specs 페이지 작성
2. Component 명세 정리
3. 개발자 노트 추가
4. Export assets

---

## 7. 실제 콘텐츠

### 상품 이름 (실제 사용)
```
신상품:
1. 배추김치 1kg - Cabbage Kimchi
2. 태양초 고추장 500g - Gochujang
3. 신라면 5개입 - Shin Ramyun 5pack
4. 왕교자 만두 1kg - Gyoza Dumplings
5. 깍두기 500g - Radish Kimchi
6. 햇반 12개입 - Instant Rice 12pack
7. 바나나맛 우유 6개입 - Banana Milk 6pack
8. 허니버터칩 - Honey Butter Chips

베스트:
1. 신라면 20개입 - Shin Ramyun 20pack ($28 / $32)
2. 포기김치 2kg - Whole Cabbage Kimchi ($25)
3. 순창 고추장 1kg - Sunchang Gochujang ($18)
4. 비비고 왕교자 2kg - Bibigo Gyoza 2kg ($22)
```

### Hero 문구
```
Main Title (English):
"Authentic Korean Food
Delivered to Your Door"

Subtitle (Korean):
"전세계 어디서나 즐기는 정통 한식"
```

---

## 8. Export Settings

### 개발용 Assets
```
Images:
├─ @1x (PNG)
├─ @2x (PNG, Retina)
└─ @3x (PNG, Mobile)

Icons:
├─ SVG (Vector)
└─ 24x24px, 32x32px, 48x48px

Colors:
└─ Export as CSS Variables
```

### 공유 링크
```
View Mode: Anyone with link can view
Comment: Allow comments
Dev Mode: Enable
```

---

## 9. 참고 자료

### v0.01 HTML Prototype
```
파일: 01.Web Service/005.Implementation/prototype/index_v0.01.html
브라우저에서 열어서 레이아웃 참고
```

### 설계 문서
```
- 07_사용자_화면_설계.md
- DESIGN_SYSTEM_GUIDE.md
```

### 경쟁사 참고 (영감용)
```
- H Mart: https://www.hmart.com
- Weee!: https://www.sayweee.com
- Yamibuy: https://www.yamibuy.com
```

---

## 10. 체크리스트

### Design System ✓
- [ ] Color Styles 등록 (12개)
- [ ] Text Styles 등록 (8개)
- [ ] Button Components (3종)
- [ ] Product Card Component
- [ ] Category Card Component
- [ ] Badge Components (2종)

### Desktop Design ✓
- [ ] Header (1920px width)
- [ ] Hero Banner
- [ ] Categories (6 cards)
- [ ] New Arrivals (8 products)
- [ ] Best Sellers (4 products)
- [ ] Footer

### Mobile Design ✓
- [ ] Header (375px width)
- [ ] Hero Banner (adjusted)
- [ ] Categories (2-column)
- [ ] Products (1-column)
- [ ] Footer (1-column)

### Prototype ✓
- [ ] Desktop 인터랙션
- [ ] Mobile 인터랙션
- [ ] Hover 효과
- [ ] 링크 연결

### Handoff ✓
- [ ] Component 명세
- [ ] Color/Typography 가이드
- [ ] Assets Export
- [ ] 개발자 노트

---

## 다음 단계 (v0.03)

v0.02 Figma 디자인 완료 후:
1. 디자인 리뷰 및 피드백
2. v0.03: React/Next.js 컴포넌트 개발 시작
3. Storybook 구축
4. API 연동 준비

---

**작성일**: 2025-11-19  
**버전**: v0.02  
**상태**: 준비 완료 - Figma 작업 시작 가능  
**예상 소요 시간**: 3-4시간


