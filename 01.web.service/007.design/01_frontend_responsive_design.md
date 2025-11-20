# Frontend 반응형 설계 가이드

## 문서 정보
- 작성일: 2025-11-19
- 버전: 2.0 (플랫폼)
- 대상: Web, Tablet, Mobile

---

## 1. 반응형 디자인 전략

### 1.1 Breakpoints 정의

```scss
// _breakpoints.scss
$breakpoints: (
  'mobile': (
    'min': 320px,
    'max': 767px
  ),
  'tablet': (
    'min': 768px,
    'max': 1023px
  ),
  'desktop': (
    'min': 1024px,
    'max': 1920px
  ),
  'wide': (
    'min': 1921px
  )
);

// 미디어 쿼리 Mixin
@mixin respond-to($breakpoint) {
  @if $breakpoint == 'mobile' {
    @media (max-width: 767px) { @content; }
  }
  @else if $breakpoint == 'tablet' {
    @media (min-width: 768px) and (max-width: 1023px) { @content; }
  }
  @else if $breakpoint == 'desktop' {
    @media (min-width: 1024px) { @content; }
  }
}
```

### 1.2 Mobile First 접근

```tsx
// 기본 스타일은 모바일을 위해 작성
// 큰 화면으로 갈수록 확장

const Container = styled.div`
  // Mobile (기본)
  padding: 16px;
  font-size: 14px;
  
  // Tablet
  @media (min-width: 768px) {
    padding: 24px;
    font-size: 16px;
  }
  
  // Desktop
  @media (min-width: 1024px) {
    padding: 32px;
    max-width: 1200px;
    margin: 0 auto;
    font-size: 16px;
  }
`;
```

---

## 2. 컴포넌트 구조

### 2.1 Atomic Design 체계

```
/components
  /atoms              # 기본 요소
    Button.tsx
    Input.tsx
    Label.tsx
    Icon.tsx
    Badge.tsx
    
  /molecules          # 조합 요소
    SearchBar.tsx
    ProductCard.tsx
    UserMenu.tsx
    LanguageSelector.tsx
    
  /organisms          # 복합 컴포넌트
    Header.tsx
    Navigation.tsx
    ProductList.tsx
    FilterPanel.tsx
    Cart.tsx
    
  /templates          # 페이지 템플릿
    MainTemplate.tsx
    ProductTemplate.tsx
    AdminTemplate.tsx
    
  /pages              # 실제 페이지
    Home.tsx
    Products.tsx
    ProductDetail.tsx
    Cart.tsx
    Checkout.tsx
```

### 2.2 반응형 컴포넌트 예시

#### Header 컴포넌트
```tsx
// Header.tsx
import { useMediaQuery } from '@/hooks/useMediaQuery';

export const Header: React.FC = () => {
  const isMobile = useMediaQuery('(max-width: 767px)');
  const isTablet = useMediaQuery('(min-width: 768px) and (max-width: 1023px)');
  
  return (
    <HeaderContainer>
      {isMobile && <MobileHeader />}
      {isTablet && <TabletHeader />}
      {!isMobile && !isTablet && <DesktopHeader />}
    </HeaderContainer>
  );
};

// MobileHeader.tsx
const MobileHeader: React.FC = () => {
  const [menuOpen, setMenuOpen] = useState(false);
  
  return (
    <MobileHeaderContainer>
      <HamburgerButton onClick={() => setMenuOpen(!menuOpen)} />
      <Logo />
      <CartIcon />
      
      {menuOpen && (
        <MobileMenu>
          <Navigation />
          <LanguageSelector />
          <UserMenu />
        </MobileMenu>
      )}
    </MobileHeaderContainer>
  );
};

// TabletHeader.tsx
const TabletHeader: React.FC = () => {
  return (
    <TabletHeaderContainer>
      <Logo />
      <SearchBar />
      <UserMenu />
      <CartIcon />
    </TabletHeaderContainer>
  );
};

// DesktopHeader.tsx
const DesktopHeader: React.FC = () => {
  return (
    <DesktopHeaderContainer>
      <TopBar>
        <LanguageSelector />
        <UserMenu />
      </TopBar>
      <MainBar>
        <Logo />
        <Navigation />
        <SearchBar />
        <CartIcon />
      </MainBar>
    </DesktopHeaderContainer>
  );
};
```

---

## 3. 소통형 기능 컴포넌트

### 3.1 실시간 채팅

```tsx
// ChatComponent.tsx
interface ChatComponentProps {
  type: '1:1' | 'group' | 'support';
  roomId: string;
  translation?: boolean;
}

export const ChatComponent: React.FC<ChatComponentProps> = ({
  type,
  roomId,
  translation = false
}) => {
  const ws = useWebSocket(`/ws/chat/${roomId}`);
  const { t, i18n } = useTranslation();
  const [messages, setMessages] = useState<Message[]>([]);
  
  useEffect(() => {
    ws.on('message', async (message) => {
      // 자동 번역 옵션
      if (translation && message.lang !== i18n.language) {
        message.translatedText = await translateMessage(
          message.text,
          message.lang,
          i18n.language
        );
      }
      setMessages(prev => [...prev, message]);
    });
  }, [ws]);
  
  return (
    <ChatContainer>
      <ChatHeader type={type} />
      <MessageList messages={messages} />
      <ChatInput onSend={sendMessage} />
    </ChatContainer>
  );
};

// 반응형 스타일
const ChatContainer = styled.div`
  display: flex;
  flex-direction: column;
  height: 100%;
  
  // Mobile
  @media (max-width: 767px) {
    height: calc(100vh - 60px);
  }
  
  // Tablet
  @media (min-width: 768px) and (max-width: 1023px) {
    height: 500px;
    border-radius: 12px;
  }
  
  // Desktop
  @media (min-width: 1024px) {
    height: 600px;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  }
`;
```

### 3.2 라이브 스트리밍

```tsx
// LiveStreamComponent.tsx
export const LiveStreamComponent: React.FC = () => {
  const [isLive, setIsLive] = useState(false);
  const [viewers, setViewers] = useState(0);
  const videoRef = useRef<HTMLVideoElement>(null);
  
  return (
    <LiveStreamContainer>
      <VideoPlayer ref={videoRef}>
        {isLive && (
          <LiveBadge>
            <RedDot /> LIVE · {viewers} 시청 중
          </LiveBadge>
        )}
      </VideoPlayer>
      
      <InteractionPanel>
        <ChatSection />
        <ProductLinkSection />
        <ReactionButtons />
      </InteractionPanel>
    </LiveStreamContainer>
  );
};

// 반응형 레이아웃
const LiveStreamContainer = styled.div`
  // Mobile - 세로 배치
  @media (max-width: 767px) {
    display: flex;
    flex-direction: column;
    
    ${VideoPlayer} {
      width: 100%;
      aspect-ratio: 16/9;
    }
    
    ${InteractionPanel} {
      width: 100%;
      height: 300px;
    }
  }
  
  // Tablet & Desktop - 가로 배치
  @media (min-width: 768px) {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 16px;
    
    ${VideoPlayer} {
      aspect-ratio: 16/9;
    }
    
    ${InteractionPanel} {
      height: 100%;
    }
  }
`;
```

### 3.3 커뮤니티 (포럼/Q&A)

```tsx
// CommunityComponent.tsx
export const CommunityComponent: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'forum' | 'qna' | 'review'>('forum');
  
  return (
    <CommunityContainer>
      <Tabs>
        <Tab active={activeTab === 'forum'} onClick={() => setActiveTab('forum')}>
          포럼
        </Tab>
        <Tab active={activeTab === 'qna'} onClick={() => setActiveTab('qna')}>
          Q&A
        </Tab>
        <Tab active={activeTab === 'review'} onClick={() => setActiveTab('review')}>
          리뷰
        </Tab>
      </Tabs>
      
      {activeTab === 'forum' && <ForumList />}
      {activeTab === 'qna' && <QnAList />}
      {activeTab === 'review' && <ReviewList />}
    </CommunityContainer>
  );
};
```

### 3.4 알림 시스템

```tsx
// NotificationComponent.tsx
export const NotificationComponent: React.FC = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  
  // WebSocket으로 실시간 알림
  useEffect(() => {
    const ws = new WebSocket('/ws/notifications');
    
    ws.onmessage = (event) => {
      const notification = JSON.parse(event.data);
      setNotifications(prev => [notification, ...prev]);
      setUnreadCount(prev => prev + 1);
      
      // 푸시 알림
      if ('Notification' in window && Notification.permission === 'granted') {
        new Notification(notification.title, {
          body: notification.message,
          icon: '/logo.png'
        });
      }
    };
    
    return () => ws.close();
  }, []);
  
  return (
    <NotificationBell>
      <BellIcon />
      {unreadCount > 0 && <Badge>{unreadCount}</Badge>}
      
      <NotificationDropdown>
        {notifications.map(notif => (
          <NotificationItem key={notif.id}>
            <NotificationIcon type={notif.type} />
            <NotificationContent>
              <NotificationTitle>{notif.title}</NotificationTitle>
              <NotificationMessage>{notif.message}</NotificationMessage>
              <NotificationTime>{formatTime(notif.createdAt)}</NotificationTime>
            </NotificationContent>
          </NotificationItem>
        ))}
      </NotificationDropdown>
    </NotificationBell>
  );
};
```

---

## 4. 상품 관련 컴포넌트

### 4.1 상품 카드 (반응형)

```tsx
// ProductCard.tsx
export const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const isMobile = useMediaQuery('(max-width: 767px)');
  
  return (
    <Card>
      <ProductImage src={product.image} alt={product.name} />
      
      {/* 라벨 표시 */}
      <LabelContainer>
        {product.haccp && <HACCPBadge />}
        {product.labels.map(label => (
          <Label key={label.id}>{label.name}</Label>
        ))}
      </LabelContainer>
      
      <ProductInfo>
        <ProductName>{product.name}</ProductName>
        <ProductOrigin>원산지: {product.origin}</ProductOrigin>
        
        {!isMobile && (
          <ProductDescription>{product.description}</ProductDescription>
        )}
        
        <PriceContainer>
          <Price>{formatCurrency(product.price)}</Price>
          {product.discount && (
            <DiscountBadge>{product.discount}% OFF</DiscountBadge>
          )}
        </PriceContainer>
        
        <Rating value={product.rating} reviews={product.reviewCount} />
        
        <ButtonGroup>
          <CartButton onClick={() => addToCart(product)}>
            장바구니
          </CartButton>
          <BuyButton onClick={() => buyNow(product)}>
            구매하기
          </BuyButton>
        </ButtonGroup>
      </ProductInfo>
    </Card>
  );
};

// 반응형 스타일
const Card = styled.div`
  // Mobile
  @media (max-width: 767px) {
    display: flex;
    padding: 12px;
    border-bottom: 1px solid #eee;
    
    ${ProductImage} {
      width: 100px;
      height: 100px;
      margin-right: 12px;
    }
    
    ${ProductInfo} {
      flex: 1;
    }
    
    ${ButtonGroup} {
      flex-direction: column;
      gap: 8px;
    }
  }
  
  // Tablet & Desktop
  @media (min-width: 768px) {
    display: flex;
    flex-direction: column;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    overflow: hidden;
    transition: transform 0.2s;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 16px rgba(0,0,0,0.15);
    }
    
    ${ProductImage} {
      width: 100%;
      aspect-ratio: 1;
      object-fit: cover;
    }
    
    ${ProductInfo} {
      padding: 16px;
    }
    
    ${ButtonGroup} {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 8px;
    }
  }
`;
```

### 4.2 상품 리스트 (그리드)

```tsx
// ProductList.tsx
export const ProductList: React.FC<ProductListProps> = ({ products }) => {
  const isMobile = useMediaQuery('(max-width: 767px)');
  const isTablet = useMediaQuery('(min-width: 768px) and (max-width: 1023px)');
  
  const columns = isMobile ? 1 : isTablet ? 2 : 4;
  
  return (
    <ProductGrid columns={columns}>
      {products.map(product => (
        <ProductCard key={product.id} product={product} />
      ))}
    </ProductGrid>
  );
};

const ProductGrid = styled.div<{ columns: number }>`
  display: grid;
  grid-template-columns: repeat(${props => props.columns}, 1fr);
  gap: ${props => props.columns === 1 ? '0' : '16px'};
  
  @media (min-width: 1024px) {
    gap: 24px;
  }
`;
```

### 4.3 상품 필터 패널

```tsx
// FilterPanel.tsx
export const FilterPanel: React.FC = () => {
  const isMobile = useMediaQuery('(max-width: 767px)');
  const [isOpen, setIsOpen] = useState(!isMobile);
  
  return (
    <>
      {isMobile && (
        <FilterButton onClick={() => setIsOpen(true)}>
          <FilterIcon /> 필터
        </FilterButton>
      )}
      
      <FilterContainer isOpen={isOpen} isMobile={isMobile}>
        {isMobile && (
          <FilterHeader>
            <h3>필터</h3>
            <CloseButton onClick={() => setIsOpen(false)} />
          </FilterHeader>
        )}
        
        <FilterSection>
          <h4>카테고리</h4>
          <CategoryList />
        </FilterSection>
        
        <FilterSection>
          <h4>가격대</h4>
          <PriceRangeSlider />
        </FilterSection>
        
        <FilterSection>
          <h4>원산지</h4>
          <OriginCheckboxes />
        </FilterSection>
        
        <FilterSection>
          <h4>인증</h4>
          <Checkbox label="HACCP" />
          <Checkbox label="유기농" />
          <Checkbox label="친환경" />
        </FilterSection>
        
        <FilterSection>
          <h4>평점</h4>
          <RatingFilter />
        </FilterSection>
        
        <ApplyButton>적용</ApplyButton>
      </FilterContainer>
    </>
  );
};

const FilterContainer = styled.div<{ isOpen: boolean; isMobile: boolean }>`
  // Mobile - 하단 슬라이드업
  @media (max-width: 767px) {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background: white;
    border-radius: 20px 20px 0 0;
    padding: 20px;
    max-height: 80vh;
    overflow-y: auto;
    transform: translateY(${props => props.isOpen ? '0' : '100%'});
    transition: transform 0.3s ease;
    z-index: 1000;
  }
  
  // Tablet & Desktop - 사이드바
  @media (min-width: 768px) {
    position: sticky;
    top: 80px;
    width: 250px;
    padding: 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  }
`;
```

---

## 5. 장바구니 & 결제

### 5.1 장바구니

```tsx
// Cart.tsx
export const Cart: React.FC = () => {
  const [items, setItems] = useState<CartItem[]>([]);
  const isMobile = useMediaQuery('(max-width: 767px)');
  
  const total = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  
  return (
    <CartContainer>
      <CartHeader>
        <h2>장바구니</h2>
        <ItemCount>{items.length}개 상품</ItemCount>
      </CartHeader>
      
      <CartItemList>
        {items.map(item => (
          <CartItemRow key={item.id} isMobile={isMobile}>
            <ItemImage src={item.image} />
            <ItemInfo>
              <ItemName>{item.name}</ItemName>
              <ItemOptions>{item.options}</ItemOptions>
              <ItemPrice>{formatCurrency(item.price)}</ItemPrice>
            </ItemInfo>
            <QuantityControl>
              <button onClick={() => decreaseQuantity(item.id)}>-</button>
              <span>{item.quantity}</span>
              <button onClick={() => increaseQuantity(item.id)}>+</button>
            </QuantityControl>
            {!isMobile && (
              <ItemTotal>{formatCurrency(item.price * item.quantity)}</ItemTotal>
            )}
            <DeleteButton onClick={() => removeItem(item.id)}>
              <TrashIcon />
            </DeleteButton>
          </CartItemRow>
        ))}
      </CartItemList>
      
      <CartSummary>
        <SummaryRow>
          <span>상품 금액</span>
          <span>{formatCurrency(total)}</span>
        </SummaryRow>
        <SummaryRow>
          <span>배송비</span>
          <span>{formatCurrency(3000)}</span>
        </SummaryRow>
        <Divider />
        <TotalRow>
          <span>총 결제 금액</span>
          <TotalAmount>{formatCurrency(total + 3000)}</TotalAmount>
        </TotalRow>
        
        <CheckoutButton onClick={() => navigate('/checkout')}>
          결제하기
        </CheckoutButton>
      </CartSummary>
    </CartContainer>
  );
};
```

### 5.2 결제 페이지

```tsx
// Checkout.tsx
export const Checkout: React.FC = () => {
  const [step, setStep] = useState<1 | 2 | 3>(1);
  
  return (
    <CheckoutContainer>
      <ProgressBar>
        <Step active={step >= 1}>배송 정보</Step>
        <Step active={step >= 2}>결제 수단</Step>
        <Step active={step >= 3}>주문 완료</Step>
      </ProgressBar>
      
      {step === 1 && <ShippingForm onNext={() => setStep(2)} />}
      {step === 2 && <PaymentForm onNext={() => setStep(3)} />}
      {step === 3 && <OrderComplete />}
    </CheckoutContainer>
  );
};
```

---

## 6. 다국어 지원

### 6.1 언어 선택기

```tsx
// LanguageSelector.tsx
export const LanguageSelector: React.FC = () => {
  const { i18n } = useTranslation();
  const [isOpen, setIsOpen] = useState(false);
  
  const languages = [
    { code: 'ko', name: '한국어', flag: '🇰🇷' },
    { code: 'en', name: 'English', flag: '🇺🇸' },
    { code: 'zh', name: '中文', flag: '🇨🇳' },
    { code: 'ja', name: '日本語', flag: '🇯🇵' }
  ];
  
  const currentLang = languages.find(lang => lang.code === i18n.language);
  
  return (
    <LanguageSelectorContainer>
      <CurrentLang onClick={() => setIsOpen(!isOpen)}>
        <span>{currentLang?.flag}</span>
        <span>{currentLang?.name}</span>
        <ChevronIcon />
      </CurrentLang>
      
      {isOpen && (
        <LanguageDropdown>
          {languages.map(lang => (
            <LanguageOption
              key={lang.code}
              active={lang.code === i18n.language}
              onClick={() => {
                i18n.changeLanguage(lang.code);
                setIsOpen(false);
              }}
            >
              <span>{lang.flag}</span>
              <span>{lang.name}</span>
              {lang.code === i18n.language && <CheckIcon />}
            </LanguageOption>
          ))}
        </LanguageDropdown>
      )}
    </LanguageSelectorContainer>
  );
};
```

---

## 7. 성능 최적화

### 7.1 이미지 최적화

```tsx
// OptimizedImage.tsx
export const OptimizedImage: React.FC<ImageProps> = ({ src, alt, sizes }) => {
  return (
    <picture>
      <source
        media="(max-width: 767px)"
        srcSet={`${src}?w=400 1x, ${src}?w=800 2x`}
      />
      <source
        media="(min-width: 768px) and (max-width: 1023px)"
        srcSet={`${src}?w=800 1x, ${src}?w=1600 2x`}
      />
      <source
        media="(min-width: 1024px)"
        srcSet={`${src}?w=1200 1x, ${src}?w=2400 2x`}
      />
      <img src={src} alt={alt} loading="lazy" />
    </picture>
  );
};
```

### 7.2 Code Splitting

```tsx
// App.tsx
import { lazy, Suspense } from 'react';

const Home = lazy(() => import('./pages/Home'));
const Products = lazy(() => import('./pages/Products'));
const ProductDetail = lazy(() => import('./pages/ProductDetail'));
const Cart = lazy(() => import('./pages/Cart'));
const Checkout = lazy(() => import('./pages/Checkout'));

export const App: React.FC = () => {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/products" element={<Products />} />
        <Route path="/products/:id" element={<ProductDetail />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/checkout" element={<Checkout />} />
      </Routes>
    </Suspense>
  );
};
```

---

**문서 관리**
- 작성자: [담당자명]
- 최종 업데이트: 2025-11-19


