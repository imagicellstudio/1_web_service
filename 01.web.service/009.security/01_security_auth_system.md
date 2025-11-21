# 보안, 인증, 권한 체계 설계

## 문서 정보
- 작성일: 2025-11-19
- 버전: 2.0 (플랫폼)
- 대상: 전체 시스템 보안 아키텍처

---

## 1. 보안 계층 구조

```
┌─────────────────────────────────────────────────────┐
│  Layer 7: Application Security                      │
│  - Input Validation                                 │
│  - XSS/CSRF Protection                              │
│  - SQL Injection Prevention                         │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Layer 6: Authentication & Authorization            │
│  - JWT Token                                        │
│  - OAuth 2.0                                        │
│  - RBAC (Role-Based Access Control)                 │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Layer 5: API Security                              │
│  - API Gateway                                      │
│  - Rate Limiting                                    │
│  - API Key Management                               │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Layer 4: Transport Security                        │
│  - HTTPS/TLS 1.3                                    │
│  - Certificate Management                           │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Layer 3: Network Security                          │
│  - Firewall                                         │
│  - DDoS Protection                                  │
│  - VPN/Private Network                              │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Layer 2: Data Security                             │
│  - Encryption at Rest (AES-256)                     │
│  - Database Security                                │
│  - Backup Encryption                                │
└─────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────┐
│  Layer 1: Infrastructure Security                   │
│  - Cloud Security (AWS/Azure)                       │
│  - Container Security (Docker/K8s)                  │
│  - OS Hardening                                     │
└─────────────────────────────────────────────────────┘
```

---

## 2. 인증 (Authentication)

### 2.1 JWT 기반 인증

```python
# auth.py
from jose import JWTError, jwt
from datetime import datetime, timedelta
from passlib.context import CryptContext

SECRET_KEY = "your-secret-key-here"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30
REFRESH_TOKEN_EXPIRE_DAYS = 7

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def verify_password(plain_password: str, hashed_password: str) -> bool:
    """비밀번호 검증"""
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str) -> str:
    """비밀번호 해싱"""
    return pwd_context.hash(password)

def create_access_token(data: dict, expires_delta: timedelta = None):
    """Access Token 생성"""
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    
    to_encode.update({
        "exp": expire,
        "iat": datetime.utcnow(),
        "type": "access"
    })
    
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

def create_refresh_token(data: dict):
    """Refresh Token 생성"""
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)
    
    to_encode.update({
        "exp": expire,
        "iat": datetime.utcnow(),
        "type": "refresh"
    })
    
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

async def get_current_user(token: str = Depends(oauth2_scheme)):
    """현재 사용자 가져오기"""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: int = payload.get("sub")
        if user_id is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    
    user = await get_user(user_id)
    if user is None:
        raise credentials_exception
    
    return user
```

### 2.2 로그인 프로세스

```python
# login.py
@router.post("/auth/login")
async def login(
    credentials: LoginCredentials,
    db: Session = Depends(get_db)
):
    """로그인"""
    # 1. 사용자 조회
    user = await get_user_by_email(db, credentials.email)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password"
        )
    
    # 2. 비밀번호 검증
    if not verify_password(credentials.password, user.password_hash):
        # 실패 로그 기록
        await log_failed_login(user.id, credentials.ip_address)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password"
        )
    
    # 3. 계정 상태 확인
    if user.status != 'active':
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=f"Account is {user.status}"
        )
    
    # 4. 2FA 확인 (활성화된 경우)
    if user.two_factor_enabled:
        # 2FA 토큰 생성 및 반환
        temp_token = create_temp_2fa_token(user.id)
        return {"requires_2fa": True, "temp_token": temp_token}
    
    # 5. 토큰 생성
    access_token = create_access_token(
        data={"sub": user.id, "role": user.role}
    )
    refresh_token = create_refresh_token(
        data={"sub": user.id}
    )
    
    # 6. 로그인 이력 저장
    await log_successful_login(
        user.id,
        credentials.ip_address,
        credentials.user_agent
    )
    
    # 7. 마지막 로그인 시간 업데이트
    await update_last_login(user.id)
    
    return {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer",
        "user": {
            "id": user.id,
            "email": user.email,
            "name": user.full_name,
            "role": user.role
        }
    }
```

### 2.3 2단계 인증 (2FA)

```python
# two_factor.py
import pyotp
from qrcode import make as make_qr

def generate_2fa_secret():
    """2FA 비밀키 생성"""
    return pyotp.random_base32()

def get_2fa_qr_code(user_email: str, secret: str) -> bytes:
    """2FA QR 코드 생성"""
    totp = pyotp.TOTP(secret)
    provisioning_uri = totp.provisioning_uri(
        name=user_email,
        issuer_name="SpicyJump"
    )
    
    qr = make_qr(provisioning_uri)
    buffer = BytesIO()
    qr.save(buffer, format='PNG')
    return buffer.getvalue()

def verify_2fa_code(secret: str, code: str) -> bool:
    """2FA 코드 검증"""
    totp = pyotp.TOTP(secret)
    return totp.verify(code, valid_window=1)

@router.post("/auth/2fa/enable")
async def enable_2fa(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """2FA 활성화"""
    # 1. 비밀키 생성
    secret = generate_2fa_secret()
    
    # 2. DB에 저장 (임시)
    await db.execute(
        "UPDATE users SET two_factor_secret = $1 WHERE id = $2",
        secret, current_user.id
    )
    
    # 3. QR 코드 생성
    qr_code = get_2fa_qr_code(current_user.email, secret)
    
    return {
        "secret": secret,
        "qr_code": base64.b64encode(qr_code).decode()
    }

@router.post("/auth/2fa/verify")
async def verify_2fa(
    code: str,
    temp_token: str,
    db: Session = Depends(get_db)
):
    """2FA 코드 검증 및 로그인 완료"""
    # 1. 임시 토큰 검증
    payload = jwt.decode(temp_token, SECRET_KEY, algorithms=[ALGORITHM])
    user_id = payload.get("sub")
    
    # 2. 사용자 조회
    user = await get_user(db, user_id)
    
    # 3. 2FA 코드 검증
    if not verify_2fa_code(user.two_factor_secret, code):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid 2FA code"
        )
    
    # 4. 최종 토큰 발급
    access_token = create_access_token(
        data={"sub": user.id, "role": user.role}
    )
    refresh_token = create_refresh_token(
        data={"sub": user.id}
    )
    
    return {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer"
    }
```

### 2.4 OAuth 2.0 소셜 로그인

```python
# oauth.py
from authlib.integrations.starlette_client import OAuth

oauth = OAuth()

# Google OAuth 설정
oauth.register(
    name='google',
    client_id=settings.GOOGLE_CLIENT_ID,
    client_secret=settings.GOOGLE_CLIENT_SECRET,
    server_metadata_url='https://accounts.google.com/.well-known/openid-configuration',
    client_kwargs={'scope': 'openid email profile'}
)

# Facebook OAuth 설정
oauth.register(
    name='facebook',
    client_id=settings.FACEBOOK_CLIENT_ID,
    client_secret=settings.FACEBOOK_CLIENT_SECRET,
    authorize_url='https://www.facebook.com/v12.0/dialog/oauth',
    access_token_url='https://graph.facebook.com/v12.0/oauth/access_token',
    client_kwargs={'scope': 'email public_profile'}
)

@router.get("/auth/google")
async def google_login(request: Request):
    """Google 로그인 시작"""
    redirect_uri = request.url_for('google_callback')
    return await oauth.google.authorize_redirect(request, redirect_uri)

@router.get("/auth/google/callback")
async def google_callback(request: Request, db: Session = Depends(get_db)):
    """Google 로그인 콜백"""
    # 1. Google로부터 토큰 받기
    token = await oauth.google.authorize_access_token(request)
    
    # 2. 사용자 정보 가져오기
    user_info = token.get('userinfo')
    
    # 3. 기존 사용자 확인 또는 생성
    user = await get_or_create_oauth_user(
        db,
        provider='google',
        provider_user_id=user_info['sub'],
        email=user_info['email'],
        name=user_info.get('name'),
        avatar=user_info.get('picture')
    )
    
    # 4. 토큰 발급
    access_token = create_access_token(
        data={"sub": user.id, "role": user.role}
    )
    refresh_token = create_refresh_token(
        data={"sub": user.id}
    )
    
    return {
        "access_token": access_token,
        "refresh_token": refresh_token,
        "token_type": "bearer"
    }
```

---

## 3. 권한 관리 (Authorization)

### 3.1 RBAC (Role-Based Access Control)

```python
# permissions.py
from enum import Enum
from typing import List

class Permission(str, Enum):
    # 거래
    TRANSACTION_READ = "transaction.read"
    TRANSACTION_UPDATE = "transaction.update"
    TRANSACTION_DELETE = "transaction.delete"
    TRANSACTION_REFUND = "transaction.refund"
    
    # 회원
    MEMBER_READ = "member.read"
    MEMBER_CREATE = "member.create"
    MEMBER_UPDATE = "member.update"
    MEMBER_DELETE = "member.delete"
    MEMBER_SUSPEND = "member.suspend"
    
    # 상품
    PRODUCT_READ = "product.read"
    PRODUCT_CREATE = "product.create"
    PRODUCT_UPDATE = "product.update"
    PRODUCT_DELETE = "product.delete"
    
    # 데이터
    DATA_READ = "data.read"
    DATA_EXPORT = "data.export"
    DATA_IMPORT = "data.import"
    DATA_DELETE = "data.delete"
    
    # 시스템
    SYSTEM_SETTINGS = "system.settings"
    SYSTEM_LOGS = "system.logs"
    SYSTEM_BACKUP = "system.backup"

# 역할별 권한 정의
ROLE_PERMISSIONS = {
    "super_admin": ["*"],  # 모든 권한
    "admin": [
        Permission.TRANSACTION_READ,
        Permission.TRANSACTION_UPDATE,
        Permission.MEMBER_READ,
        Permission.MEMBER_UPDATE,
        Permission.PRODUCT_READ,
        Permission.PRODUCT_CREATE,
        Permission.PRODUCT_UPDATE,
        Permission.DATA_READ,
        Permission.DATA_EXPORT,
    ],
    "moderator": [
        Permission.TRANSACTION_READ,
        Permission.MEMBER_READ,
        Permission.PRODUCT_READ,
    ],
    "seller": [
        Permission.PRODUCT_CREATE,
        Permission.PRODUCT_UPDATE,
        Permission.TRANSACTION_READ,
    ],
    "buyer": [
        Permission.PRODUCT_READ,
    ]
}

def has_permission(user: User, permission: Permission) -> bool:
    """사용자 권한 확인"""
    user_permissions = ROLE_PERMISSIONS.get(user.role, [])
    
    # Super admin은 모든 권한
    if "*" in user_permissions:
        return True
    
    return permission in user_permissions

def require_permission(permission: Permission):
    """권한 필요 데코레이터"""
    def decorator(func):
        @wraps(func)
        async def wrapper(*args, current_user: User = Depends(get_current_user), **kwargs):
            if not has_permission(current_user, permission):
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail=f"Permission denied: {permission}"
                )
            return await func(*args, current_user=current_user, **kwargs)
        return wrapper
    return decorator

# 사용 예시
@router.get("/admin/transactions")
@require_permission(Permission.TRANSACTION_READ)
async def get_transactions(current_user: User = Depends(get_current_user)):
    """거래 내역 조회 (권한 필요)"""
    return await fetch_transactions()

@router.delete("/admin/members/{member_id}")
@require_permission(Permission.MEMBER_DELETE)
async def delete_member(
    member_id: int,
    current_user: User = Depends(get_current_user)
):
    """회원 삭제 (권한 필요)"""
    return await delete_member_by_id(member_id)
```

### 3.2 동적 권한 관리

```python
# dynamic_permissions.py
async def get_user_permissions(user_id: int, db: Session) -> List[str]:
    """사용자의 모든 권한 조회"""
    query = """
        SELECT DISTINCT p.resource, p.action
        FROM users u
        JOIN user_roles ur ON u.id = ur.user_id
        JOIN role_permissions rp ON ur.role_id = rp.role_id
        JOIN permissions p ON rp.permission_id = p.id
        WHERE u.id = $1 AND (ur.expires_at IS NULL OR ur.expires_at > NOW())
    """
    
    rows = await db.fetch_all(query, user_id)
    permissions = [f"{row['resource']}.{row['action']}" for row in rows]
    
    return permissions

async def assign_role_to_user(
    user_id: int,
    role_id: int,
    granted_by: int,
    expires_at: datetime = None,
    db: Session = Depends(get_db)
):
    """사용자에게 역할 할당"""
    await db.execute(
        """
        INSERT INTO user_roles (user_id, role_id, granted_by, expires_at)
        VALUES ($1, $2, $3, $4)
        ON CONFLICT (user_id, role_id) DO UPDATE
        SET expires_at = EXCLUDED.expires_at
        """,
        user_id, role_id, granted_by, expires_at
    )
    
    # 감사 로그
    await log_audit(
        user_id=granted_by,
        action="assign_role",
        resource_type="user",
        resource_id=user_id,
        new_data={"role_id": role_id, "expires_at": expires_at}
    )
```

---

## 4. API 보안

### 4.1 Rate Limiting

```python
# rate_limiting.py
from fastapi import Request
from slowapi import Limiter
from slowapi.util import get_remote_address

limiter = Limiter(key_func=get_remote_address)

# 전역 Rate Limit
app.state.limiter = limiter

@router.get("/api/products")
@limiter.limit("100/minute")  # 분당 100회
async def get_products(request: Request):
    """상품 목록 (Rate Limit 적용)"""
    return await fetch_products()

@router.post("/api/orders")
@limiter.limit("10/minute")  # 분당 10회
async def create_order(request: Request, order: OrderCreate):
    """주문 생성 (Rate Limit 엄격)"""
    return await create_new_order(order)

# 사용자별 Rate Limit
async def get_user_rate_limit_key(request: Request):
    """사용자별 Rate Limit 키"""
    user = await get_current_user_optional(request)
    if user:
        return f"user:{user.id}"
    return get_remote_address(request)

@router.post("/api/reviews")
@limiter.limit("5/hour", key_func=get_user_rate_limit_key)
async def create_review(request: Request, review: ReviewCreate):
    """리뷰 작성 (사용자별 시간당 5회)"""
    return await create_new_review(review)
```

### 4.2 API Key 관리

```python
# api_key.py
import secrets

async def generate_api_key() -> tuple[str, str]:
    """API 키 생성"""
    # 랜덤 키 생성
    api_key = secrets.token_urlsafe(32)
    
    # 해시 저장 (보안)
    key_hash = hashlib.sha256(api_key.encode()).hexdigest()
    
    return api_key, key_hash

@router.post("/api-keys")
async def create_api_key(
    key_data: APIKeyCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """API 키 생성"""
    api_key, key_hash = await generate_api_key()
    
    await db.execute(
        """
        INSERT INTO api_keys (user_id, key_hash, name, permissions, rate_limit, expires_at)
        VALUES ($1, $2, $3, $4, $5, $6)
        """,
        current_user.id,
        key_hash,
        key_data.name,
        json.dumps(key_data.permissions),
        key_data.rate_limit,
        key_data.expires_at
    )
    
    return {
        "api_key": api_key,  # 한 번만 보여줌
        "message": "API 키를 안전한 곳에 저장하세요. 다시 확인할 수 없습니다."
    }

async def verify_api_key(api_key: str, db: Session) -> Optional[User]:
    """API 키 검증"""
    key_hash = hashlib.sha256(api_key.encode()).hexdigest()
    
    row = await db.fetch_one(
        """
        SELECT k.*, u.*
        FROM api_keys k
        JOIN users u ON k.user_id = u.id
        WHERE k.key_hash = $1 
        AND k.is_active = TRUE
        AND (k.expires_at IS NULL OR k.expires_at > NOW())
        """,
        key_hash
    )
    
    if not row:
        return None
    
    # 마지막 사용 시간 업데이트
    await db.execute(
        "UPDATE api_keys SET last_used_at = NOW() WHERE key_hash = $1",
        key_hash
    )
    
    return User(**row)
```

---

## 5. 데이터 보안

### 5.1 데이터 암호화

```python
# encryption.py
from cryptography.fernet import Fernet
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2

# 암호화 키 생성 (환경 변수에서 로드)
ENCRYPTION_KEY = settings.ENCRYPTION_KEY.encode()
fernet = Fernet(ENCRYPTION_KEY)

def encrypt_data(data: str) -> str:
    """데이터 암호화"""
    encrypted = fernet.encrypt(data.encode())
    return encrypted.decode()

def decrypt_data(encrypted_data: str) -> str:
    """데이터 복호화"""
    decrypted = fernet.decrypt(encrypted_data.encode())
    return decrypted.decode()

# 민감 정보 암호화 저장
class EncryptedField:
    """암호화 필드 (SQLAlchemy)"""
    
    def process_bind_param(self, value, dialect):
        if value is not None:
            return encrypt_data(value)
        return value
    
    def process_result_value(self, value, dialect):
        if value is not None:
            return decrypt_data(value)
        return value

# 사용 예시
class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True)
    email = Column(String)
    phone = Column(String)  # 암호화 저장
    social_security_number = Column(String)  # 암호화 저장
```

### 5.2 개인정보 마스킹

```python
# masking.py
def mask_email(email: str) -> str:
    """이메일 마스킹"""
    local, domain = email.split('@')
    if len(local) <= 2:
        masked_local = local[0] + '*' * (len(local) - 1)
    else:
        masked_local = local[0] + '*' * (len(local) - 2) + local[-1]
    
    return f"{masked_local}@{domain}"

def mask_phone(phone: str) -> str:
    """전화번호 마스킹"""
    if len(phone) <= 4:
        return '*' * len(phone)
    return phone[:4] + '*' * (len(phone) - 4)

def mask_card_number(card_number: str) -> str:
    """카드번호 마스킹"""
    return '*' * 12 + card_number[-4:]

# API 응답에서 자동 마스킹
class UserResponse(BaseModel):
    id: int
    email: str
    phone: str
    
    @validator('email')
    def mask_email_field(cls, v):
        return mask_email(v)
    
    @validator('phone')
    def mask_phone_field(cls, v):
        return mask_phone(v) if v else None
```

---

## 6. 보안 로깅 및 모니터링

### 6.1 보안 이벤트 로깅

```python
# security_logging.py
import logging
from enum import Enum

class SecurityEventType(str, Enum):
    LOGIN_SUCCESS = "login_success"
    LOGIN_FAILED = "login_failed"
    LOGOUT = "logout"
    PASSWORD_CHANGED = "password_changed"
    PERMISSION_DENIED = "permission_denied"
    SUSPICIOUS_ACTIVITY = "suspicious_activity"
    DATA_ACCESS = "data_access"
    DATA_EXPORT = "data_export"

async def log_security_event(
    event_type: SecurityEventType,
    user_id: Optional[int],
    ip_address: str,
    user_agent: str,
    severity: str = "info",
    details: dict = None,
    db: Session = None
):
    """보안 이벤트 로깅"""
    await db.execute(
        """
        INSERT INTO security_logs (
            event_type, user_id, ip_address, user_agent, severity, details
        ) VALUES ($1, $2, $3, $4, $5, $6)
        """,
        event_type,
        user_id,
        ip_address,
        user_agent,
        severity,
        json.dumps(details) if details else None
    )
    
    # 심각한 이벤트는 즉시 알림
    if severity in ['critical', 'high']:
        await send_security_alert(event_type, user_id, details)

# 미들웨어로 모든 요청 로깅
@app.middleware("http")
async def log_requests(request: Request, call_next):
    # 요청 시작 시간
    start_time = time.time()
    
    # 요청 처리
    response = await call_next(request)
    
    # 처리 시간 계산
    process_time = time.time() - start_time
    
    # 보안 로그
    if response.status_code >= 400:
        await log_security_event(
            event_type=SecurityEventType.PERMISSION_DENIED if response.status_code == 403 else "error",
            user_id=request.state.user_id if hasattr(request.state, 'user_id') else None,
            ip_address=request.client.host,
            user_agent=request.headers.get('user-agent'),
            severity='medium',
            details={
                "path": request.url.path,
                "method": request.method,
                "status_code": response.status_code,
                "process_time": process_time
            }
        )
    
    return response
```

### 6.2 이상 행위 탐지

```python
# anomaly_detection.py
async def detect_suspicious_activity(user_id: int, db: Session):
    """이상 행위 탐지"""
    
    # 1. 로그인 실패 횟수 확인
    failed_logins = await db.fetch_val(
        """
        SELECT COUNT(*)
        FROM login_history
        WHERE user_id = $1 
        AND success = FALSE
        AND created_at > NOW() - INTERVAL '1 hour'
        """,
        user_id
    )
    
    if failed_logins >= 5:
        await log_security_event(
            SecurityEventType.SUSPICIOUS_ACTIVITY,
            user_id,
            None,
            None,
            severity='high',
            details={"reason": "Multiple failed login attempts"}
        )
    
    # 2. 비정상적인 IP 주소 확인
    recent_ips = await db.fetch_all(
        """
        SELECT DISTINCT ip_address, location
        FROM login_history
        WHERE user_id = $1
        AND created_at > NOW() - INTERVAL '24 hours'
        """,
        user_id
    )
    
    if len(recent_ips) > 5:  # 24시간 내 5개 이상의 다른 IP
        await log_security_event(
            SecurityEventType.SUSPICIOUS_ACTIVITY,
            user_id,
            None,
            None,
            severity='medium',
            details={"reason": "Multiple IPs in short time"}
        )
    
    # 3. 대량 데이터 접근 확인
    data_access_count = await db.fetch_val(
        """
        SELECT COUNT(*)
        FROM audit_logs
        WHERE user_id = $1
        AND action = 'read'
        AND created_at > NOW() - INTERVAL '1 hour'
        """,
        user_id
    )
    
    if data_access_count > 1000:  # 시간당 1000회 초과
        await log_security_event(
            SecurityEventType.SUSPICIOUS_ACTIVITY,
            user_id,
            None,
            None,
            severity='high',
            details={"reason": "Excessive data access"}
        )
        
        # 계정 자동 정지
        await suspend_user_account(user_id, reason="Suspicious activity detected")
```

---

## 7. 보안 체크리스트

### 7.1 개발 단계

- [ ] 모든 비밀번호는 bcrypt로 해싱
- [ ] JWT 토큰에 민감 정보 포함하지 않음
- [ ] SQL Injection 방지 (Prepared Statements)
- [ ] XSS 방지 (입력 검증 및 이스케이프)
- [ ] CSRF 보호 (CSRF 토큰)
- [ ] 입력 검증 (Pydantic 모델)
- [ ] 출력 인코딩
- [ ] 에러 메시지에 민감 정보 노출 방지

### 7.2 배포 단계

- [ ] HTTPS/TLS 1.3 적용
- [ ] 보안 헤더 설정 (HSTS, CSP, X-Frame-Options)
- [ ] 환경 변수로 비밀키 관리
- [ ] 데이터베이스 암호화 (at rest)
- [ ] 백업 암호화
- [ ] 로그 수집 및 모니터링
- [ ] 침입 탐지 시스템 (IDS)
- [ ] DDoS 보호

### 7.3 운영 단계

- [ ] 정기적인 보안 업데이트
- [ ] 취약점 스캐닝
- [ ] 침투 테스트
- [ ] 보안 감사
- [ ] 인시던트 대응 계획
- [ ] 데이터 백업 및 복구 테스트
- [ ] 액세스 로그 검토
- [ ] 권한 정기 검토

---

**문서 관리**
- 작성자: 장재훈
- 최종 업데이트: 2025-11-19
- 다음 검토: 월별 보안 검토 시


