# ARENA X — Esports Tournament Platform

**ARENA X** is a production-oriented, mobile-first esports tournament platform designed primarily for Free Fire MAX, with architectural support for additional battle royale and competitive titles (BGMI, Call of Duty Mobile).

---

## 🎮 Platform Features & Implementation Status

| Feature Domain | Status | Details |
|---|---|---|
| **Design System** | ✅ Production Ready | Dark Obsidian & Cyber Cyan/Electric Violet theme, M3 compliance, custom adaptive launcher icon, cinematic hero art, English & Bengali localization. |
| **Tournament Engine** | ✅ Functional | Solo, Duo, and Squad tournaments; atomic slot allocation; automated status lifecycle (Registration, Check-In, Room Release, Live, Completed). |
| **Room Credentials Security** | ✅ Hardened | Room ID & Password withheld until 15 minutes prior to match start; visible only to verified registered players. |
| **Wallet & Ledger** | ✅ Immutable & Integer-Safe | Exact integer paise representation (never floating point); auditable double-entry ledger; deposit/withdrawal audit trail. |
| **Watch & Earn (Rewarded Ads)**| ✅ Implemented | Daily limit (5 ads/day), 60s anti-fraud cooldown, randomized server-calculated credit (₹0.50 - ₹2.00 equivalent tournament points), strict Google AdMob policy compliance. |
| **Three Live Leaderboards** | ✅ Dynamic & Reactive | Most Active Players (Top 100), Top Referrers (Top 10), Top Depositors (Top 10) with Daily, Weekly, Monthly, and All-Time filters. |
| **Admin Prize Control** | ✅ Configurable | Admin-controlled leaderboard prize tiers for Rank 1, 2, 3, and Top 10 with budget enforcement and batch payout workflows. |
| **Admin Operations Dashboard**| ✅ Complete | Tournament publishing, Room ID credential release, match result finalization, and withdrawal approval/rejection with automatic ledger refunding. |
| **Data Persistence** | ✅ Active (Room DB) | Robust device-local SQLite Room Database with reactive Kotlin Flows and comprehensive seed dataset. |
| **Cloud Firestore & Backend** | 📋 Ready for Provisioning | Firestore schema (`/firebase-blueprint.json`), Zero-Trust rules (`/firestore.rules`), and index definitions (`/firestore.indexes.json`) generated and ready for deployment. |

---

## 💳 Payment Gateway Integration Guide (UPI / NetBanking)

### 1. Razorpay / Cashfree Merchant Onboarding
- **Never confirm deposits client-side.**
- Client initiates an order by requesting a server order ID.
- The webhook listener must verify the webhook signature before crediting balances:
```javascript
// Webhook signature verification pattern
const crypto = require('crypto');
const expectedSignature = crypto
  .createHmac('sha256', process.env.RAZORPAY_WEBHOOK_SECRET)
  .update(JSON.stringify(req.body))
  .digest('hex');

if (expectedSignature === req.headers['x-razorpay-signature']) {
  // Idempotently process deposit in Firestore transaction
}
```

### 2. Payouts (Withdrawals)
- Withdrawals use IMPS UPI transfer to the user's verified VPA.
- Minimum payout: ₹50.00 (5,000 paise). Maximum payout: ₹5,000.00 (500,000 paise).
- Pending withdrawals are held in `pendingWithdrawalPaise` until admin approval. If rejected, the amount is automatically refunded back to `balancePaise`.

---

## 📺 Rewarded Ads Compliance (Google AdMob Policy)
- **Policy Gate:** In compliance with Google AdMob policy, ads cannot offer direct cash or transferable fiat money. ARENA X awards **non-transferable tournament entry credits and bonus points**.
- **Anti-Fraud:** Daily limit capped at 5 completed ads per 24 hours, with a mandatory 60-second cooldown between impressions.

---

## 🏗️ Technology Stack
- **Language:** Kotlin 2.2.10
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Persistence:** Room Database 2.7.0 (KSP)
- **Async & Concurrency:** Kotlin Coroutines & Reactive StateFlow
- **Image Loading:** Coil Compose
- **Architecture:** MVVM + Clean Repository Pattern
