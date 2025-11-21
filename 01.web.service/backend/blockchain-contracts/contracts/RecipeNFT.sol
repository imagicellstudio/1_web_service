// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721Royalty.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/Counters.sol";

/**
 * @title RecipeNFT
 * @dev 레시피 NFT with 로열티
 * 
 * 크리에이터가 레시피를 NFT로 발행하고, 2차 판매 시 로열티를 받을 수 있습니다.
 * ERC-2981 표준을 사용하여 마켓플레이스에서 자동으로 로열티가 지급됩니다.
 */
contract RecipeNFT is ERC721, ERC721URIStorage, ERC721Royalty, Ownable {
    using Counters for Counters.Counter;
    
    Counters.Counter private _tokenIdCounter;
    
    struct Recipe {
        uint256 tokenId;
        string name;                // 레시피명
        address creator;            // 크리에이터
        string category;            // 카테고리 (한식, 중식, 양식 등)
        string difficulty;          // 난이도 (쉬움, 보통, 어려움)
        uint256 cookingTime;        // 조리 시간 (분)
        uint256 servings;           // 인분
        string[] ingredients;       // 재료 목록
        string[] steps;             // 조리 순서
        string imageUri;            // 완성 이미지 URI (IPFS)
        string videoUri;            // 조리 영상 URI (IPFS, 선택)
        uint96 royaltyPercentage;   // 로열티 (basis points, 500 = 5%)
        uint256 createdAt;          // 생성일
        uint256 price;              // 판매 가격 (XLCFI Token)
        bool isForSale;             // 판매 중 여부
    }
    
    // 토큰 ID → 레시피
    mapping(uint256 => Recipe) public recipes;
    
    // 크리에이터 → 토큰 ID 목록
    mapping(address => uint256[]) public creatorRecipes;
    
    // 플랫폼 수수료 (basis points, 250 = 2.5%)
    uint96 public platformFee = 250;
    address public platformWallet;
    
    // 이벤트
    event RecipeMinted(
        uint256 indexed tokenId,
        address indexed creator,
        string name,
        uint256 price
    );
    
    event RecipeSold(
        uint256 indexed tokenId,
        address indexed seller,
        address indexed buyer,
        uint256 price
    );
    
    event RecipePriceUpdated(
        uint256 indexed tokenId,
        uint256 oldPrice,
        uint256 newPrice
    );
    
    event RecipeSaleStatusUpdated(
        uint256 indexed tokenId,
        bool isForSale
    );
    
    constructor(address _platformWallet) ERC721("K-Food Recipe NFT", "KFRP") {
        platformWallet = _platformWallet;
    }
    
    /**
     * @dev 레시피 NFT 발행
     */
    function mintRecipe(
        string memory name,
        string memory category,
        string memory difficulty,
        uint256 cookingTime,
        uint256 servings,
        string[] memory ingredients,
        string[] memory steps,
        string memory imageUri,
        string memory videoUri,
        string memory tokenURI,
        uint96 royaltyPercentage,
        uint256 price
    ) external returns (uint256) {
        require(bytes(name).length > 0, "Name is required");
        require(ingredients.length > 0, "Ingredients are required");
        require(steps.length > 0, "Steps are required");
        require(royaltyPercentage <= 1000, "Royalty too high (max 10%)");
        
        uint256 tokenId = _tokenIdCounter.current();
        _tokenIdCounter.increment();
        
        _safeMint(msg.sender, tokenId);
        _setTokenURI(tokenId, tokenURI);
        _setTokenRoyalty(tokenId, msg.sender, royaltyPercentage);
        
        recipes[tokenId] = Recipe({
            tokenId: tokenId,
            name: name,
            creator: msg.sender,
            category: category,
            difficulty: difficulty,
            cookingTime: cookingTime,
            servings: servings,
            ingredients: ingredients,
            steps: steps,
            imageUri: imageUri,
            videoUri: videoUri,
            royaltyPercentage: royaltyPercentage,
            createdAt: block.timestamp,
            price: price,
            isForSale: price > 0
        });
        
        creatorRecipes[msg.sender].push(tokenId);
        
        emit RecipeMinted(tokenId, msg.sender, name, price);
        
        return tokenId;
    }
    
    /**
     * @dev 레시피 가격 업데이트
     */
    function updateRecipePrice(uint256 tokenId, uint256 newPrice) external {
        require(ownerOf(tokenId) == msg.sender, "Not the owner");
        
        uint256 oldPrice = recipes[tokenId].price;
        recipes[tokenId].price = newPrice;
        
        emit RecipePriceUpdated(tokenId, oldPrice, newPrice);
    }
    
    /**
     * @dev 판매 상태 업데이트
     */
    function setForSale(uint256 tokenId, bool isForSale) external {
        require(ownerOf(tokenId) == msg.sender, "Not the owner");
        
        recipes[tokenId].isForSale = isForSale;
        
        emit RecipeSaleStatusUpdated(tokenId, isForSale);
    }
    
    /**
     * @dev 크리에이터의 레시피 목록 조회
     */
    function getCreatorRecipes(address creator) 
        external 
        view 
        returns (uint256[] memory) 
    {
        return creatorRecipes[creator];
    }
    
    /**
     * @dev 판매 중인 레시피 수 조회
     */
    function getForSaleCount() external view returns (uint256) {
        uint256 count = 0;
        uint256 totalSupply = _tokenIdCounter.current();
        
        for (uint256 i = 0; i < totalSupply; i++) {
            if (_exists(i) && recipes[i].isForSale) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * @dev 플랫폼 수수료 업데이트
     */
    function updatePlatformFee(uint96 newFee) external onlyOwner {
        require(newFee <= 1000, "Fee too high (max 10%)");
        platformFee = newFee;
    }
    
    /**
     * @dev 플랫폼 지갑 업데이트
     */
    function updatePlatformWallet(address newWallet) external onlyOwner {
        require(newWallet != address(0), "Invalid address");
        platformWallet = newWallet;
    }
    
    // Override functions
    function _burn(uint256 tokenId) 
        internal 
        override(ERC721, ERC721URIStorage, ERC721Royalty) 
    {
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
        override(ERC721, ERC721URIStorage, ERC721Royalty)
        returns (bool)
    {
        return super.supportsInterface(interfaceId);
    }
}


