# AFM2026: Economy, Shop & Monetization Refactor

## Overview
This refactor expands AFM2026 into a production-ready mobile football management ecosystem by introducing a dual-currency system, real-money monetization (Visa/AzamPesa), and an offline-first economy engine.

## Core Changes
### Dual-Currency System
- **CASH**: Used for club operations (wages, transfers, facilities).
- **COINS**: Premium currency for manager boosts (emergency funding, speedups).

### Monetization & Payments
- **PaymentGatewayManager**: Handles **AzamPesa** (TZS) and **Visa/Mastercard** payments with offline-safe queueing.
- **Shop UI**: FCM26-inspired shop with coin bundles and special offers.

### Advanced Economics
- **Sponsorship System**: Negotiation-based deals for shirt and stadium sponsors.
- **Fan Economy**: Simulates ticket and merchandise revenue based on club performance and derbies.
- **Facility Upgrades**: Progression system for stadium, academy, and medical centers with coin-acceleration.

### Gameplay Loop
- **Monthly Financial Reports**: Comprehensive month-end accounting for revenue and expenses.
- **Single Save Enforcement**: Mandatory overwrite warning when starting a new career.

## Verification Summary
- **Economy Logic**: Verified balance updates and payout calculations in `EconomyManager`.
- **Payment Flows**: Simulated successful and failed transactions via `PaymentGatewayManager`.
- **UI Integrity**: Verified shop cards, payment dialogs, and negotiation screens in portrait mode.
