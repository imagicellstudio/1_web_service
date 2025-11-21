# Backend 관리자 시스템 설계

## 문서 정보
- 작성일: 2025-11-19
- 버전: 2.0 (플랫폼)
- 대상: 관리자 대시보드 및 관리 시스템

---

## 1. 관리자 대시보드 구조

### 1.1 전체 레이아웃

```
┌────────────────────────────────────────────────────────────┐
│  HEADER                                                     │
│  Logo | SpicyJump 관리자  |  알림 | 설정 | 관리자명 ▼   │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────┐                                              │
│  │          │  MAIN CONTENT AREA                           │
│  │  SIDE    │                                              │
│  │  MENU    │  ┌────────────────────────────────────────┐ │
│  │          │  │                                        │ │
│  │  - 대시보드│  │  현재 선택된 메뉴의 컨텐츠 영역       │ │
│  │  - 거래   │  │                                        │ │
│  │  - 회원   │  │                                        │ │
│  │  - 상품   │  │                                        │ │
│  │  - 데이터 │  │                                        │ │
│  │  - 표준   │  │                                        │ │
│  │  - 보안   │  │                                        │ │
│  │  - 연계   │  │                                        │ │
│  │  - 설정   │  │                                        │ │
│  │          │  └────────────────────────────────────────┘ │
│  └──────────┘                                              │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

### 1.2 사이드 메뉴 구조

```typescript
const adminMenuStructure = {
  dashboard: {
    icon: 'DashboardIcon',
    label: '대시보드',
    path: '/admin',
    children: []
  },
  transaction: {
    icon: 'TransactionIcon',
    label: '거래 관리',
    path: '/admin/transaction',
    children: [
      { label: '거래 내역', path: '/admin/transaction/list' },
      { label: '결제 관리', path: '/admin/transaction/payments' },
      { label: '정산 관리', path: '/admin/transaction/settlements' },
      { label: '거래 분석', path: '/admin/transaction/analytics' }
    ]
  },
  member: {
    icon: 'UsersIcon',
    label: '회원 관리',
    path: '/admin/member',
    children: [
      { label: '회원 목록', path: '/admin/member/list' },
      { label: '판매자 관리', path: '/admin/member/sellers' },
      { label: '권한 관리', path: '/admin/member/roles' },
      { label: '회원 분석', path: '/admin/member/analytics' }
    ]
  },
  reward: {
    icon: 'AwardIcon',
    label: '라벨/리워드',
    path: '/admin/reward',
    children: [
      { label: '라벨 관리', path: '/admin/reward/labels' },
      { label: '리워드 프로그램', path: '/admin/reward/programs' },
      { label: '포인트 관리', path: '/admin/reward/points' }
    ]
  },
  compliance: {
    icon: 'ShieldIcon',
    label: '규정/개인정보',
    path: '/admin/compliance',
    children: [
      { label: '개인정보 관리', path: '/admin/compliance/privacy' },
      { label: '약관 관리', path: '/admin/compliance/terms' },
      { label: '컴플라이언스', path: '/admin/compliance/reports' }
    ]
  },
  standards: {
    icon: 'DatabaseIcon',
    label: '표준정보 관리',
    path: '/admin/standards',
    children: [
      { label: 'HS Code', path: '/admin/standards/hs-code' },
      { label: 'HACCP', path: '/admin/standards/haccp' },
      { label: '원산지', path: '/admin/standards/origin' },
      { label: '영양성분', path: '/admin/standards/nutrition' },
      { label: '알러지 식품', path: '/admin/standards/allergen' },
      { label: '식약처 정보', path: '/admin/standards/mfds' },
      { label: '농식품 코드', path: '/admin/standards/agri-code' }
    ]
  },
  data: {
    icon: 'ServerIcon',
    label: '데이터 관리',
    path: '/admin/data',
    children: [
      { label: '식품 데이터', path: '/admin/data/food' },
      { label: '원산지 데이터', path: '/admin/data/origin' },
      { label: '거래 데이터', path: '/admin/data/transaction' },
      { label: '블록체인 데이터', path: '/admin/data/blockchain' },
      { label: '회원 데이터', path: '/admin/data/member' },
      { label: '라벨링 데이터', path: '/admin/data/labeling' },
      { label: '데이터 전처리', path: '/admin/data/preprocessing' },
      { label: '데이터 분석', path: '/admin/data/analytics' }
    ]
  },
  security: {
    icon: 'LockIcon',
    label: '보안 관리',
    path: '/admin/security',
    children: [
      { label: '보안 로그', path: '/admin/security/logs' },
      { label: '인증 관리', path: '/admin/security/auth' },
      { label: 'API 키 관리', path: '/admin/security/api-keys' },
      { label: '위협 탐지', path: '/admin/security/threats' }
    ]
  },
  integration: {
    icon: 'LinkIcon',
    label: '연계 관리',
    path: '/admin/integration',
    children: [
      { label: 'API 연동', path: '/admin/integration/api' },
      { label: '결제 연동', path: '/admin/integration/payment' },
      { label: '블록체인 연동', path: '/admin/integration/blockchain' },
      { label: '공공기관 연동', path: '/admin/integration/public' }
    ]
  },
  localization: {
    icon: 'GlobeIcon',
    label: '언어지원',
    path: '/admin/localization',
    children: [
      { label: '언어 관리', path: '/admin/localization/languages' },
      { label: '번역 관리', path: '/admin/localization/translations' },
      { label: '컨텐츠 관리', path: '/admin/localization/content' }
    ]
  },
  settings: {
    icon: 'SettingsIcon',
    label: '시스템 설정',
    path: '/admin/settings',
    children: [
      { label: '일반 설정', path: '/admin/settings/general' },
      { label: '이메일 설정', path: '/admin/settings/email' },
      { label: '알림 설정', path: '/admin/settings/notification' },
      { label: '백업/복구', path: '/admin/settings/backup' }
    ]
  }
};
```

---

## 2. 대시보드 (홈)

### 2.1 KPI 카드

```typescript
// Dashboard.tsx
export const Dashboard: React.FC = () => {
  const { data: kpis } = useQuery('dashboardKPIs', fetchKPIs);
  
  return (
    <DashboardContainer>
      <PageHeader>
        <h1>대시보드</h1>
        <DateRangePicker />
      </PageHeader>
      
      {/* KPI 카드 그리드 */}
      <KPIGrid>
        <KPICard
          title="실시간 사용자"
          value={kpis?.activeUsers}
          icon={<UsersIcon />}
          trend={{ value: 12.5, direction: 'up' }}
          color="blue"
        />
        <KPICard
          title="일일 거래액"
          value={formatCurrency(kpis?.dailyRevenue)}
          icon={<DollarIcon />}
          trend={{ value: 8.3, direction: 'up' }}
          color="green"
        />
        <KPICard
          title="거래 성공률"
          value={`${kpis?.successRate}%`}
          icon={<CheckIcon />}
          trend={{ value: 2.1, direction: 'up' }}
          color="purple"
        />
        <KPICard
          title="평균 응답시간"
          value={`${kpis?.avgResponseTime}ms`}
          icon={<ClockIcon />}
          trend={{ value: 5.0, direction: 'down' }}
          color="orange"
        />
      </KPIGrid>
      
      {/* 차트 섹션 */}
      <ChartsRow>
        <ChartCard title="거래 추이" span={2}>
          <LineChart data={kpis?.transactionTrend} />
        </ChartCard>
        <ChartCard title="결제 수단 분포">
          <PieChart data={kpis?.paymentMethods} />
        </ChartCard>
      </ChartsRow>
      
      <ChartsRow>
        <ChartCard title="국가별 매출">
          <BarChart data={kpis?.revenueByCountry} />
        </ChartCard>
        <ChartCard title="실시간 활동">
          <ActivityFeed activities={kpis?.recentActivities} />
        </ChartCard>
      </ChartsRow>
      
      {/* 시스템 상태 */}
      <SystemStatus>
        <h3>시스템 상태</h3>
        <StatusGrid>
          <StatusItem
            service="API Gateway"
            status="healthy"
            uptime="99.98%"
          />
          <StatusItem
            service="Database"
            status="healthy"
            uptime="99.95%"
          />
          <StatusItem
            service="Blockchain"
            status="healthy"
            uptime="99.92%"
          />
          <StatusItem
            service="Payment Gateway"
            status="warning"
            uptime="99.75%"
          />
        </StatusGrid>
      </SystemStatus>
    </DashboardContainer>
  );
};

// KPI 카드 컴포넌트
const KPICard: React.FC<KPICardProps> = ({
  title,
  value,
  icon,
  trend,
  color
}) => {
  return (
    <KPICardContainer color={color}>
      <KPIHeader>
        <KPITitle>{title}</KPITitle>
        <IconWrapper>{icon}</IconWrapper>
      </KPIHeader>
      <KPIValue>{value}</KPIValue>
      {trend && (
        <KPITrend direction={trend.direction}>
          {trend.direction === 'up' ? '↑' : '↓'} {trend.value}%
          <span>vs 어제</span>
        </KPITrend>
      )}
    </KPICardContainer>
  );
};
```

### 2.2 실시간 활동 피드

```typescript
// ActivityFeed.tsx
export const ActivityFeed: React.FC<{ activities: Activity[] }> = ({
  activities
}) => {
  return (
    <ActivityFeedContainer>
      {activities.map(activity => (
        <ActivityItem key={activity.id}>
          <ActivityIcon type={activity.type} />
          <ActivityContent>
            <ActivityText>{activity.message}</ActivityText>
            <ActivityTime>{formatRelativeTime(activity.timestamp)}</ActivityTime>
          </ActivityContent>
        </ActivityItem>
      ))}
    </ActivityFeedContainer>
  );
};
```

---

## 3. 거래/결제 관리

### 3.1 거래 내역 목록

```typescript
// TransactionList.tsx
export const TransactionList: React.FC = () => {
  const [filters, setFilters] = useState({
    status: 'all',
    dateRange: 'today',
    search: ''
  });
  
  const { data, isLoading } = useQuery(
    ['transactions', filters],
    () => fetchTransactions(filters)
  );
  
  return (
    <PageContainer>
      <PageHeader>
        <h1>거래 내역</h1>
        <ButtonGroup>
          <ExportButton />
          <RefreshButton />
        </ButtonGroup>
      </PageHeader>
      
      {/* 필터 */}
      <FilterBar>
        <Select
          value={filters.status}
          onChange={(val) => setFilters({ ...filters, status: val })}
          options={[
            { value: 'all', label: '전체' },
            { value: 'completed', label: '완료' },
            { value: 'pending', label: '대기' },
            { value: 'cancelled', label: '취소' }
          ]}
        />
        <DateRangePicker
          value={filters.dateRange}
          onChange={(val) => setFilters({ ...filters, dateRange: val })}
        />
        <SearchInput
          placeholder="거래 ID, 사용자명 검색"
          value={filters.search}
          onChange={(val) => setFilters({ ...filters, search: val })}
        />
      </FilterBar>
      
      {/* 통계 요약 */}
      <StatsBar>
        <Stat label="총 거래" value={data?.totalCount} />
        <Stat label="총 금액" value={formatCurrency(data?.totalAmount)} />
        <Stat label="성공률" value={`${data?.successRate}%`} />
      </StatsBar>
      
      {/* 거래 테이블 */}
      <DataTable
        columns={[
          { key: 'id', header: '거래 ID', width: 120 },
          { key: 'buyer', header: '구매자', width: 150 },
          { key: 'seller', header: '판매자', width: 150 },
          { key: 'product', header: '상품', width: 200 },
          { key: 'amount', header: '금액', width: 120, align: 'right' },
          { key: 'status', header: '상태', width: 100, render: renderStatus },
          { key: 'createdAt', header: '거래일시', width: 150 },
          { key: 'actions', header: '작업', width: 100, render: renderActions }
        ]}
        data={data?.transactions}
        loading={isLoading}
        onRowClick={(row) => navigate(`/admin/transaction/${row.id}`)}
      />
      
      <Pagination
        current={data?.page}
        total={data?.totalPages}
        onChange={(page) => navigate(`?page=${page}`)}
      />
    </PageContainer>
  );
};

// 상태 렌더링
const renderStatus = (status: string) => {
  const statusConfig = {
    completed: { color: 'green', label: '완료' },
    pending: { color: 'orange', label: '대기' },
    cancelled: { color: 'red', label: '취소' },
    refunded: { color: 'gray', label: '환불' }
  };
  
  const config = statusConfig[status];
  
  return (
    <StatusBadge color={config.color}>
      {config.label}
    </StatusBadge>
  );
};
```

### 3.2 거래 상세

```typescript
// TransactionDetail.tsx
export const TransactionDetail: React.FC = () => {
  const { id } = useParams();
  const { data: transaction } = useQuery(
    ['transaction', id],
    () => fetchTransactionDetail(id)
  );
  
  return (
    <DetailContainer>
      <DetailHeader>
        <BackButton />
        <h1>거래 상세</h1>
        <StatusBadge>{transaction?.status}</StatusBadge>
      </DetailHeader>
      
      <DetailGrid>
        {/* 기본 정보 */}
        <Card title="기본 정보">
          <InfoRow label="거래 ID" value={transaction?.id} />
          <InfoRow label="거래일시" value={formatDateTime(transaction?.createdAt)} />
          <InfoRow label="결제일시" value={formatDateTime(transaction?.paidAt)} />
          <InfoRow label="상태" value={transaction?.status} />
        </Card>
        
        {/* 구매자 정보 */}
        <Card title="구매자">
          <InfoRow label="이름" value={transaction?.buyer.name} />
          <InfoRow label="이메일" value={transaction?.buyer.email} />
          <InfoRow label="전화번호" value={transaction?.buyer.phone} />
        </Card>
        
        {/* 판매자 정보 */}
        <Card title="판매자">
          <InfoRow label="업체명" value={transaction?.seller.companyName} />
          <InfoRow label="담당자" value={transaction?.seller.contactName} />
          <InfoRow label="연락처" value={transaction?.seller.phone} />
        </Card>
        
        {/* 상품 정보 */}
        <Card title="상품 정보" span={2}>
          <ProductInfo>
            <ProductImage src={transaction?.product.image} />
            <ProductDetails>
              <ProductName>{transaction?.product.name}</ProductName>
              <ProductPrice>{formatCurrency(transaction?.product.price)}</ProductPrice>
              <ProductQuantity>수량: {transaction?.quantity}</ProductQuantity>
            </ProductDetails>
          </ProductInfo>
        </Card>
        
        {/* 결제 정보 */}
        <Card title="결제 정보">
          <InfoRow label="결제 수단" value={transaction?.payment.method} />
          <InfoRow label="상품 금액" value={formatCurrency(transaction?.amount)} />
          <InfoRow label="배송비" value={formatCurrency(transaction?.shippingFee)} />
          <InfoRow label="총 결제 금액" value={formatCurrency(transaction?.totalAmount)} bold />
        </Card>
        
        {/* 블록체인 정보 */}
        {transaction?.blockchainTxHash && (
          <Card title="블록체인 정보">
            <InfoRow label="트랜잭션 해시" value={transaction.blockchainTxHash} />
            <InfoRow label="블록 번호" value={transaction.blockNumber} />
            <InfoRow label="확인 수" value={transaction.confirmations} />
            <Button
              onClick={() => window.open(`/blockchain/${transaction.blockchainTxHash}`)}
            >
              블록체인에서 확인
            </Button>
          </Card>
        )}
        
        {/* 타임라인 */}
        <Card title="거래 타임라인" span={2}>
          <Timeline events={transaction?.timeline} />
        </Card>
      </DetailGrid>
      
      {/* 액션 버튼 */}
      <ActionButtons>
        {transaction?.status === 'pending' && (
          <>
            <Button variant="success" onClick={handleApprove}>승인</Button>
            <Button variant="danger" onClick={handleCancel}>취소</Button>
          </>
        )}
        {transaction?.status === 'completed' && (
          <Button variant="warning" onClick={handleRefund}>환불</Button>
        )}
        <Button variant="secondary" onClick={handleExport}>내보내기</Button>
      </ActionButtons>
    </DetailContainer>
  );
};
```

---

## 4. 회원 관리

### 4.1 회원 목록

```typescript
// MemberList.tsx
export const MemberList: React.FC = () => {
  const [selectedMembers, setSelectedMembers] = useState<number[]>([]);
  
  return (
    <PageContainer>
      <PageHeader>
        <h1>회원 관리</h1>
        <Button onClick={() => navigate('/admin/member/create')}>
          + 회원 추가
        </Button>
      </PageHeader>
      
      {/* 필터 */}
      <FilterBar>
        <Select label="역할" options={roleOptions} />
        <Select label="상태" options={statusOptions} />
        <SearchInput placeholder="이름, 이메일 검색" />
      </FilterBar>
      
      {/* 일괄 작업 */}
      {selectedMembers.length > 0 && (
        <BulkActions>
          <span>{selectedMembers.length}명 선택됨</span>
          <Button onClick={handleBulkActivate}>활성화</Button>
          <Button onClick={handleBulkSuspend}>정지</Button>
          <Button onClick={handleBulkExport}>내보내기</Button>
        </BulkActions>
      )}
      
      {/* 회원 테이블 */}
      <DataTable
        selectable
        selected={selectedMembers}
        onSelectionChange={setSelectedMembers}
        columns={[
          { key: 'id', header: 'ID', width: 80 },
          { key: 'avatar', header: '', width: 60, render: renderAvatar },
          { key: 'name', header: '이름', width: 150 },
          { key: 'email', header: '이메일', width: 200 },
          { key: 'role', header: '역할', width: 100, render: renderRole },
          { key: 'status', header: '상태', width: 100, render: renderStatus },
          { key: 'createdAt', header: '가입일', width: 120 },
          { key: 'lastLoginAt', header: '최근 로그인', width: 120 },
          { key: 'actions', header: '작업', width: 120, render: renderActions }
        ]}
        data={members}
      />
    </PageContainer>
  );
};
```

### 4.2 권한 관리

```typescript
// RoleManagement.tsx
export const RoleManagement: React.FC = () => {
  const [roles, setRoles] = useState<Role[]>([]);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  
  return (
    <RoleManagementContainer>
      <LeftPanel>
        <h2>역할 목록</h2>
        <RoleList>
          {roles.map(role => (
            <RoleItem
              key={role.id}
              active={selectedRole?.id === role.id}
              onClick={() => setSelectedRole(role)}
            >
              <RoleIcon>{role.icon}</RoleIcon>
              <RoleName>{role.name}</RoleName>
              <MemberCount>{role.memberCount}명</MemberCount>
            </RoleItem>
          ))}
        </RoleList>
        <Button onClick={handleCreateRole}>+ 역할 추가</Button>
      </LeftPanel>
      
      <RightPanel>
        {selectedRole && (
          <>
            <RoleHeader>
              <h2>{selectedRole.name}</h2>
              <ButtonGroup>
                <Button onClick={handleEditRole}>수정</Button>
                <Button variant="danger" onClick={handleDeleteRole}>삭제</Button>
              </ButtonGroup>
            </RoleHeader>
            
            <RoleDescription>
              {selectedRole.description}
            </RoleDescription>
            
            <PermissionsSection>
              <h3>권한 설정</h3>
              <PermissionsGrid>
                {permissionCategories.map(category => (
                  <PermissionCategory key={category.id}>
                    <CategoryHeader>{category.name}</CategoryHeader>
                    {category.permissions.map(permission => (
                      <PermissionRow key={permission.id}>
                        <Checkbox
                          checked={selectedRole.permissions.includes(permission.id)}
                          onChange={(checked) => handlePermissionChange(permission.id, checked)}
                        />
                        <PermissionLabel>{permission.label}</PermissionLabel>
                        <PermissionDescription>
                          {permission.description}
                        </PermissionDescription>
                      </PermissionRow>
                    ))}
                  </PermissionCategory>
                ))}
              </PermissionsGrid>
            </PermissionsSection>
            
            <SaveButton onClick={handleSavePermissions}>
              권한 저장
            </SaveButton>
          </>
        )}
      </RightPanel>
    </RoleManagementContainer>
  );
};

// 권한 카테고리 정의
const permissionCategories = [
  {
    id: 'transaction',
    name: '거래 관리',
    permissions: [
      { id: 'transaction.read', label: '조회', description: '거래 내역 조회' },
      { id: 'transaction.update', label: '수정', description: '거래 상태 변경' },
      { id: 'transaction.delete', label: '삭제', description: '거래 삭제' },
      { id: 'transaction.refund', label: '환불', description: '거래 환불 처리' }
    ]
  },
  {
    id: 'member',
    name: '회원 관리',
    permissions: [
      { id: 'member.read', label: '조회', description: '회원 정보 조회' },
      { id: 'member.create', label: '생성', description: '회원 추가' },
      { id: 'member.update', label: '수정', description: '회원 정보 수정' },
      { id: 'member.delete', label: '삭제', description: '회원 삭제' },
      { id: 'member.suspend', label: '정지', description: '회원 정지/해제' }
    ]
  },
  {
    id: 'product',
    name: '상품 관리',
    permissions: [
      { id: 'product.read', label: '조회', description: '상품 조회' },
      { id: 'product.create', label: '등록', description: '상품 등록' },
      { id: 'product.update', label: '수정', description: '상품 수정' },
      { id: 'product.delete', label: '삭제', description: '상품 삭제' }
    ]
  },
  {
    id: 'data',
    name: '데이터 관리',
    permissions: [
      { id: 'data.read', label: '조회', description: '데이터 조회' },
      { id: 'data.export', label: '내보내기', description: '데이터 내보내기' },
      { id: 'data.import', label: '가져오기', description: '데이터 가져오기' },
      { id: 'data.delete', label: '삭제', description: '데이터 삭제' }
    ]
  },
  {
    id: 'system',
    name: '시스템 관리',
    permissions: [
      { id: 'system.settings', label: '설정', description: '시스템 설정 변경' },
      { id: 'system.logs', label: '로그', description: '시스템 로그 조회' },
      { id: 'system.backup', label: '백업', description: '백업/복구' }
    ]
  }
];
```

---

## 5. 표준정보 관리

### 5.1 HS Code 관리

```typescript
// HSCodeManagement.tsx
export const HSCodeManagement: React.FC = () => {
  const [codes, setCodes] = useState<HSCode[]>([]);
  const [selectedCode, setSelectedCode] = useState<HSCode | null>(null);
  
  return (
    <HSCodeContainer>
      <PageHeader>
        <h1>HS Code 관리</h1>
        <Button onClick={handleImportCodes}>코드 가져오기</Button>
      </PageHeader>
      
      <FilterBar>
        <Input placeholder="HS Code 검색" />
        <Select label="류(Chapter)" options={chapterOptions} />
        <Button onClick={handleSearch}>검색</Button>
      </FilterBar>
      
      <HSCodeTree>
        {codes.map(code => (
          <TreeNode
            key={code.id}
            code={code}
            selected={selectedCode?.id === code.id}
            onClick={() => setSelectedCode(code)}
            onExpand={handleExpand}
          />
        ))}
      </HSCodeTree>
      
      {selectedCode && (
        <HSCodeDetailPanel>
          <h3>{selectedCode.code}</h3>
          <InfoRow label="설명(한글)" value={selectedCode.descriptionKo} />
          <InfoRow label="설명(영문)" value={selectedCode.descriptionEn} />
          <InfoRow label="기본세율" value={`${selectedCode.tariffRate}%`} />
          <InfoRow label="단위" value={selectedCode.unit} />
          
          <h4>FTA 협정 세율</h4>
          <Table>
            {selectedCode.ftaRates.map(fta => (
              <tr key={fta.country}>
                <td>{fta.country}</td>
                <td>{fta.ftaName}</td>
                <td>{fta.rate}%</td>
              </tr>
            ))}
          </Table>
          
          <ButtonGroup>
            <Button onClick={handleEdit}>수정</Button>
            <Button variant="danger" onClick={handleDelete}>삭제</Button>
          </ButtonGroup>
        </HSCodeDetailPanel>
      )}
    </HSCodeContainer>
  );
};
```

---

## 6. 데이터 관리

### 6.1 데이터 전처리 관리

```typescript
// DataPreprocessing.tsx
export const DataPreprocessing: React.FC = () => {
  const [jobs, setJobs] = useState<PreprocessingJob[]>([]);
  
  return (
    <DataPreprocessingContainer>
      <PageHeader>
        <h1>데이터 전처리</h1>
        <Button onClick={handleCreateJob}>+ 새 작업</Button>
      </PageHeader>
      
      <JobList>
        {jobs.map(job => (
          <JobCard key={job.id}>
            <JobHeader>
              <JobName>{job.name}</JobName>
              <StatusBadge status={job.status}>{job.status}</StatusBadge>
            </JobHeader>
            <JobInfo>
              <InfoItem>
                <label>데이터 유형:</label>
                <span>{job.dataType}</span>
              </InfoItem>
              <InfoItem>
                <label>작업:</label>
                <span>{job.operations.join(', ')}</span>
              </InfoItem>
              <InfoItem>
                <label>처리 레코드:</label>
                <span>{job.recordsProcessed.toLocaleString()}</span>
              </InfoItem>
              <InfoItem>
                <label>실행 시간:</label>
                <span>{formatDuration(job.duration)}</span>
              </InfoItem>
            </JobInfo>
            <ProgressBar value={job.progress} />
            <JobActions>
              <Button onClick={() => handleViewLog(job.id)}>로그 보기</Button>
              {job.status === 'completed' && (
                <Button onClick={() => handleDownload(job.id)}>결과 다운로드</Button>
              )}
              {job.status === 'running' && (
                <Button variant="danger" onClick={() => handleCancel(job.id)}>
                  취소
                </Button>
              )}
            </JobActions>
          </JobCard>
        ))}
      </JobList>
    </DataPreprocessingContainer>
  );
};
```

---

## 7. 보안 관리

### 7.1 보안 로그 모니터링

```typescript
// SecurityLogs.tsx
export const SecurityLogs: React.FC = () => {
  const [logs, setLogs] = useState<SecurityLog[]>([]);
  const [filters, setFilters] = useState({
    severity: 'all',
    eventType: 'all',
    dateRange: 'today'
  });
  
  return (
    <SecurityLogsContainer>
      <PageHeader>
        <h1>보안 로그</h1>
        <ButtonGroup>
          <Button onClick={handleExport}>내보내기</Button>
          <Button onClick={handleRefresh}>새로고침</Button>
        </ButtonGroup>
      </PageHeader>
      
      <FilterBar>
        <Select
          label="심각도"
          value={filters.severity}
          onChange={(val) => setFilters({ ...filters, severity: val })}
          options={[
            { value: 'all', label: '전체' },
            { value: 'critical', label: '치명적' },
            { value: 'high', label: '높음' },
            { value: 'medium', label: '중간' },
            { value: 'low', label: '낮음' }
          ]}
        />
        <Select
          label="이벤트 유형"
          options={eventTypeOptions}
        />
        <DateRangePicker />
      </FilterBar>
      
      <SecurityLogsTable>
        {logs.map(log => (
          <LogRow key={log.id} severity={log.severity}>
            <LogTime>{formatDateTime(log.timestamp)}</LogTime>
            <LogSeverity severity={log.severity}>
              {log.severity}
            </LogSeverity>
            <LogEventType>{log.eventType}</LogEventType>
            <LogMessage>{log.message}</LogMessage>
            <LogUser>{log.user}</LogUser>
            <LogIP>{log.ipAddress}</LogIP>
            <LogActions>
              <Button size="small" onClick={() => handleViewDetail(log.id)}>
                상세
              </Button>
            </LogActions>
          </LogRow>
        ))}
      </SecurityLogsTable>
    </SecurityLogsContainer>
  );
};
```

---

## 8. 연계 관리

### 8.1 API 연동 모니터링

```typescript
// APIIntegrationMonitoring.tsx
export const APIIntegrationMonitoring: React.FC = () => {
  const { data: integrations } = useQuery('integrations', fetchIntegrations);
  
  return (
    <IntegrationContainer>
      <PageHeader>
        <h1>API 연동 관리</h1>
        <Button onClick={handleAddIntegration}>+ 연동 추가</Button>
      </PageHeader>
      
      <IntegrationGrid>
        {integrations?.map(integration => (
          <IntegrationCard key={integration.id}>
            <CardHeader>
              <IntegrationLogo src={integration.logo} />
              <IntegrationName>{integration.name}</IntegrationName>
              <StatusIndicator status={integration.status} />
            </CardHeader>
            
            <CardBody>
              <MetricRow>
                <MetricLabel>마지막 동기화</MetricLabel>
                <MetricValue>
                  {formatRelativeTime(integration.lastSync)}
                </MetricValue>
              </MetricRow>
              <MetricRow>
                <MetricLabel>성공률</MetricLabel>
                <MetricValue>{integration.successRate}%</MetricValue>
              </MetricRow>
              <MetricRow>
                <MetricLabel>오늘 호출</MetricLabel>
                <MetricValue>{integration.todayCalls.toLocaleString()}</MetricValue>
              </MetricRow>
              <MetricRow>
                <MetricLabel>평균 응답시간</MetricLabel>
                <MetricValue>{integration.avgResponseTime}ms</MetricValue>
              </MetricRow>
            </CardBody>
            
            <CardActions>
              <Button onClick={() => handleSync(integration.id)}>
                동기화
              </Button>
              <Button onClick={() => handleSettings(integration.id)}>
                설정
              </Button>
              <Button onClick={() => handleLogs(integration.id)}>
                로그
              </Button>
            </CardActions>
          </IntegrationCard>
        ))}
      </IntegrationGrid>
    </IntegrationContainer>
  );
};
```

---

## 9. 언어지원 관리

### 9.1 번역 관리

```typescript
// TranslationManagement.tsx
export const TranslationManagement: React.FC = () => {
  const [selectedLang, setSelectedLang] = useState('ko');
  const [translations, setTranslations] = useState<Translation[]>([]);
  
  return (
    <TranslationContainer>
      <PageHeader>
        <h1>번역 관리</h1>
        <ButtonGroup>
          <Button onClick={handleAutoTranslate}>자동 번역</Button>
          <Button onClick={handleImport}>가져오기</Button>
          <Button onClick={handleExport}>내보내기</Button>
        </ButtonGroup>
      </PageHeader>
      
      {/* 언어 탭 */}
      <LanguageTabs>
        {languages.map(lang => (
          <LanguageTab
            key={lang.code}
            active={selectedLang === lang.code}
            onClick={() => setSelectedLang(lang.code)}
          >
            {lang.flag} {lang.name}
            <Coverage>{lang.coverage}%</Coverage>
          </LanguageTab>
        ))}
      </LanguageTabs>
      
      {/* 번역 테이블 */}
      <TranslationTable>
        <thead>
          <tr>
            <th>키</th>
            <th>한국어 (기준)</th>
            <th>{languages.find(l => l.code === selectedLang)?.name}</th>
            <th>상태</th>
            <th>작업</th>
          </tr>
        </thead>
        <tbody>
          {translations.map(trans => (
            <tr key={trans.key}>
              <td><Code>{trans.key}</Code></td>
              <td>{trans.ko}</td>
              <td>
                <TranslationInput
                  value={trans[selectedLang]}
                  onChange={(val) => handleUpdate(trans.key, selectedLang, val)}
                />
              </td>
              <td>
                <StatusBadge status={trans.status}>
                  {trans.status}
                </StatusBadge>
              </td>
              <td>
                <Button size="small" onClick={() => handleSave(trans.key)}>
                  저장
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </TranslationTable>
    </TranslationContainer>
  );
};
```

---

**문서 관리**
- 작성자: 장재훈
- 최종 업데이트: 2025-11-19
- 다음 단계: 실제 구현 시작


