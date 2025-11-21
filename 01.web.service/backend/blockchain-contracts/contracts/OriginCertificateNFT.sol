// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/access/AccessControl.sol";
import "@openzeppelin/contracts/utils/Counters.sol";

/**
 * @title OriginCertificateNFT
 * @dev 원산지 인증서 NFT
 * 
 * 식품의 원산지, HACCP 인증, 유기농 인증 등을 블록체인에 기록하여
 * 위변조 불가능한 인증서를 발행합니다.
 */
contract OriginCertificateNFT is ERC721, ERC721URIStorage, AccessControl {
    using Counters for Counters.Counter;
    
    bytes32 public constant VERIFIER_ROLE = keccak256("VERIFIER_ROLE");
    Counters.Counter private _tokenIdCounter;
    
    struct Certificate {
        uint256 tokenId;
        uint256 productId;          // 플랫폼 상품 ID
        string productName;         // 상품명
        string farmName;            // 농장명
        string location;            // 위치 (주소)
        string farmerName;          // 농부명
        uint256 harvestDate;        // 수확일 (timestamp)
        bool haccpCertified;        // HACCP 인증 여부
        bool organicCertified;      // 유기농 인증 여부
        string foodCode;            // 식품코드 (식품의약품안전처)
        string[] certificationImages; // 인증서 이미지 URI (IPFS)
        uint256 issuedAt;           // 발행일
        address issuer;             // 발행자 (검증자)
        bool isActive;              // 활성 상태
    }
    
    // 토큰 ID → 인증서
    mapping(uint256 => Certificate) public certificates;
    
    // 상품 ID → 토큰 ID (한 상품당 하나의 NFT)
    mapping(uint256 => uint256) public productToToken;
    
    // 이벤트
    event CertificateIssued(
        uint256 indexed tokenId,
        uint256 indexed productId,
        address indexed farmer,
        string productName
    );
    
    event CertificateRevoked(
        uint256 indexed tokenId,
        uint256 indexed productId,
        string reason
    );
    
    event CertificateUpdated(
        uint256 indexed tokenId,
        uint256 indexed productId
    );
    
    constructor() ERC721("K-Food Origin Certificate", "KFOC") {
        _grantRole(DEFAULT_ADMIN_ROLE, msg.sender);
        _grantRole(VERIFIER_ROLE, msg.sender);
    }
    
    /**
     * @dev 원산지 인증서 발행
     * @param farmer 농부 주소 (NFT 소유자)
     * @param productId 상품 ID
     * @param productName 상품명
     * @param farmName 농장명
     * @param location 위치
     * @param farmerName 농부명
     * @param harvestDate 수확일
     * @param haccpCertified HACCP 인증 여부
     * @param organicCertified 유기농 인증 여부
     * @param foodCode 식품코드
     * @param certificationImages 인증서 이미지 URI
     * @param tokenURI 메타데이터 URI (IPFS)
     */
    function issueCertificate(
        address farmer,
        uint256 productId,
        string memory productName,
        string memory farmName,
        string memory location,
        string memory farmerName,
        uint256 harvestDate,
        bool haccpCertified,
        bool organicCertified,
        string memory foodCode,
        string[] memory certificationImages,
        string memory tokenURI
    ) external onlyRole(VERIFIER_ROLE) returns (uint256) {
        require(farmer != address(0), "Invalid farmer address");
        require(productToToken[productId] == 0, "Certificate already exists for this product");
        
        uint256 tokenId = _tokenIdCounter.current();
        _tokenIdCounter.increment();
        
        _safeMint(farmer, tokenId);
        _setTokenURI(tokenId, tokenURI);
        
        certificates[tokenId] = Certificate({
            tokenId: tokenId,
            productId: productId,
            productName: productName,
            farmName: farmName,
            location: location,
            farmerName: farmerName,
            harvestDate: harvestDate,
            haccpCertified: haccpCertified,
            organicCertified: organicCertified,
            foodCode: foodCode,
            certificationImages: certificationImages,
            issuedAt: block.timestamp,
            issuer: msg.sender,
            isActive: true
        });
        
        productToToken[productId] = tokenId;
        
        emit CertificateIssued(tokenId, productId, farmer, productName);
        
        return tokenId;
    }
    
    /**
     * @dev 인증서 취소 (위조 발견, 인증 만료 등)
     */
    function revokeCertificate(
        uint256 tokenId,
        string memory reason
    ) external onlyRole(VERIFIER_ROLE) {
        require(_exists(tokenId), "Certificate does not exist");
        require(certificates[tokenId].isActive, "Certificate already revoked");
        
        certificates[tokenId].isActive = false;
        
        emit CertificateRevoked(tokenId, certificates[tokenId].productId, reason);
    }
    
    /**
     * @dev 인증서 정보 업데이트 (인증 추가 등)
     */
    function updateCertificate(
        uint256 tokenId,
        bool haccpCertified,
        bool organicCertified,
        string[] memory certificationImages
    ) external onlyRole(VERIFIER_ROLE) {
        require(_exists(tokenId), "Certificate does not exist");
        
        Certificate storage cert = certificates[tokenId];
        cert.haccpCertified = haccpCertified;
        cert.organicCertified = organicCertified;
        cert.certificationImages = certificationImages;
        
        emit CertificateUpdated(tokenId, cert.productId);
    }
    
    /**
     * @dev 상품 ID로 인증서 조회
     */
    function getCertificateByProductId(uint256 productId) 
        external 
        view 
        returns (Certificate memory) 
    {
        uint256 tokenId = productToToken[productId];
        require(tokenId != 0, "Certificate not found for this product");
        return certificates[tokenId];
    }
    
    /**
     * @dev 인증서 검증 (활성 상태 확인)
     */
    function verifyCertificate(uint256 tokenId) 
        external 
        view 
        returns (bool) 
    {
        return _exists(tokenId) && certificates[tokenId].isActive;
    }
    
    /**
     * @dev 검증자 추가
     */
    function addVerifier(address verifier) external onlyRole(DEFAULT_ADMIN_ROLE) {
        grantRole(VERIFIER_ROLE, verifier);
    }
    
    /**
     * @dev 검증자 제거
     */
    function removeVerifier(address verifier) external onlyRole(DEFAULT_ADMIN_ROLE) {
        revokeRole(VERIFIER_ROLE, verifier);
    }
    
    // Override functions
    function _burn(uint256 tokenId) internal override(ERC721, ERC721URIStorage) {
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




