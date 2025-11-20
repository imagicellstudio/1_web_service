# SpicyJump Homepage Prototype v0.01

## 개요
HTML/CSS/JavaScript로 구현한 SpicyJump 메인 화면 프로토타입입니다.

## 파일 구조
```
prototype/
├── index_v0.01.html          # 메인 HTML (브라우저에서 이 파일 열기)
├── styles/
│   └── main_v0.01.css        # 스타일시트
├── scripts/
│   └── main_v0.01.js         # 인터랙션 JavaScript
├── VERSION_HISTORY.md        # 버전 히스토리
└── README_v0.01.md          # 이 파일
```

## 실행 방법

### 방법 1: 직접 열기 (추천)
1. `index_v0.01.html` 파일을 더블클릭
2. 또는 Chrome/Edge로 드래그 앤 드롭

### 방법 2: Live Server
```bash
# Cursor/VSCode에서
# Alt + L + O 또는 "Go Live" 버튼 클릭
```

### 방법 3: Python 서버
```bash
cd "01.Web Service/005.Implementation/prototype"
python -m http.server 8000
# http://localhost:8000/index_v0.01.html 접속
```

## 반응형 테스트
브라우저에서 **F12** → **Ctrl+Shift+M** (반응형 모드)

### 테스트 권장 사이즈
- 📱 Mobile: 375px (iPhone SE)
- 📱 Mobile L: 428px (iPhone 14 Pro Max)
- 📲 Tablet: 768px (iPad)
- 💻 Desktop: 1920px

## 구현 기능

### ✅ 완료된 기능
- [x] 반응형 레이아웃 (Desktop/Tablet/Mobile)
- [x] Header (로고, 검색, 카테고리, 장바구니)
- [x] Hero Banner
- [x] 카테고리 6개
- [x] 신상품 8개 (NEW 뱃지)
- [x] 베스트 상품 4개 (BEST 뱃지, 할인가)
- [x] Footer (4단 컬럼, Newsletter)
- [x] 장바구니 담기 애니메이션
- [x] Hover 효과 (카드 상승)
- [x] Smooth Scrolling

### 📝 미구현 (추후 버전)
- [ ] 실제 이미지 (현재 이모지 사용)
- [ ] 모바일 메뉴 드로어
- [ ] 검색 기능
- [ ] 페이지네이션
- [ ] 필터링
- [ ] API 연동

## 디자인 시스템

### 컬러
```css
Primary:   #E63946 (Spicy Red)
Secondary: #F1FAEE (Light)
Accent:    #A8DADC (Blue)
Dark:      #1D3557 (Navy)
Gray:      #6C757D
```

### 타이포그래피
- 한글: Noto Sans KR
- 영문: Inter
- H1: 48px (Mobile: 32px)
- Body: 16px

### 간격
- xs: 4px
- sm: 8px
- md: 16px
- lg: 24px
- xl: 32px
- xxl: 48px

## 기술 스택
- HTML5
- CSS3 (Grid, Flexbox, CSS Variables)
- Vanilla JavaScript
- Google Fonts

## 브라우저 호환성
- ✅ Chrome 90+
- ✅ Edge 90+
- ✅ Firefox 88+
- ✅ Safari 14+

## 다음 단계 (v0.02)
1. Figma로 고품질 디자인 작성
2. 실제 이미지 적용
3. 컴포넌트 라이브러리 구축
4. 인터랙션 프로토타입
5. 개발자 핸드오프 준비

## 참고 문서
- 설계 문서: `01.Web Service/002.Design/`
- 화면 설계서: `07_사용자_화면_설계.md`
- 디자인 시스템: `DESIGN_SYSTEM_GUIDE.md`

---

**작성일**: 2025-11-19  
**버전**: v0.01  
**다음 버전**: v0.02 (Figma Design)


