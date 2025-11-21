# 평가관리 및 CMS 시스템 설계

## 문서 정보
- 작성일: 2025-11-19
- 버전: 2.0 (플랫폼)
- 대상: 평가, 댓글, 콘텐츠 관리 시스템

---

## 1. 평가관리 시스템 개요

### 1.1 핵심 기능
- **감정평가**: 좋아요, 싫어요, 도움됨, 공감 등
- **리뷰/댓글**: 상품 리뷰, 게시글 댓글, 대댓글
- **평점**: 5점 척도, 세부 항목별 평점
- **CMS**: 게시글 작성, 편집, 이미지 업로드
- **연계**: 라벨링/리워드, 거래(블록체인), 결제와 데이터 매핑

### 1.2 시스템 구조

```
┌──────────────────────────────────────────────────────────┐
│                    Frontend Layer                         │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐         │
│  │ 리뷰 작성  │  │ 댓글 시스템 │  │ 게시글 작성 │         │
│  │ 감정평가   │  │ 대댓글      │  │ 이미지편집  │         │
│  └────────────┘  └────────────┘  └────────────┘         │
└──────────────────────────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────┐
│                   Backend API Layer                       │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐         │
│  │ 평가 API   │  │ 댓글 API   │  │ CMS API    │         │
│  │ 감정 API   │  │ 알림 API   │  │ 미디어 API │         │
│  └────────────┘  └────────────┘  └────────────┘         │
└──────────────────────────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────┐
│                   Data Mapping Layer                      │
│  리뷰 ←→ 거래(블록체인) ←→ 결제 ←→ 리워드               │
│  평가 ←→ 라벨링 ←→ AI 학습 데이터                       │
└──────────────────────────────────────────────────────────┘
                          ↓
┌──────────────────────────────────────────────────────────┐
│                    Database Layer                         │
│  reviews, comments, reactions, posts, media              │
└──────────────────────────────────────────────────────────┘
```

---

## 2. 데이터베이스 스키마

### 2.1 리뷰 및 평가 테이블

```sql
-- 리뷰 테이블 (확장)
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    order_id INTEGER REFERENCES orders(id) ON DELETE SET NULL,
    transaction_id INTEGER REFERENCES transactions(id), -- 거래 연결
    payment_id INTEGER REFERENCES payments(id), -- 결제 연결
    
    -- 평점
    rating_overall INTEGER CHECK (rating_overall >= 1 AND rating_overall <= 5),
    rating_quality INTEGER CHECK (rating_quality >= 1 AND rating_quality <= 5),
    rating_delivery INTEGER CHECK (rating_delivery >= 1 AND rating_delivery <= 5),
    rating_packaging INTEGER CHECK (rating_packaging >= 1 AND rating_packaging <= 5),
    
    -- 내용
    title VARCHAR(200),
    content TEXT NOT NULL,
    pros TEXT, -- 장점
    cons TEXT, -- 단점
    
    -- 미디어
    images JSONB, -- [{url, caption, order}]
    videos JSONB, -- [{url, thumbnail, duration}]
    
    -- 메타
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    is_featured BOOLEAN DEFAULT FALSE, -- 베스트 리뷰
    helpful_count INTEGER DEFAULT 0,
    unhelpful_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    view_count INTEGER DEFAULT 0,
    
    -- 상태
    status VARCHAR(20) DEFAULT 'published', -- draft, published, hidden, flagged, deleted
    moderation_status VARCHAR(20) DEFAULT 'pending', -- pending, approved, rejected
    moderation_reason TEXT,
    moderated_by INTEGER REFERENCES users(id),
    moderated_at TIMESTAMP,
    
    -- 리워드 연결
    reward_points_earned INTEGER DEFAULT 0,
    reward_transaction_id INTEGER,
    
    -- 블록체인 연결
    blockchain_hash VARCHAR(255), -- 리뷰 위변조 방지
    blockchain_verified BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_transaction_id ON reviews(transaction_id);
CREATE INDEX idx_reviews_status ON reviews(status);
CREATE INDEX idx_reviews_rating ON reviews(rating_overall DESC);
CREATE INDEX idx_reviews_created_at ON reviews(created_at DESC);

-- 댓글 테이블
CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    parent_id INTEGER REFERENCES comments(id) ON DELETE CASCADE, -- 대댓글
    target_type VARCHAR(50) NOT NULL, -- review, post, product
    target_id INTEGER NOT NULL,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    
    content TEXT NOT NULL,
    images JSONB, -- 댓글에 이미지 첨부
    
    -- 감정평가
    like_count INTEGER DEFAULT 0,
    dislike_count INTEGER DEFAULT 0,
    
    -- 상태
    status VARCHAR(20) DEFAULT 'published',
    is_pinned BOOLEAN DEFAULT FALSE, -- 고정 댓글
    is_author_reply BOOLEAN DEFAULT FALSE, -- 작성자 답글
    
    -- 블록체인
    blockchain_hash VARCHAR(255),
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_comments_target ON comments(target_type, target_id);
CREATE INDEX idx_comments_user_id ON comments(user_id);
CREATE INDEX idx_comments_parent_id ON comments(parent_id);
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);

-- 감정평가 (Reactions)
CREATE TABLE reactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    target_type VARCHAR(50) NOT NULL, -- review, comment, post
    target_id INTEGER NOT NULL,
    reaction_type VARCHAR(50) NOT NULL, -- like, love, helpful, funny, angry, sad
    
    created_at TIMESTAMP DEFAULT NOW(),
    
    UNIQUE(user_id, target_type, target_id, reaction_type)
);

CREATE INDEX idx_reactions_target ON reactions(target_type, target_id);
CREATE INDEX idx_reactions_user_id ON reactions(user_id);
CREATE INDEX idx_reactions_type ON reactions(reaction_type);

-- 리뷰 도움됨 투표
CREATE TABLE review_helpfulness (
    review_id INTEGER REFERENCES reviews(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    is_helpful BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (review_id, user_id)
);

-- 게시글 (CMS)
CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES post_categories(id),
    
    title VARCHAR(500) NOT NULL,
    slug VARCHAR(500) UNIQUE,
    content TEXT NOT NULL,
    excerpt TEXT, -- 요약
    
    -- 미디어
    featured_image VARCHAR(500),
    images JSONB,
    videos JSONB,
    attachments JSONB, -- 첨부파일
    
    -- SEO
    meta_title VARCHAR(200),
    meta_description TEXT,
    meta_keywords JSONB,
    
    -- 통계
    view_count INTEGER DEFAULT 0,
    like_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    share_count INTEGER DEFAULT 0,
    
    -- 상태
    status VARCHAR(20) DEFAULT 'draft', -- draft, published, scheduled, archived
    visibility VARCHAR(20) DEFAULT 'public', -- public, private, members_only
    is_featured BOOLEAN DEFAULT FALSE,
    is_pinned BOOLEAN DEFAULT FALSE,
    
    -- 스케줄링
    published_at TIMESTAMP,
    scheduled_at TIMESTAMP,
    
    -- 작성자 정보
    author_name VARCHAR(200),
    author_avatar VARCHAR(500),
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_category_id ON posts(category_id);
CREATE INDEX idx_posts_slug ON posts(slug);
CREATE INDEX idx_posts_status ON posts(status);
CREATE INDEX idx_posts_published_at ON posts(published_at DESC);

-- 게시글 카테고리
CREATE TABLE post_categories (
    id SERIAL PRIMARY KEY,
    parent_id INTEGER REFERENCES post_categories(id),
    name_ko VARCHAR(200),
    name_en VARCHAR(200),
    slug VARCHAR(200) UNIQUE,
    description TEXT,
    icon VARCHAR(100),
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 미디어 라이브러리
CREATE TABLE media (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    
    -- 파일 정보
    filename VARCHAR(500) NOT NULL,
    original_filename VARCHAR(500),
    file_path VARCHAR(1000) NOT NULL,
    file_url VARCHAR(1000) NOT NULL,
    file_size BIGINT, -- bytes
    mime_type VARCHAR(100),
    
    -- 미디어 타입
    media_type VARCHAR(50), -- image, video, audio, document
    
    -- 이미지 메타데이터
    width INTEGER,
    height INTEGER,
    thumbnail_url VARCHAR(1000),
    
    -- 비디오 메타데이터
    duration INTEGER, -- seconds
    
    -- 태그 및 설명
    title VARCHAR(500),
    alt_text VARCHAR(500),
    caption TEXT,
    description TEXT,
    tags JSONB,
    
    -- 사용 추적
    usage_count INTEGER DEFAULT 0,
    used_in JSONB, -- [{type: 'review', id: 123}, {type: 'post', id: 456}]
    
    -- CDN
    cdn_url VARCHAR(1000),
    is_optimized BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_media_user_id ON media(user_id);
CREATE INDEX idx_media_type ON media(media_type);
CREATE INDEX idx_media_created_at ON media(created_at DESC);

-- 리뷰-거래 매핑
CREATE TABLE review_transaction_mapping (
    review_id INTEGER REFERENCES reviews(id) ON DELETE CASCADE,
    transaction_id INTEGER REFERENCES transactions(id) ON DELETE CASCADE,
    payment_id INTEGER REFERENCES payments(id),
    order_id INTEGER REFERENCES orders(id),
    blockchain_tx_hash VARCHAR(255),
    verified_at TIMESTAMP,
    PRIMARY KEY (review_id, transaction_id)
);

-- 리뷰-리워드 매핑
CREATE TABLE review_reward_mapping (
    review_id INTEGER REFERENCES reviews(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id),
    points_earned INTEGER,
    reward_type VARCHAR(50), -- review_text, review_photo, review_video, best_review
    transaction_id INTEGER REFERENCES point_transactions(id),
    created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (review_id)
);
```

---

## 3. Backend API 설계

### 3.1 리뷰 API

```python
# reviews_api.py
from fastapi import APIRouter, UploadFile, File
from typing import List, Optional

router = APIRouter()

class ReviewCreate(BaseModel):
    product_id: int
    order_id: Optional[int]
    rating_overall: int = Field(..., ge=1, le=5)
    rating_quality: Optional[int] = Field(None, ge=1, le=5)
    rating_delivery: Optional[int] = Field(None, ge=1, le=5)
    rating_packaging: Optional[int] = Field(None, ge=1, le=5)
    title: str = Field(..., max_length=200)
    content: str = Field(..., min_length=10)
    pros: Optional[str]
    cons: Optional[str]
    images: Optional[List[str]] = []
    videos: Optional[List[str]] = []

class ReviewResponse(BaseModel):
    id: int
    product_id: int
    user: UserBasic
    rating_overall: int
    title: str
    content: str
    images: List[dict]
    is_verified_purchase: bool
    helpful_count: int
    unhelpful_count: int
    comment_count: int
    created_at: datetime
    blockchain_hash: Optional[str]

@router.post("/reviews")
async def create_review(
    review: ReviewCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """리뷰 작성"""
    
    # 1. 구매 검증
    order = await verify_purchase(db, current_user.id, review.order_id)
    if not order:
        raise HTTPException(
            status_code=400,
            detail="Only verified purchases can leave reviews"
        )
    
    # 2. 중복 리뷰 확인
    existing = await db.fetch_one(
        "SELECT id FROM reviews WHERE user_id = $1 AND product_id = $2 AND order_id = $3",
        current_user.id, review.product_id, review.order_id
    )
    if existing:
        raise HTTPException(status_code=400, detail="Review already exists")
    
    # 3. 리뷰 저장
    review_id = await db.fetch_val(
        """
        INSERT INTO reviews (
            product_id, user_id, order_id, rating_overall, rating_quality,
            rating_delivery, rating_packaging, title, content, pros, cons,
            images, videos, is_verified_purchase
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, TRUE)
        RETURNING id
        """,
        review.product_id, current_user.id, review.order_id,
        review.rating_overall, review.rating_quality, review.rating_delivery,
        review.rating_packaging, review.title, review.content,
        review.pros, review.cons,
        json.dumps(review.images), json.dumps(review.videos)
    )
    
    # 4. 거래-결제 매핑
    transaction = await get_transaction_by_order(db, review.order_id)
    if transaction:
        await db.execute(
            """
            INSERT INTO review_transaction_mapping (
                review_id, transaction_id, payment_id, order_id
            ) VALUES ($1, $2, $3, $4)
            """,
            review_id, transaction.id, transaction.payment_id, review.order_id
        )
    
    # 5. 블록체인 기록 (리뷰 위변조 방지)
    blockchain_hash = await record_review_on_blockchain(
        review_id, current_user.id, review.product_id
    )
    await db.execute(
        "UPDATE reviews SET blockchain_hash = $1, blockchain_verified = TRUE WHERE id = $2",
        blockchain_hash, review_id
    )
    
    # 6. 리워드 포인트 지급
    points = calculate_review_points(review)
    await grant_review_reward(db, current_user.id, review_id, points)
    
    # 7. 상품 평점 업데이트
    await update_product_rating(db, review.product_id)
    
    # 8. 알림 발송 (판매자에게)
    await send_notification(
        user_id=order.seller_id,
        type='new_review',
        data={'review_id': review_id, 'product_id': review.product_id}
    )
    
    return {"id": review_id, "message": "Review created successfully"}

@router.get("/reviews/{review_id}")
async def get_review(review_id: int, db: Session = Depends(get_db)):
    """리뷰 상세 조회"""
    review = await db.fetch_one(
        """
        SELECT r.*, u.username, u.avatar_url, u.full_name,
               (SELECT COUNT(*) FROM comments WHERE target_type='review' AND target_id=r.id) as comment_count
        FROM reviews r
        JOIN users u ON r.user_id = u.id
        WHERE r.id = $1 AND r.status = 'published'
        """,
        review_id
    )
    
    if not review:
        raise HTTPException(status_code=404, detail="Review not found")
    
    # 조회수 증가
    await db.execute("UPDATE reviews SET view_count = view_count + 1 WHERE id = $1", review_id)
    
    return ReviewResponse(**review)

@router.post("/reviews/{review_id}/helpful")
async def mark_helpful(
    review_id: int,
    is_helpful: bool,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """리뷰 도움됨 표시"""
    await db.execute(
        """
        INSERT INTO review_helpfulness (review_id, user_id, is_helpful)
        VALUES ($1, $2, $3)
        ON CONFLICT (review_id, user_id) DO UPDATE
        SET is_helpful = EXCLUDED.is_helpful
        """,
        review_id, current_user.id, is_helpful
    )
    
    # 카운트 업데이트
    await db.execute(
        """
        UPDATE reviews
        SET helpful_count = (SELECT COUNT(*) FROM review_helpfulness WHERE review_id = $1 AND is_helpful = TRUE),
            unhelpful_count = (SELECT COUNT(*) FROM review_helpfulness WHERE review_id = $1 AND is_helpful = FALSE)
        WHERE id = $1
        """,
        review_id
    )
    
    return {"message": "Vote recorded"}

def calculate_review_points(review: ReviewCreate) -> int:
    """리뷰 포인트 계산"""
    points = 100  # 기본 포인트
    
    if review.images and len(review.images) > 0:
        points += 50  # 사진 리뷰
    
    if review.videos and len(review.videos) > 0:
        points += 100  # 동영상 리뷰
    
    if len(review.content) > 100:
        points += 30  # 상세 리뷰
    
    if review.pros or review.cons:
        points += 20  # 장단점 작성
    
    return points

async def grant_review_reward(
    db: Session,
    user_id: int,
    review_id: int,
    points: int
):
    """리뷰 리워드 지급"""
    # 포인트 트랜잭션 생성
    tx_id = await db.fetch_val(
        """
        INSERT INTO point_transactions (user_id, type, amount, reason, reference_type, reference_id)
        VALUES ($1, 'earn', $2, 'Review reward', 'review', $3)
        RETURNING id
        """,
        user_id, points, review_id
    )
    
    # 매핑 테이블 업데이트
    await db.execute(
        """
        INSERT INTO review_reward_mapping (review_id, user_id, points_earned, reward_type, transaction_id)
        VALUES ($1, $2, $3, $4, $5)
        """,
        review_id, user_id, points, 'review_text', tx_id
    )
    
    # 사용자 포인트 업데이트
    await db.execute(
        "UPDATE reward_points SET points = points + $1, total_earned = total_earned + $1 WHERE user_id = $2",
        points, user_id
    )
```

### 3.2 댓글 API

```python
# comments_api.py
class CommentCreate(BaseModel):
    target_type: str  # review, post, product
    target_id: int
    parent_id: Optional[int] = None
    content: str = Field(..., min_length=1, max_length=1000)
    images: Optional[List[str]] = []

class CommentResponse(BaseModel):
    id: int
    user: UserBasic
    content: str
    images: List[str]
    like_count: int
    dislike_count: int
    replies: List['CommentResponse'] = []
    is_author_reply: bool
    created_at: datetime

@router.post("/comments")
async def create_comment(
    comment: CommentCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """댓글 작성"""
    
    # 1. 대상 존재 확인
    target_exists = await verify_target_exists(
        db, comment.target_type, comment.target_id
    )
    if not target_exists:
        raise HTTPException(status_code=404, detail="Target not found")
    
    # 2. 댓글 저장
    comment_id = await db.fetch_val(
        """
        INSERT INTO comments (target_type, target_id, parent_id, user_id, content, images)
        VALUES ($1, $2, $3, $4, $5, $6)
        RETURNING id
        """,
        comment.target_type, comment.target_id, comment.parent_id,
        current_user.id, comment.content, json.dumps(comment.images)
    )
    
    # 3. 작성자 답글 여부 확인
    if comment.target_type == 'review':
        review = await db.fetch_one(
            "SELECT user_id FROM reviews WHERE id = $1",
            comment.target_id
        )
        if review and review['user_id'] == current_user.id:
            await db.execute(
                "UPDATE comments SET is_author_reply = TRUE WHERE id = $1",
                comment_id
            )
    
    # 4. 블록체인 기록
    blockchain_hash = await record_comment_on_blockchain(
        comment_id, current_user.id, comment.target_type, comment.target_id
    )
    await db.execute(
        "UPDATE comments SET blockchain_hash = $1 WHERE id = $2",
        blockchain_hash, comment_id
    )
    
    # 5. 댓글 수 업데이트
    await update_comment_count(db, comment.target_type, comment.target_id)
    
    # 6. 알림 발송
    await notify_comment_created(
        db, comment.target_type, comment.target_id, current_user.id
    )
    
    return {"id": comment_id, "message": "Comment created"}

@router.get("/comments")
async def get_comments(
    target_type: str,
    target_id: int,
    page: int = 1,
    limit: int = 20,
    sort: str = 'recent',  # recent, popular
    db: Session = Depends(get_db)
):
    """댓글 목록 조회"""
    offset = (page - 1) * limit
    
    order_by = "created_at DESC" if sort == 'recent' else "like_count DESC"
    
    comments = await db.fetch_all(
        f"""
        SELECT c.*, u.username, u.avatar_url, u.full_name,
               (SELECT COUNT(*) FROM comments WHERE parent_id = c.id) as reply_count
        FROM comments c
        JOIN users u ON c.user_id = u.id
        WHERE c.target_type = $1 AND c.target_id = $2 AND c.parent_id IS NULL
        AND c.status = 'published' AND c.deleted_at IS NULL
        ORDER BY {order_by}
        LIMIT $3 OFFSET $4
        """,
        target_type, target_id, limit, offset
    )
    
    # 대댓글 가져오기
    result = []
    for comment in comments:
        replies = await get_comment_replies(db, comment['id'])
        result.append({
            **comment,
            'replies': replies
        })
    
    return result

async def get_comment_replies(db: Session, parent_id: int) -> List[dict]:
    """대댓글 조회"""
    return await db.fetch_all(
        """
        SELECT c.*, u.username, u.avatar_url, u.full_name
        FROM comments c
        JOIN users u ON c.user_id = u.id
        WHERE c.parent_id = $1 AND c.status = 'published' AND c.deleted_at IS NULL
        ORDER BY c.created_at ASC
        """,
        parent_id
    )
```

### 3.3 감정평가 API

```python
# reactions_api.py
class ReactionCreate(BaseModel):
    target_type: str  # review, comment, post
    target_id: int
    reaction_type: str  # like, love, helpful, funny, angry, sad

@router.post("/reactions")
async def add_reaction(
    reaction: ReactionCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """감정평가 추가"""
    
    # 기존 반응 확인 및 토글
    existing = await db.fetch_one(
        """
        SELECT id FROM reactions
        WHERE user_id = $1 AND target_type = $2 AND target_id = $3 AND reaction_type = $4
        """,
        current_user.id, reaction.target_type, reaction.target_id, reaction.reaction_type
    )
    
    if existing:
        # 이미 같은 반응이 있으면 제거 (토글)
        await db.execute(
            "DELETE FROM reactions WHERE id = $1",
            existing['id']
        )
        action = "removed"
    else:
        # 다른 타입의 반응 제거 (한 번에 하나의 반응만)
        await db.execute(
            """
            DELETE FROM reactions
            WHERE user_id = $1 AND target_type = $2 AND target_id = $3
            """,
            current_user.id, reaction.target_type, reaction.target_id
        )
        
        # 새 반응 추가
        await db.execute(
            """
            INSERT INTO reactions (user_id, target_type, target_id, reaction_type)
            VALUES ($1, $2, $3, $4)
            """,
            current_user.id, reaction.target_type, reaction.target_id, reaction.reaction_type
        )
        action = "added"
    
    # 카운트 업데이트
    await update_reaction_counts(db, reaction.target_type, reaction.target_id)
    
    return {"message": f"Reaction {action}"}

@router.get("/reactions")
async def get_reactions(
    target_type: str,
    target_id: int,
    db: Session = Depends(get_db)
):
    """감정평가 통계 조회"""
    reactions = await db.fetch_all(
        """
        SELECT reaction_type, COUNT(*) as count
        FROM reactions
        WHERE target_type = $1 AND target_id = $2
        GROUP BY reaction_type
        """,
        target_type, target_id
    )
    
    return {row['reaction_type']: row['count'] for row in reactions}

async def update_reaction_counts(db: Session, target_type: str, target_id: int):
    """반응 카운트 업데이트"""
    if target_type == 'comment':
        like_count = await db.fetch_val(
            """
            SELECT COUNT(*) FROM reactions
            WHERE target_type = 'comment' AND target_id = $1 AND reaction_type = 'like'
            """,
            target_id
        )
        await db.execute(
            "UPDATE comments SET like_count = $1 WHERE id = $2",
            like_count, target_id
        )
```

### 3.4 게시글 CMS API

```python
# posts_api.py
class PostCreate(BaseModel):
    title: str = Field(..., max_length=500)
    content: str = Field(..., min_length=10)
    category_id: Optional[int]
    excerpt: Optional[str]
    featured_image: Optional[str]
    images: Optional[List[str]] = []
    status: str = 'draft'  # draft, published
    visibility: str = 'public'
    scheduled_at: Optional[datetime]
    meta_title: Optional[str]
    meta_description: Optional[str]
    meta_keywords: Optional[List[str]]

@router.post("/posts")
async def create_post(
    post: PostCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """게시글 작성"""
    
    # slug 생성
    slug = generate_slug(post.title)
    
    post_id = await db.fetch_val(
        """
        INSERT INTO posts (
            user_id, category_id, title, slug, content, excerpt,
            featured_image, images, status, visibility, scheduled_at,
            meta_title, meta_description, meta_keywords,
            published_at
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15)
        RETURNING id
        """,
        current_user.id, post.category_id, post.title, slug, post.content,
        post.excerpt, post.featured_image, json.dumps(post.images),
        post.status, post.visibility, post.scheduled_at,
        post.meta_title, post.meta_description, json.dumps(post.meta_keywords),
        datetime.now() if post.status == 'published' else None
    )
    
    return {"id": post_id, "slug": slug}

@router.post("/posts/{post_id}/publish")
async def publish_post(
    post_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """게시글 발행"""
    await db.execute(
        """
        UPDATE posts
        SET status = 'published', published_at = NOW(), updated_at = NOW()
        WHERE id = $1 AND user_id = $2
        """,
        post_id, current_user.id
    )
    
    return {"message": "Post published"}
```

### 3.5 미디어 업로드 API

```python
# media_api.py
import aiofiles
from PIL import Image
import io

@router.post("/media/upload")
async def upload_media(
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """미디어 업로드"""
    
    # 1. 파일 검증
    if file.size > 10 * 1024 * 1024:  # 10MB 제한
        raise HTTPException(status_code=400, detail="File too large")
    
    allowed_types = ['image/jpeg', 'image/png', 'image/gif', 'image/webp', 'video/mp4']
    if file.content_type not in allowed_types:
        raise HTTPException(status_code=400, detail="Invalid file type")
    
    # 2. 파일명 생성
    ext = file.filename.split('.')[-1]
    filename = f"{uuid.uuid4()}.{ext}"
    file_path = f"uploads/{datetime.now().year}/{datetime.now().month}/{filename}"
    
    # 3. S3 업로드
    file_url = await upload_to_s3(file, file_path)
    
    # 4. 이미지 처리 (썸네일 생성)
    thumbnail_url = None
    width, height = None, None
    
    if file.content_type.startswith('image/'):
        # 이미지 메타데이터 추출
        content = await file.read()
        image = Image.open(io.BytesIO(content))
        width, height = image.size
        
        # 썸네일 생성
        thumbnail = create_thumbnail(image, (300, 300))
        thumbnail_path = f"thumbnails/{filename}"
        thumbnail_url = await upload_thumbnail_to_s3(thumbnail, thumbnail_path)
    
    # 5. DB 저장
    media_id = await db.fetch_val(
        """
        INSERT INTO media (
            user_id, filename, original_filename, file_path, file_url,
            file_size, mime_type, media_type, width, height, thumbnail_url
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
        RETURNING id
        """,
        current_user.id, filename, file.filename, file_path, file_url,
        file.size, file.content_type,
        'image' if file.content_type.startswith('image/') else 'video',
        width, height, thumbnail_url
    )
    
    return {
        "id": media_id,
        "url": file_url,
        "thumbnail_url": thumbnail_url,
        "width": width,
        "height": height
    }

@router.post("/media/edit")
async def edit_image(
    media_id: int,
    operations: List[dict],  # [{type: 'crop', params: {...}}, {type: 'filter', params: {...}}]
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """이미지 편집"""
    
    # 미디어 조회
    media = await db.fetch_one(
        "SELECT * FROM media WHERE id = $1 AND user_id = $2",
        media_id, current_user.id
    )
    
    if not media:
        raise HTTPException(status_code=404, detail="Media not found")
    
    # 이미지 다운로드
    image_data = await download_from_s3(media['file_path'])
    image = Image.open(io.BytesIO(image_data))
    
    # 편집 작업 수행
    for op in operations:
        if op['type'] == 'crop':
            image = image.crop(tuple(op['params']['box']))
        elif op['type'] == 'resize':
            image = image.resize(tuple(op['params']['size']))
        elif op['type'] == 'rotate':
            image = image.rotate(op['params']['degrees'])
        elif op['type'] == 'filter':
            # 필터 적용 (PIL ImageFilter)
            pass
    
    # 편집된 이미지 업로드
    edited_filename = f"edited_{media['filename']}"
    edited_path = f"uploads/edited/{edited_filename}"
    edited_url = await upload_pil_image_to_s3(image, edited_path)
    
    # 새 미디어 항목 생성
    new_media_id = await db.fetch_val(
        """
        INSERT INTO media (
            user_id, filename, file_path, file_url, mime_type, media_type, width, height
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
        RETURNING id
        """,
        current_user.id, edited_filename, edited_path, edited_url,
        'image/jpeg', 'image', image.width, image.height
    )
    
    return {
        "id": new_media_id,
        "url": edited_url
    }
```

---

## 4. Frontend 컴포넌트

### 4.1 리뷰 작성 컴포넌트

```tsx
// ReviewForm.tsx
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Rating, ImageUpload, TextEditor } from '@/components';

interface ReviewFormProps {
  productId: number;
  orderId: number;
  onSuccess: () => void;
}

export const ReviewForm: React.FC<ReviewFormProps> = ({
  productId,
  orderId,
  onSuccess
}) => {
  const { register, handleSubmit, setValue, watch } = useForm();
  const [images, setImages] = useState<string[]>([]);
  const [videos, setVideos] = useState<string[]>([]);
  
  const onSubmit = async (data: any) => {
    const response = await api.post('/reviews', {
      product_id: productId,
      order_id: orderId,
      ...data,
      images,
      videos
    });
    
    if (response.ok) {
      onSuccess();
      toast.success('리뷰가 작성되었습니다. 포인트가 적립되었습니다!');
    }
  };
  
  return (
    <ReviewFormContainer onSubmit={handleSubmit(onSubmit)}>
      <FormSection>
        <h3>상품은 어떠셨나요?</h3>
        
        {/* 전체 평점 */}
        <RatingRow>
          <label>전체 평점</label>
          <Rating
            value={watch('rating_overall')}
            onChange={(val) => setValue('rating_overall', val)}
            size="large"
          />
        </RatingRow>
        
        {/* 세부 평점 */}
        <RatingRow>
          <label>상품 품질</label>
          <Rating
            value={watch('rating_quality')}
            onChange={(val) => setValue('rating_quality', val)}
          />
        </RatingRow>
        
        <RatingRow>
          <label>배송 속도</label>
          <Rating
            value={watch('rating_delivery')}
            onChange={(val) => setValue('rating_delivery', val)}
          />
        </RatingRow>
        
        <RatingRow>
          <label>포장 상태</label>
          <Rating
            value={watch('rating_packaging')}
            onChange={(val) => setValue('rating_packaging', val)}
          />
        </RatingRow>
      </FormSection>
      
      <FormSection>
        <h3>리뷰 작성</h3>
        
        <Input
          {...register('title', { required: true })}
          placeholder="리뷰 제목을 입력하세요"
          maxLength={200}
        />
        
        <TextArea
          {...register('content', { required: true, minLength: 10 })}
          placeholder="상품에 대한 솔직한 리뷰를 작성해주세요 (최소 10자)"
          rows={5}
        />
        
        <TwoColumnGrid>
          <TextArea
            {...register('pros')}
            placeholder="장점"
            rows={3}
          />
          <TextArea
            {...register('cons')}
            placeholder="단점"
            rows={3}
          />
        </TwoColumnGrid>
      </FormSection>
      
      <FormSection>
        <h3>사진 및 동영상 첨부</h3>
        <RewardBadge>사진 +50P, 동영상 +100P</RewardBadge>
        
        <ImageUpload
          multiple
          maxFiles={10}
          onUpload={(urls) => setImages([...images, ...urls])}
          onRemove={(url) => setImages(images.filter(img => img !== url))}
          value={images}
        />
        
        <VideoUpload
          maxFiles={3}
          onUpload={(urls) => setVideos([...videos, ...urls])}
          value={videos}
        />
      </FormSection>
      
      <SubmitSection>
        <RewardInfo>
          예상 적립 포인트: {calculatePoints()}P
        </RewardInfo>
        <Button type="submit" size="large">리뷰 등록</Button>
      </SubmitSection>
    </ReviewFormContainer>
  );
};
```

### 4.2 댓글 시스템 컴포넌트

```tsx
// CommentSection.tsx
export const CommentSection: React.FC<CommentSectionProps> = ({
  targetType,
  targetId
}) => {
  const [comments, setComments] = useState<Comment[]>([]);
  const [newComment, setNewComment] = useState('');
  const [replyingTo, setReplyingTo] = useState<number | null>(null);
  
  const handleSubmit = async () => {
    const response = await api.post('/comments', {
      target_type: targetType,
      target_id: targetId,
      parent_id: replyingTo,
      content: newComment
    });
    
    if (response.ok) {
      setComments([response.data, ...comments]);
      setNewComment('');
      setReplyingTo(null);
    }
  };
  
  return (
    <CommentContainer>
      <CommentHeader>
        <h3>댓글 {comments.length}</h3>
      </CommentHeader>
      
      {/* 댓글 작성 */}
      <CommentInput>
        <Avatar src={currentUser.avatar} />
        <TextArea
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder="댓글을 입력하세요..."
          rows={3}
        />
        <Button onClick={handleSubmit}>등록</Button>
      </CommentInput>
      
      {/* 댓글 목록 */}
      <CommentList>
        {comments.map(comment => (
          <CommentItem key={comment.id}>
            <CommentAuthor>
              <Avatar src={comment.user.avatar} size="small" />
              <AuthorName>{comment.user.name}</AuthorName>
              <CommentDate>{formatRelativeTime(comment.created_at)}</CommentDate>
            </CommentAuthor>
            
            <CommentContent>{comment.content}</CommentContent>
            
            {comment.images && comment.images.length > 0 && (
              <CommentImages>
                {comment.images.map(img => (
                  <img key={img} src={img} alt="" />
                ))}
              </CommentImages>
            )}
            
            <CommentActions>
              <ActionButton onClick={() => handleReaction(comment.id, 'like')}>
                <ThumbsUpIcon /> {comment.like_count}
              </ActionButton>
              <ActionButton onClick={() => setReplyingTo(comment.id)}>
                <ReplyIcon /> 답글
              </ActionButton>
              {comment.user.id === currentUser.id && (
                <>
                  <ActionButton onClick={() => handleEdit(comment.id)}>
                    수정
                  </ActionButton>
                  <ActionButton onClick={() => handleDelete(comment.id)}>
                    삭제
                  </ActionButton>
                </>
              )}
            </CommentActions>
            
            {/* 대댓글 */}
            {comment.replies && comment.replies.length > 0 && (
              <RepliesList>
                {comment.replies.map(reply => (
                  <ReplyItem key={reply.id}>
                    {/* 대댓글 렌더링 */}
                  </ReplyItem>
                ))}
              </RepliesList>
            )}
          </CommentItem>
        ))}
      </CommentList>
    </CommentContainer>
  );
};
```

### 4.3 감정평가 컴포넌트

```tsx
// ReactionButtons.tsx
export const ReactionButtons: React.FC<ReactionButtonsProps> = ({
  targetType,
  targetId
}) => {
  const [reactions, setReactions] = useState<Record<string, number>>({});
  const [userReaction, setUserReaction] = useState<string | null>(null);
  
  const reactionTypes = [
    { type: 'like', icon: '👍', label: '좋아요' },
    { type: 'love', icon: '❤️', label: '최고예요' },
    { type: 'helpful', icon: '💡', label: '도움됨' },
    { type: 'funny', icon: '😄', label: '재미있어요' }
  ];
  
  const handleReaction = async (reactionType: string) => {
    await api.post('/reactions', {
      target_type: targetType,
      target_id: targetId,
      reaction_type: reactionType
    });
    
    // 로컬 상태 업데이트
    if (userReaction === reactionType) {
      setUserReaction(null);
      setReactions({
        ...reactions,
        [reactionType]: (reactions[reactionType] || 0) - 1
      });
    } else {
      if (userReaction) {
        setReactions({
          ...reactions,
          [userReaction]: (reactions[userReaction] || 0) - 1,
          [reactionType]: (reactions[reactionType] || 0) + 1
        });
      } else {
        setReactions({
          ...reactions,
          [reactionType]: (reactions[reactionType] || 0) + 1
        });
      }
      setUserReaction(reactionType);
    }
  };
  
  return (
    <ReactionButtonsContainer>
      {reactionTypes.map(({ type, icon, label }) => (
        <ReactionButton
          key={type}
          active={userReaction === type}
          onClick={() => handleReaction(type)}
        >
          <span className="icon">{icon}</span>
          <span className="label">{label}</span>
          <span className="count">{reactions[type] || 0}</span>
        </ReactionButton>
      ))}
    </ReactionButtonsContainer>
  );
};
```

### 4.4 이미지 편집기 컴포넌트

```tsx
// ImageEditor.tsx
import Cropper from 'react-easy-crop';

export const ImageEditor: React.FC<ImageEditorProps> = ({
  image,
  onSave
}) => {
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [rotation, setRotation] = useState(0);
  const [filter, setFilter] = useState('none');
  
  const filters = [
    { name: 'none', label: '원본' },
    { name: 'grayscale', label: '흑백' },
    { name: 'sepia', label: '세피아' },
    { name: 'brightness', label: '밝게' },
    { name: 'contrast', label: '대비' }
  ];
  
  const handleSave = async () => {
    const operations = [
      { type: 'crop', params: { box: croppedAreaPixels } },
      { type: 'rotate', params: { degrees: rotation } },
      { type: 'filter', params: { name: filter } }
    ];
    
    const result = await api.post('/media/edit', {
      media_id: image.id,
      operations
    });
    
    onSave(result.data.url);
  };
  
  return (
    <EditorContainer>
      <EditorCanvas>
        <Cropper
          image={image.url}
          crop={crop}
          zoom={zoom}
          rotation={rotation}
          onCropChange={setCrop}
          onZoomChange={setZoom}
          onRotationChange={setRotation}
          aspect={4 / 3}
        />
      </EditorCanvas>
      
      <EditorControls>
        <ControlGroup>
          <label>확대/축소</label>
          <Slider
            value={zoom}
            onChange={setZoom}
            min={1}
            max={3}
            step={0.1}
          />
        </ControlGroup>
        
        <ControlGroup>
          <label>회전</label>
          <Slider
            value={rotation}
            onChange={setRotation}
            min={0}
            max={360}
            step={1}
          />
        </ControlGroup>
        
        <ControlGroup>
          <label>필터</label>
          <FilterGrid>
            {filters.map(f => (
              <FilterButton
                key={f.name}
                active={filter === f.name}
                onClick={() => setFilter(f.name)}
              >
                {f.label}
              </FilterButton>
            ))}
          </FilterGrid>
        </ControlGroup>
        
        <ButtonGroup>
          <Button variant="secondary" onClick={onCancel}>취소</Button>
          <Button onClick={handleSave}>저장</Button>
        </ButtonGroup>
      </EditorControls>
    </EditorContainer>
  );
};
```

---

## 5. 데이터 매핑 및 연계

### 5.1 리뷰-거래-결제 연계 플로우

```
1. 고객이 상품 구매
   ↓
2. 결제 완료 (payments 테이블)
   ↓
3. 거래 생성 (transactions 테이블)
   ↓
4. 블록체인에 거래 기록 (blockchain_transactions 테이블)
   ↓
5. 상품 배송 완료
   ↓
6. 리뷰 작성 가능 알림
   ↓
7. 리뷰 작성 (reviews 테이블)
   ↓
8. 리뷰-거래 매핑 (review_transaction_mapping)
   ↓
9. 리뷰 블록체인 기록 (위변조 방지)
   ↓
10. 리워드 포인트 지급 (point_transactions)
    ↓
11. 리뷰-리워드 매핑 (review_reward_mapping)
```

### 5.2 평가-라벨링 연계

```python
# labeling_integration.py
async def create_review_labels_for_ai(review_id: int, db: Session):
    """리뷰 데이터를 AI 학습용으로 라벨링"""
    
    review = await db.fetch_one(
        "SELECT * FROM reviews WHERE id = $1",
        review_id
    )
    
    # 자동 라벨링
    labels = []
    
    # 감정 분석
    sentiment = analyze_sentiment(review['content'])
    labels.append({
        'label': 'sentiment',
        'value': sentiment,  # positive, negative, neutral
        'confidence': 0.85
    })
    
    # 카테고리 분류
    category = classify_review_category(review['content'])
    labels.append({
        'label': 'category',
        'value': category,
        'confidence': 0.90
    })
    
    # 품질 평가
    quality_keywords = extract_quality_keywords(review['content'])
    labels.append({
        'label': 'quality_aspects',
        'value': quality_keywords,
        'confidence': 0.78
    })
    
    # DB에 저장
    await db.execute(
        """
        INSERT INTO data_labels (data_type, data_id, labels, labeled_by)
        VALUES ('review', $1, $2, NULL)
        """,
        review_id, json.dumps(labels)
    )
    
    return labels
```

---

**문서 관리**
- 작성자: 장재훈
- 최종 업데이트: 2025-11-19
- 연관 문서: 플랫폼 아키텍처, Backend 관리자 시스템, 데이터베이스 스키마


