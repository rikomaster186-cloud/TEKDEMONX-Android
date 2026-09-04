# TEKDEMONX Android

Android tətbiqi `https://x.tekdemonx.workers.dev` hesab sistemindən istifadə edir.

## İlk açılış
1. TR / EN seçimi
2. LOGIN və ya REGISTER
3. Login olduqdan sonra əsas TEKDEMONX saytı tətbiqin içində açılır
4. Saytın daxili LOGIN / REGISTER düymələri tətbiq rejimində gizlədilir
5. Session cookie saxlanır, ona görə istifadəçi girişdədirsə növbəti açılışda birbaşa əsas hissə açılır

## GitHub Actions ilə APK
Repository-yə push etdikdən sonra:
Actions → Build TEKDEMONX APK → Run workflow

Hazır APK:
Actions run → Artifacts → TEKDEMONX-APK

Google Play üçün imzalanmış AAB ayrıca signing key ilə hazırlanmalıdır.
