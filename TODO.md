# Khata implementation status

- [x] Native Android Kotlin + Jetpack Compose app shell, Ember & Ink theme, onboarding, and four-tab navigation.
- [x] Offline Room transaction/category/rule storage and DataStore preferences.
- [x] Deterministic merchant and keyword categorization, review queue, correction learning, and retroactive merchant categorization.
- [x] Home spending comparison/budget ring, categories, recent transactions, month-to-date comparison, quote banner, and search.
- [x] Calendar day heatmap, yearly activity heatmap, category analysis, top merchants, and transaction detail actions.
- [x] Settings for profile, five languages/script toggle, theme, surface, motion, haptics, budget, CSV sharing/export, and clear-data confirmation.
- [x] Complete supplied five-language `quotes.json` and trigger-specific quote lookup.
- [x] GitHub Actions debug APK build, unit tests, and 14-day artifact upload.
- [ ] Connect Account Aggregator provider SDK and consent/sync workflow; keep this optional.
- [ ] Implement CSV import and full custom-category/merchant-rule management screens.
- [ ] Add Caveat and Google Sans font assets and apply exact type scale; currently uses Android sans/cursive fallbacks.
- [ ] Add release signing and verify a signed release package after user-supplied signing configuration.
