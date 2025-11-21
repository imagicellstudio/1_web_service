// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/access/AccessControl.sol";
import "@openzeppelin/contracts/utils/Counters.sol";

/**
 * @title MembershipNFT
 * @dev 멤버십 NFT
 * 
 * 등급별 멤버십 NFT를 발행하고, 등급에 따른 혜택을 제공합니다.
 * 멤버십은 P2P 거래가 가능하며, 유효기간이 있습니다.
 */
contract MembershipNFT is ERC721, ERC721URIStorage, AccessControl {
    using Counters for Counters.Counter;
    
    bytes32 public constant ISSUER_ROLE = keccak256("ISSUER_ROLE");
    Counters.Counter private _tokenIdCounter;
    
    enum MembershipTier {
        BRONZE,     // 브론즈
        SILVER,     // 실버
        GOLD,       // 골드
        PLATINUM,   // 플래티넘
        DIAMOND     // 다이아몬드
    }
    
    struct Membership {
        uint256 tokenId;
        MembershipTier tier;        // 등급
        address holder;             // 소유자
        uint256 issuedAt;           // 발행일
        uint256 expiresAt;          // 만료일
        bool isActive;              // 활성 상태
        uint256 discountRate;       // 할인율 (basis points, 5000 = 50%)
        bool priorityAccess;        // 우선 구매권
        bool exclusiveCommunity;    // 전용 커뮤니티 접근
        uint256 monthlyTokens;      // 월간 토큰 에어드랍
    }
    
    // 토큰 ID → 멤버십
    mapping(uint256 => Membership) public memberships;
    
    // 사용자 → 토큰 ID (한 사용자당 하나의 멤버십)
    mapping(address => uint256) public userMembership;
    
    // 등급별 혜택 설정
    struct TierBenefits {
        uint256 discountRate;
        bool priorityAccess;
        bool exclusiveCommunity;
        uint256 monthlyTokens;
        uint256 durationDays;       // 유효 기간 (일)
    }
    
    mapping(MembershipTier => TierBenefits) public tierBenefits;
    
    // 이벤트
    event MembershipIssued(
        uint256 indexed tokenId,
        address indexed holder,
        MembershipTier tier,
        uint256 expiresAt
    );
    
    event MembershipRenewed(
        uint256 indexed tokenId,
        uint256 newExpiresAt
    );
    
    event MembershipUpgraded(
        uint256 indexed tokenId,
        MembershipTier oldTier,
        MembershipTier newTier
    );
    
    event MembershipRevoked(
        uint256 indexed tokenId,
        string reason
    );
    
    constructor() ERC721("K-Food Membership", "KFMB") {
        _grantRole(DEFAULT_ADMIN_ROLE, msg.sender);
        _grantRole(ISSUER_ROLE, msg.sender);
        
        // 등급별 혜택 초기화
        _initializeTierBenefits();
    }
    
    /**
     * @dev 등급별 혜택 초기화
     */
    function _initializeTierBenefits() private {
        // BRONZE
        tierBenefits[MembershipTier.BRONZE] = TierBenefits({
            discountRate: 1000,         // 10%
            priorityAccess: false,
            exclusiveCommunity: false,
            monthlyTokens: 10 * 10**18, // 10 XLCFI
            durationDays: 30
        });
        
        // SILVER
        tierBenefits[MembershipTier.SILVER] = TierBenefits({
            discountRate: 2000,         // 20%
            priorityAccess: false,
            exclusiveCommunity: true,
            monthlyTokens: 25 * 10**18, // 25 XLCFI
            durationDays: 90
        });
        
        // GOLD
        tierBenefits[MembershipTier.GOLD] = TierBenefits({
            discountRate: 3000,         // 30%
            priorityAccess: true,
            exclusiveCommunity: true,
            monthlyTokens: 50 * 10**18, // 50 XLCFI
            durationDays: 180
        });
        
        // PLATINUM
        tierBenefits[MembershipTier.PLATINUM] = TierBenefits({
            discountRate: 4000,         // 40%
            priorityAccess: true,
            exclusiveCommunity: true,
            monthlyTokens: 100 * 10**18, // 100 XLCFI
            durationDays: 365
        });
        
        // DIAMOND
        tierBenefits[MembershipTier.DIAMOND] = TierBenefits({
            discountRate: 5000,         // 50%
            priorityAccess: true,
            exclusiveCommunity: true,
            monthlyTokens: 200 * 10**18, // 200 XLCFI
            durationDays: 365
        });
    }
    
    /**
     * @dev 멤버십 발행
     */
    function issueMembership(
        address holder,
        MembershipTier tier,
        string memory tokenURI
    ) external onlyRole(ISSUER_ROLE) returns (uint256) {
        require(holder != address(0), "Invalid holder address");
        require(userMembership[holder] == 0, "User already has membership");
        
        uint256 tokenId = _tokenIdCounter.current();
        _tokenIdCounter.increment();
        
        _safeMint(holder, tokenId);
        _setTokenURI(tokenId, tokenURI);
        
        TierBenefits memory benefits = tierBenefits[tier];
        uint256 expiresAt = block.timestamp + (benefits.durationDays * 1 days);
        
        memberships[tokenId] = Membership({
            tokenId: tokenId,
            tier: tier,
            holder: holder,
            issuedAt: block.timestamp,
            expiresAt: expiresAt,
            isActive: true,
            discountRate: benefits.discountRate,
            priorityAccess: benefits.priorityAccess,
            exclusiveCommunity: benefits.exclusiveCommunity,
            monthlyTokens: benefits.monthlyTokens
        });
        
        userMembership[holder] = tokenId;
        
        emit MembershipIssued(tokenId, holder, tier, expiresAt);
        
        return tokenId;
    }
    
    /**
     * @dev 멤버십 갱신
     */
    function renewMembership(uint256 tokenId) 
        external 
        onlyRole(ISSUER_ROLE) 
    {
        require(_exists(tokenId), "Membership does not exist");
        
        Membership storage membership = memberships[tokenId];
        TierBenefits memory benefits = tierBenefits[membership.tier];
        
        uint256 newExpiresAt = block.timestamp + (benefits.durationDays * 1 days);
        membership.expiresAt = newExpiresAt;
        membership.isActive = true;
        
        emit MembershipRenewed(tokenId, newExpiresAt);
    }
    
    /**
     * @dev 멤버십 업그레이드
     */
    function upgradeMembership(uint256 tokenId, MembershipTier newTier) 
        external 
        onlyRole(ISSUER_ROLE) 
    {
        require(_exists(tokenId), "Membership does not exist");
        require(uint8(newTier) > uint8(memberships[tokenId].tier), "Not an upgrade");
        
        Membership storage membership = memberships[tokenId];
        MembershipTier oldTier = membership.tier;
        
        membership.tier = newTier;
        
        TierBenefits memory benefits = tierBenefits[newTier];
        membership.discountRate = benefits.discountRate;
        membership.priorityAccess = benefits.priorityAccess;
        membership.exclusiveCommunity = benefits.exclusiveCommunity;
        membership.monthlyTokens = benefits.monthlyTokens;
        
        emit MembershipUpgraded(tokenId, oldTier, newTier);
    }
    
    /**
     * @dev 멤버십 취소
     */
    function revokeMembership(uint256 tokenId, string memory reason) 
        external 
        onlyRole(ISSUER_ROLE) 
    {
        require(_exists(tokenId), "Membership does not exist");
        
        memberships[tokenId].isActive = false;
        
        emit MembershipRevoked(tokenId, reason);
    }
    
    /**
     * @dev 멤버십 유효성 확인
     */
    function isValidMembership(uint256 tokenId) 
        external 
        view 
        returns (bool) 
    {
        if (!_exists(tokenId)) return false;
        
        Membership memory membership = memberships[tokenId];
        return membership.isActive && block.timestamp < membership.expiresAt;
    }
    
    /**
     * @dev 사용자의 멤버십 조회
     */
    function getUserMembership(address user) 
        external 
        view 
        returns (Membership memory) 
    {
        uint256 tokenId = userMembership[user];
        require(tokenId != 0, "User has no membership");
        return memberships[tokenId];
    }
    
    /**
     * @dev 등급별 혜택 업데이트
     */
    function updateTierBenefits(
        MembershipTier tier,
        uint256 discountRate,
        bool priorityAccess,
        bool exclusiveCommunity,
        uint256 monthlyTokens,
        uint256 durationDays
    ) external onlyRole(DEFAULT_ADMIN_ROLE) {
        tierBenefits[tier] = TierBenefits({
            discountRate: discountRate,
            priorityAccess: priorityAccess,
            exclusiveCommunity: exclusiveCommunity,
            monthlyTokens: monthlyTokens,
            durationDays: durationDays
        });
    }
    
    /**
     * @dev 발행자 추가
     */
    function addIssuer(address issuer) external onlyRole(DEFAULT_ADMIN_ROLE) {
        grantRole(ISSUER_ROLE, issuer);
    }
    
    /**
     * @dev 발행자 제거
     */
    function removeIssuer(address issuer) external onlyRole(DEFAULT_ADMIN_ROLE) {
        revokeRole(ISSUER_ROLE, issuer);
    }
    
    // Override transfer to update userMembership mapping
    function _transfer(
        address from,
        address to,
        uint256 tokenId
    ) internal override {
        super._transfer(from, to, tokenId);
        
        // Update mappings
        userMembership[from] = 0;
        userMembership[to] = tokenId;
        memberships[tokenId].holder = to;
    }
    
    // Override functions
    function _burn(uint256 tokenId) 
        internal 
        override(ERC721, ERC721URIStorage) 
    {
        address owner = ownerOf(tokenId);
        userMembership[owner] = 0;
        super._burn(tokenId);
    }
    
    function tokenURI(uint256 tokenId)
        public
        view
        override(ERC721, ERC721URIStorage)
        returns (string memory)
    {
        return super.tokenURI(tokenId);
    }
    
    function supportsInterface(bytes4 interfaceId)
        public
        view
        override(ERC721, ERC721URIStorage, AccessControl)
        returns (bool)
    {
        return super.supportsInterface(interfaceId);
    }
}

