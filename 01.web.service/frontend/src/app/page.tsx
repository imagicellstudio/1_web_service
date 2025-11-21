import Image from "next/image";
import { Search, ShoppingCart, Bell, User, Menu, ArrowRight, Star, TrendingUp, Activity, Heart } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function Home() {
  // Force rebuild
  return (
    <div className="min-h-screen bg-background text-foreground font-sans">
      {/* Navigation */}
      <nav className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container flex h-16 items-center justify-between px-4 md:px-6">
          <div className="flex items-center gap-6">
            <a className="flex items-center gap-2 font-bold text-xl" href="#">
              <span className="text-primary">SpicyJump</span>
            </a>
            <div className="hidden md:flex items-center gap-6 text-sm font-medium text-muted-foreground">
              <a className="hover:text-foreground transition-colors" href="#">Categories</a>
              <a className="hover:text-foreground transition-colors" href="#">NFT Market</a>
              <a className="hover:text-foreground transition-colors" href="#">Community</a>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <div className="relative hidden sm:block w-full max-w-sm">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <input
                type="search"
                placeholder="Search K-Food..."
                className="h-9 w-64 rounded-md border border-input bg-transparent px-9 py-1 text-sm shadow-sm transition-colors file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
              />
            </div>
            <Button variant="ghost" size="icon" className="relative">
              <Bell className="h-5 w-5" />
              <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-red-500" />
            </Button>
            <Button variant="ghost" size="icon">
              <ShoppingCart className="h-5 w-5" />
            </Button>
            <Button variant="ghost" size="icon">
              <User className="h-5 w-5" />
            </Button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative overflow-hidden py-20 md:py-32 bg-gradient-to-b from-background to-muted/30">
        <div className="container px-4 md:px-6 relative z-10">
          <div className="grid gap-12 lg:grid-cols-2 lg:gap-8 items-center">
            <div className="flex flex-col justify-center space-y-8">
              <div className="space-y-4">
                <div className="inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-colors focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 border-transparent bg-primary text-primary-foreground hover:bg-primary/80">
                  New Arrival
                </div>
                <h1 className="text-4xl font-extrabold tracking-tight lg:text-6xl xl:text-7xl">
                  Discover Premium <span className="text-primary">K-Food</span> & NFTs
                </h1>
                <p className="max-w-[600px] text-muted-foreground md:text-xl">
                  Experience the authentic taste of Korea with blockchain-verified sourcing. Trade recipes, ingredients, and exclusive memberships.
                </p>
              </div>
              <div className="flex flex-col gap-2 min-[400px]:flex-row">
                <Button size="lg" className="h-12 px-8">
                  Explore Market <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
                <Button size="lg" variant="outline" className="h-12 px-8">
                  View NFT Collections
                </Button>
              </div>
              
              {/* Stats / Indices Preview */}
              <div className="grid grid-cols-3 gap-4 pt-8 border-t">
                <div className="space-y-1">
                  <h4 className="text-2xl font-bold flex items-center gap-2">
                    98% <TrendingUp className="h-4 w-4 text-green-500" />
                  </h4>
                  <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Market Potential</p>
                </div>
                <div className="space-y-1">
                  <h4 className="text-2xl font-bold flex items-center gap-2">
                    4.9 <Star className="h-4 w-4 text-yellow-500 fill-yellow-500" />
                  </h4>
                  <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Avg. Rating</p>
                </div>
                <div className="space-y-1">
                  <h4 className="text-2xl font-bold flex items-center gap-2">
                    12k+ <Activity className="h-4 w-4 text-blue-500" />
                  </h4>
                  <p className="text-xs text-muted-foreground font-medium uppercase tracking-wider">Active Traders</p>
                </div>
              </div>
            </div>
            <div className="relative mx-auto w-full max-w-[500px] lg:max-w-none">
               <div className="aspect-square overflow-hidden rounded-3xl bg-muted relative shadow-2xl rotate-3 hover:rotate-0 transition-transform duration-500">
                  {/* Placeholder for Hero Image - In a real app, use next/image */}
                  <div className="absolute inset-0 bg-gradient-to-br from-primary/20 to-purple-500/20 flex items-center justify-center">
                    <span className="text-muted-foreground/50 font-bold text-2xl">Premium K-Food Image</span>
                  </div>
               </div>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Products */}
      <section className="py-16 md:py-24">
        <div className="container px-4 md:px-6">
          <div className="flex items-center justify-between mb-10">
            <h2 className="text-3xl font-bold tracking-tight">Trending Products</h2>
            <a href="#" className="text-sm font-medium text-primary hover:underline">View all</a>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="group relative overflow-hidden rounded-xl border bg-card text-card-foreground shadow transition-all hover:shadow-lg">
                <div className="aspect-[4/3] bg-muted relative overflow-hidden">
                  <div className="absolute inset-0 bg-gradient-to-br from-muted/50 to-muted flex items-center justify-center group-hover:scale-105 transition-transform duration-300">
                     <span className="text-muted-foreground/30 font-medium">Product Image {i}</span>
                  </div>
                  <div className="absolute top-2 right-2 bg-background/80 backdrop-blur px-2 py-1 rounded-full text-xs font-semibold flex items-center gap-1">
                    <Star className="h-3 w-3 fill-yellow-500 text-yellow-500" /> 4.8
                  </div>
                </div>
                <div className="p-4 space-y-2">
                  <h3 className="font-semibold leading-none tracking-tight">Premium Kimchi Set</h3>
                  <p className="text-sm text-muted-foreground">Authentic fermentation, 3kg</p>
                  <div className="flex items-center justify-between pt-2">
                    <span className="font-bold text-lg">$24.00</span>
                    <Button size="sm" variant="secondary" className="h-8 w-8 p-0 rounded-full">
                      <ShoppingCart className="h-4 w-4" />
                    </Button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* NFT Showcase */}
      <section className="py-16 md:py-24 bg-slate-950 text-white overflow-hidden relative">
         <div className="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
         <div className="container px-4 md:px-6 relative z-10">
            <div className="text-center max-w-2xl mx-auto mb-16 space-y-4">
               <h2 className="text-3xl md:text-5xl font-bold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white to-white/60">
                  Digital Ownership
               </h2>
               <p className="text-slate-400 text-lg">
                  Verify authenticity and trade exclusive recipes with our blockchain-backed NFT system.
               </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
               {[1, 2, 3].map((i) => (
                  <div key={i} className="relative group">
                     <div className="absolute -inset-0.5 bg-gradient-to-r from-pink-600 to-purple-600 rounded-2xl blur opacity-75 group-hover:opacity-100 transition duration-1000 group-hover:duration-200"></div>
                     <div className="relative bg-slate-900 rounded-xl p-6 space-y-4 h-full flex flex-col">
                        <div className="aspect-square rounded-lg bg-slate-800 flex items-center justify-center mb-4">
                           <span className="text-slate-600 font-mono">NFT #{i}024</span>
                        </div>
                        <div className="space-y-2 flex-1">
                           <h3 className="text-xl font-bold text-white">Origin Certificate #{i}</h3>
                           <p className="text-slate-400 text-sm">Verified source from Jeju Island organic farm.</p>
                        </div>
                        <div className="flex items-center justify-between pt-4 border-t border-slate-800">
                           <div className="flex flex-col">
                              <span className="text-xs text-slate-500">Current Bid</span>
                              <span className="font-mono font-bold text-purple-400">0.5 ETH</span>
                           </div>
                           <Button variant="outline" className="border-slate-700 hover:bg-slate-800 text-white hover:text-white">
                              Place Bid
                           </Button>
                        </div>
                     </div>
                  </div>
               ))}
            </div>
         </div>
      </section>
    </div>
  );
}
