"use client";

import React, { createContext, useContext, useState, useEffect } from 'react';

type Language = 'ko' | 'en';

interface LanguageContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: string) => string;
}

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export const translations = {
  ko: {
    "nav.home": "홈",
    "nav.shop": "쇼핑",
    "nav.nft": "NFT",
    "nav.about": "소개",
    "hero.title": "매운맛의 새로운 차원",
    "hero.subtitle": "프리미엄 K-Food와 블록체인의 만남. 미각을 깨우는 매운맛과 디지털 자산의 특별한 경험을 만나보세요.",
    "hero.cta": "지금 시작하기",
    "hero.learnMore": "더 알아보기",
    "featured.title": "인기 상품",
    "featured.subtitle": "SpicyJump가 엄선한 프리미엄 매운맛 컬렉션",
    "nft.title": "SpicyJump NFT",
    "nft.subtitle": "매운맛을 즐기는 새로운 방법, 멤버십 혜택과 함께하세요",
    "footer.rights": "© 2024 SpicyJump. All rights reserved.",
  },
  en: {
    "nav.home": "Home",
    "nav.shop": "Shop",
    "nav.nft": "NFT",
    "nav.about": "About",
    "hero.title": "A New Dimension of Spice",
    "hero.subtitle": "Premium K-Food meets Blockchain. Experience the awakening taste of spice and special digital assets.",
    "hero.cta": "Get Started",
    "hero.learnMore": "Learn More",
    "featured.title": "Featured Products",
    "featured.subtitle": "Premium spicy collection curated by SpicyJump",
    "nft.title": "SpicyJump NFT",
    "nft.subtitle": "A new way to enjoy spice, join with membership benefits",
    "footer.rights": "© 2024 SpicyJump. All rights reserved.",
  }
};

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [language, setLanguage] = useState<Language>('ko');

  const t = (key: string) => {
    return translations[language][key as keyof typeof translations['ko']] || key;
  };

  return (
    <LanguageContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </LanguageContext.Provider>
  );
}

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (context === undefined) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
}
