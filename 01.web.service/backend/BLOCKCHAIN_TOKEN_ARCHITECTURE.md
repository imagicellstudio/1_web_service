# 블록체인 토큰 시스템 아키텍처

## 📋 개요

**목적:** 사용자 간 거래 보상 및 투명성 확보  
**작성일:** 2025-11-20  
**구현 단계:** Phase 3 (설계 완료)

---

## ⚠️ 중요: 결제 시스템과의 명확한 구분

### 결제 시스템 (PG 연동) ≠ 블록체인 토큰 시스템

| 구분 | 결제 시스템 | 블록체인 토큰 시스템 |
|------|------------|---------------------|
| **목적** | 웹서비스 상품/서비스 구매 | 사용자 간 거래 보상 |
| **기술** | PG사 API (토스, 나이스, Stripe) | 블록체인 (Ethereum/Polygon) |
| **화폐** | 법정화폐 (KRW, USD) | 플랫폼 토큰 (XLCFI Token) |
| **대상** | 플랫폼 ↔ 사용자 | 사용자 ↔ 사용자 |
| **거래 예시** | 상품 구매, 소개비 결제 | 거래 보상, 리뷰 보상, P2P 거래 |
| **현금화** | 즉시 (PG사 정산) | 추후 결정 (토큰 → 현금) |

---

## 1. 블록체인 토큰 시스템 목적

### 1.1 핵심 목표

1. **거래 투명성**
   - 모든 사용자 간 거래 이력을 블록체인에 기록
   - 위변조 불가능한 거래 증명
   - 원산지 추적 가능

2. **보상 시스템**
   - 상품 등록 보상
   - 리뷰 작성 보상
   - 거래 중개 보상
   - 추천인 보상

3. **P2P 거래**
   - 사용자 간 직거래 지원
   - 에스크로 기능 (스마트 컨트랙트)
   - 거래 수수료 최소화

4. **커뮤니티 활성화**
   - 토큰 보유량에 따른 혜택
   - 거버넌스 참여 (투표권)
   - 스테이킹 보상

---

## 2. 토큰 이코노미 설계

### 2.1 XLCFI Token 기본 정보

```yaml
Token Name: XLCFI Token
Symbol: XLCFI
Blockchain: Polygon (낮은 가스비)
Standard: ERC-20
Total Supply: 1,000,000,000 XLCFI (10억 개)
Decimals: 18
```

### 2.2 토큰 분배

```
총 발행량: 1,000,000,000 XLCFI (100%)

├─ 40% (400M) - 사용자 보상 풀
│  ├─ 거래 보상: 20%
│  ├─ 리뷰 보상: 10%
│  └─ 추천 보상: 10%
│
├─ 25% (250M) - 팀 & 개발
│  └─ 4년 베스팅 (1년 락업)
│
├─ 20% (200M) - 마케팅 & 파트너십
│  └─ 2년 베스팅
│
├─ 10% (100M) - 유동성 풀
│  └─ DEX 상장용
│
└─ 5% (50M) - 초기 에어드랍
   └─ 얼리 어답터 보상
```

### 2.3 토큰 획득 방법

| 활동 | 보상 (XLCFI) | 조건 |
|------|--------------|------|
| 회원가입 | 10 | 최초 1회 |
| 상품 등록 | 5 | 승인된 상품당 |
| 상품 판매 | 거래액의 1% | 판매 완료 시 |
| 리뷰 작성 | 2 | 구매 후 리뷰 |
| 추천인 가입 | 5 | 추천인 1명당 |
| 일일 출석 | 0.5 | 매일 1회 |
| 거래 중개 | 거래액의 0.5% | 중개 성공 시 |

### 2.4 토큰 사용처

| 사용처 | 소모 (XLCFI) | 설명 |
|--------|--------------|------|
| 프리미엄 상품 등록 | 10 | 상위 노출 |
| 광고 게재 | 50~500 | 기간별 차등 |
| 수수료 할인 | 거래액의 0.5% | 토큰 결제 시 50% 할인 |
| 프리미엄 멤버십 | 100/월 | 추가 혜택 |
| 거버넌스 투표 | 0 (보유만) | 1 XLCFI = 1 투표권 |

---

## 3. 시스템 아키텍처

### 3.1 전체 구조

```
┌─────────────────────────────────────────────────────────────┐
│                      Frontend (Next.js)                      │
│  - 토큰 지갑 연동 (MetaMask, WalletConnect)                   │
│  - 토큰 잔액 표시                                             │
│  - 거래 이력 조회                                             │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              Backend (Java Spring Boot)                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  xlcfi-blockchain-service                             │  │
│  │  - TokenService: 토큰 발행/전송                        │  │
│  │  - TransactionService: 거래 기록                       │  │
│  │  - RewardService: 보상 지급                           │  │
│  │  - WalletService: 지갑 관리                           │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  Blockchain Layer                            │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │  Polygon Network │  │  Smart Contracts │                │
│  │  - 낮은 가스비    │  │  - ERC-20 Token  │                │
│  │  - 빠른 확정      │  │  - Escrow        │                │
│  │  - Ethereum 호환  │  │  - Reward Pool   │                │
│  └──────────────────┘  └──────────────────┘                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    Database (PostgreSQL)                     │
│  - blockchain_transactions: 거래 이력                        │
│  - user_wallets: 사용자 지갑 주소                            │
│  - token_rewards: 보상 이력                                  │
│  - token_balances: 토큰 잔액 (캐시)                          │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 결제 시스템과의 통합

```
[사용자 A] ─────> [플랫폼] ─────> [사용자 B]
    │                │                │
    │ 1. 상품 구매   │                │
    │ (PG 결제)      │                │
    │ KRW 50,000     │                │
    ├────────────────┤                │
    │                │                │
    │                │ 2. 판매자에게  │
    │                │    토큰 보상   │
    │                ├────────────────┤
    │                │ +500 XLCFI     │
    │                │ (거래액의 1%)  │
    │                │                │
    │ 3. 구매자에게  │                │
    │    리뷰 보상   │                │
    ├────────────────┤                │
    │ +2 XLCFI       │                │
    │ (리뷰 작성)    │                │
```

**핵심 원칙:**
- 법정화폐 결제는 PG사 통해서만
- 토큰은 보상 및 사용자 간 거래에만 사용
- 토큰 → 현금 전환은 플랫폼이 관리

---

## 4. 스마트 컨트랙트 설계

### 4.1 XLCFIToken.sol (ERC-20)

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract XLCFIToken is ERC20, Ownable {
    
    // 총 발행량: 10억 개
    uint256 public constant TOTAL_SUPPLY = 1_000_000_000 * 10**18;
    
    // 보상 풀 주소
    address public rewardPool;
    
    constructor() ERC20("XLCFI Token", "XLCFI") {
        _mint(msg.sender, TOTAL_SUPPLY);
    }
    
    /**
     * 보상 풀 설정
     */
    function setRewardPool(address _rewardPool) external onlyOwner {
        rewardPool = _rewardPool;
    }
    
    /**
     * 보상 지급 (백엔드에서 호출)
     */
    function reward(address to, uint256 amount) external {
        require(msg.sender == rewardPool, "Only reward pool can call");
        _transfer(rewardPool, to, amount);
    }
}
```

### 4.2 Escrow.sol (P2P 거래)

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/security/ReentrancyGuard.sol";

contract Escrow is ReentrancyGuard {
    
    IERC20 public xlcfiToken;
    
    struct Trade {
        address buyer;
        address seller;
        uint256 amount;
        bool isCompleted;
        bool isCancelled;
    }
    
    mapping(uint256 => Trade) public trades;
    uint256 public tradeCounter;
    
    event TradeCreated(uint256 indexed tradeId, address buyer, address seller, uint256 amount);
    event TradeCompleted(uint256 indexed tradeId);
    event TradeCancelled(uint256 indexed tradeId);
    
    constructor(address _xlcfiToken) {
        xlcfiToken = IERC20(_xlcfiToken);
    }
    
    /**
     * 거래 생성 (구매자가 토큰 예치)
     */
    function createTrade(address seller, uint256 amount) external nonReentrant returns (uint256) {
        require(amount > 0, "Amount must be greater than 0");
        
        // 구매자로부터 토큰 전송
        require(xlcfiToken.transferFrom(msg.sender, address(this), amount), "Transfer failed");
        
        tradeCounter++;
        trades[tradeCounter] = Trade({
            buyer: msg.sender,
            seller: seller,
            amount: amount,
            isCompleted: false,
            isCancelled: false
        });
        
        emit TradeCreated(tradeCounter, msg.sender, seller, amount);
        return tradeCounter;
    }
    
    /**
     * 거래 완료 (구매자 확인 후)
     */
    function completeTrade(uint256 tradeId) external nonReentrant {
        Trade storage trade = trades[tradeId];
        require(msg.sender == trade.buyer, "Only buyer can complete");
        require(!trade.isCompleted, "Already completed");
        require(!trade.isCancelled, "Already cancelled");
        
        trade.isCompleted = true;
        
        // 판매자에게 토큰 전송
        require(xlcfiToken.transfer(trade.seller, trade.amount), "Transfer failed");
        
        emit TradeCompleted(tradeId);
    }
    
    /**
     * 거래 취소 (분쟁 시 관리자가 호출)
     */
    function cancelTrade(uint256 tradeId) external nonReentrant {
        Trade storage trade = trades[tradeId];
        require(!trade.isCompleted, "Already completed");
        require(!trade.isCancelled, "Already cancelled");
        
        trade.isCancelled = true;
        
        // 구매자에게 토큰 환불
        require(xlcfiToken.transfer(trade.buyer, trade.amount), "Transfer failed");
        
        emit TradeCancelled(tradeId);
    }
}
```

### 4.3 RewardPool.sol (보상 관리)

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";

contract RewardPool is Ownable {
    
    IERC20 public xlcfiToken;
    
    // 보상 타입별 금액
    mapping(string => uint256) public rewardAmounts;
    
    // 사용자별 보상 이력
    mapping(address => uint256) public totalRewards;
    
    event RewardDistributed(address indexed user, string rewardType, uint256 amount);
    
    constructor(address _xlcfiToken) {
        xlcfiToken = IERC20(_xlcfiToken);
        
        // 기본 보상 금액 설정
        rewardAmounts["SIGNUP"] = 10 * 10**18;
        rewardAmounts["PRODUCT_REGISTER"] = 5 * 10**18;
        rewardAmounts["REVIEW"] = 2 * 10**18;
        rewardAmounts["REFERRAL"] = 5 * 10**18;
        rewardAmounts["DAILY_CHECKIN"] = 0.5 * 10**18;
    }
    
    /**
     * 보상 지급 (백엔드에서 호출)
     */
    function distributeReward(address user, string memory rewardType) external onlyOwner {
        uint256 amount = rewardAmounts[rewardType];
        require(amount > 0, "Invalid reward type");
        
        require(xlcfiToken.transfer(user, amount), "Transfer failed");
        
        totalRewards[user] += amount;
        
        emit RewardDistributed(user, rewardType, amount);
    }
    
    /**
     * 보상 금액 업데이트
     */
    function updateRewardAmount(string memory rewardType, uint256 amount) external onlyOwner {
        rewardAmounts[rewardType] = amount;
    }
}
```

---

## 5. 데이터베이스 스키마

### 5.1 blockchain_transactions

```sql
CREATE TABLE blockchain_transactions (
    id BIGSERIAL PRIMARY KEY,
    
    -- 거래 정보
    transaction_hash VARCHAR(66) NOT NULL UNIQUE,
    block_number BIGINT,
    
    -- 사용자 정보
    from_address VARCHAR(42) NOT NULL,
    to_address VARCHAR(42) NOT NULL,
    from_user_id BIGINT,
    to_user_id BIGINT,
    
    -- 토큰 정보
    token_amount DECIMAL(30, 18) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL, -- REWARD, TRANSFER, ESCROW
    
    -- 메타데이터
    description TEXT,
    metadata JSONB,
    
    -- 상태
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, FAILED
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    confirmed_at TIMESTAMP,
    
    -- 인덱스
    INDEX idx_from_address (from_address),
    INDEX idx_to_address (to_address),
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_tx_hash (transaction_hash),
    INDEX idx_created_at (created_at)
);
```

### 5.2 user_wallets

```sql
CREATE TABLE user_wallets (
    id BIGSERIAL PRIMARY KEY,
    
    -- 사용자 정보
    user_id BIGINT NOT NULL UNIQUE,
    
    -- 지갑 주소
    wallet_address VARCHAR(42) NOT NULL UNIQUE,
    
    -- 잔액 (캐시)
    balance DECIMAL(30, 18) NOT NULL DEFAULT 0,
    
    -- 상태
    is_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_synced_at TIMESTAMP,
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 5.3 token_rewards

```sql
CREATE TABLE token_rewards (
    id BIGSERIAL PRIMARY KEY,
    
    -- 사용자 정보
    user_id BIGINT NOT NULL,
    
    -- 보상 정보
    reward_type VARCHAR(50) NOT NULL, -- SIGNUP, PRODUCT_REGISTER, REVIEW, etc.
    amount DECIMAL(30, 18) NOT NULL,
    
    -- 관련 엔티티
    related_entity_type VARCHAR(50), -- PRODUCT, ORDER, REVIEW
    related_entity_id BIGINT,
    
    -- 블록체인 정보
    transaction_hash VARCHAR(66),
    
    -- 상태
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, DISTRIBUTED, FAILED
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    distributed_at TIMESTAMP,
    
    -- 인덱스
    INDEX idx_user_id (user_id),
    INDEX idx_reward_type (reward_type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 6. 토큰 현금화 전략

### 6.1 옵션 1: 플랫폼 직접 환전

**장점:**
- 사용자 편의성 최고
- 즉시 현금화 가능
- 플랫폼 통제 가능

**단점:**
- 플랫폼 유동성 부담
- 법적 규제 리스크 (금융업 해당 가능)
- 환전 수수료 부담

**구현:**
```
사용자 → 플랫폼 환전 요청
       → 플랫폼이 토큰 회수
       → 은행 계좌로 KRW 송금
       → 수수료: 5~10%
```

### 6.2 옵션 2: DEX 상장 (탈중앙화 거래소)

**장점:**
- 플랫폼 부담 없음
- 시장 가격 형성
- 글로벌 거래 가능

**단점:**
- 가격 변동성
- 사용자 학습 곡선
- 유동성 확보 필요

**구현:**
```
XLCFI Token → Uniswap/Sushiswap 상장
            → 유동성 풀 제공 (XLCFI/USDT)
            → 사용자가 직접 스왑
            → 수수료: 0.3% (DEX 수수료)
```

### 6.3 옵션 3: 제3자 환전 서비스 연동

**장점:**
- 플랫폼 리스크 최소화
- 전문 업체 활용
- 법적 책임 분산

**단점:**
- 수수료 높음
- 서비스 의존성
- 사용자 경험 저하

**구현:**
```
사용자 → 제3자 환전 서비스 (예: 업비트, 빗썸)
       → 토큰 입금
       → KRW 출금
       → 수수료: 10~15%
```

### 6.4 옵션 4: 스테이블 코인 연동

**장점:**
- 가격 안정성
- 글로벌 사용 가능
- 빠른 전환

**단점:**
- 스테이블 코인 리스크
- 추가 전환 단계
- 규제 불확실성

**구현:**
```
XLCFI Token → USDT/USDC 스왑
            → 스테이블 코인 보유
            → 필요 시 현금화
            → 수수료: 1~3%
```

### 6.5 권장 전략 (하이브리드)

**Phase 1 (초기):**
- DEX 상장 (Uniswap on Polygon)
- 최소 유동성 제공
- 사용자 직접 스왑

**Phase 2 (성장기):**
- 플랫폼 환전 서비스 시범 운영
- 일정 금액 이상만 환전 가능
- 수수료: 7%

**Phase 3 (성숙기):**
- 스테이블 코인 자동 스왑
- 제3자 환전 서비스 연동
- 다양한 옵션 제공

---

## 7. 구현 로드맵

### Phase 1: 기본 인프라 (4주)

**Week 1-2: 스마트 컨트랙트**
- [ ] XLCFIToken.sol 개발
- [ ] Escrow.sol 개발
- [ ] RewardPool.sol 개발
- [ ] 테스트넷 배포 (Mumbai)

**Week 3-4: 백엔드 연동**
- [ ] xlcfi-blockchain-service 모듈 생성
- [ ] Web3j 연동
- [ ] TokenService 구현
- [ ] WalletService 구현

### Phase 2: 보상 시스템 (3주)

**Week 5-6: 보상 로직**
- [ ] RewardService 구현
- [ ] 이벤트 리스너 (상품 등록, 리뷰 등)
- [ ] 자동 보상 지급

**Week 7: 테스트**
- [ ] 통합 테스트
- [ ] 보안 감사

### Phase 3: P2P 거래 (2주)

**Week 8-9: 에스크로**
- [ ] TransactionService 구현
- [ ] 에스크로 UI
- [ ] 분쟁 해결 프로세스

### Phase 4: 현금화 (2주)

**Week 10-11: DEX 상장**
- [ ] Uniswap 유동성 풀 생성
- [ ] 프론트엔드 스왑 UI
- [ ] 가격 모니터링

---

## 8. 보안 고려사항

### 8.1 스마트 컨트랙트 보안

- ✅ OpenZeppelin 라이브러리 사용
- ✅ ReentrancyGuard 적용
- ✅ Access Control (Ownable)
- ✅ 외부 감사 필수

### 8.2 백엔드 보안

- ✅ Private Key 안전 보관 (AWS KMS)
- ✅ 트랜잭션 서명 분리
- ✅ Rate Limiting
- ✅ 이상 거래 탐지

### 8.3 사용자 보안

- ✅ 지갑 연동 (MetaMask)
- ✅ 2FA 인증
- ✅ 출금 한도 설정
- ✅ 의심 거래 알림

---

## 9. 결론

### ✅ 설계 완료

**블록체인 토큰 시스템**이 **결제 시스템과 명확히 구분**되어 설계되었습니다.

**핵심 원칙:**
1. 결제는 PG사 (법정화폐)
2. 토큰은 보상 및 P2P 거래
3. 현금화는 플랫폼이 관리

**다음 단계:**
- Phase 3에서 구현 시작
- 스마트 컨트랙트 개발
- 백엔드 블록체인 서비스 구현

---

**작성일:** 2025-11-20  
**작성자:** AI Assistant  
**구현 단계:** Phase 3 (설계 완료, 구현 대기)



