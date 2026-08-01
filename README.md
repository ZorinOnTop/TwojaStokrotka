<p align="center">
  <img src="assets/logo.png" alt="TwojaStokrotka logo" width="150">
</p>

<h1 align="center">TwojaStokrotka</h1>

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-3ddc84?logo=android&logoColor=white" alt="Platform: Android">
  <img src="https://img.shields.io/badge/language-Kotlin-7f52ff?logo=kotlin&logoColor=white" alt="Language: Kotlin">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white" alt="UI: Jetpack Compose">
  <img src="https://img.shields.io/badge/status-w%20budowie-orange" alt="Status: w budowie">
  <img src="https://img.shields.io/badge/Google%20Services-none-red?logo=google&logoColor=white" alt="No Google Services">
  <img src="https://img.shields.io/github/license/ZorinOnTop/TwojaStokrotka" alt="License">
</p>

<p align="center">
  <img src="https://img.shields.io/github/stars/ZorinOnTop/TwojaStokrotka?style=social" alt="GitHub stars">
  <img src="https://img.shields.io/github/issues/ZorinOnTop/TwojaStokrotka" alt="Open issues">
  <img src="https://img.shields.io/github/last-commit/ZorinOnTop/TwojaStokrotka" alt="Last commit">
</p>

Nieoficjalna alternatywa dla aplikacji **Nasza Stokrotka** (rabaty, kupony, karta lojalnościowa, kupony za płatki), tworzona z myślą o telefonach ze zmodyfikowanym systemem (root, custom ROM, flash) i bez usług Google (bez Google Play Services / GApps).

> 🚧 Projekt jest w aktywnym rozwoju. Większość funkcji działa i jest podłączona do prawdziwego API. Do ukończenia zostały: mapa sklepów, powiadomienia push i kilka mniejszych elementów.

## Dlaczego ten projekt?

Oficjalna aplikacja Nasza Stokrotka, tak jak wiele aplikacji sieci handlowych, mocno opiera się na usługach Google (Firebase, Google Play Services) i często słabo lub w ogóle nie działa na telefonach z odblokowanym bootloaderem, zrootowanych, z custom ROM-em lub bez GApps.

TwojaStokrotka jest odpowiednikiem funkcjonalnym (rabaty, karta lojalnościowa, kupony, transfer punktów, naklejki, magazyn) wyglądającym niemal identycznie jak oryginał, ale działającym bez zależności od usług Google.

## Funkcje

- 📱 Logowanie numerem telefonu + kod OTP (bez dodatkowych kluczy/tokenów wpisywanych ręcznie)
- 🏠 Dashboard główny
- 🎟️ Kupony za płatki
- 🧾 Paragony i szczegóły paragonu
- 📜 Historia transakcji
- 🔁 Transfer punktów + historia transferów
- 🏷️ Oferty, szczegóły ofert oraz sortowanie/filtrowanie
- 🎁 Zarządzanie nagrodami
- 🛒 Listy zakupowe + szczegóły listy
- ⭐ Naklejki (stickers)
- 📖 Magazyn (magazine)
- 📃 Regulamin / zasady
- ℹ️ Ekran "O aplikacji"

Większość powyższych jest już podłączona do prawdziwego API Nasza Stokrotka. **Do ukończenia zostają:** mapa sklepów, powiadomienia push oraz kilka mniejszych elementów.

## Prywatność

TwojaStokrotka jest tworzona z myślą o osobach, które cenią sobie prywatność.

Podczas analizy ruchu sieciowego oryginalnej aplikacji Nasza Stokrotka (przez przechwycenie ruchu HTTPS) zaobserwowano requesty do usług stron trzecich, mimo że aplikacja ma służyć jedynie do rabatów i karty lojalnościowej:

- **Facebook Graph API** (`graph.facebook.com`) — m.in. wysyłanie zdarzeń aktywności (`/activities`), pobieranie tokenów dostępu oraz danych typu `model_asset` (fingerprinting urządzenia)
- **Firebase Remote Config** (`firebase-settings...`) — infrastruktura Google
- **SalesManago** (`api.salesmanago...`, endpoint `/mobile/contact/upsert`) — system marketing automation/CRM, aktualizujący profil kontaktowy użytkownika
- **Sentry** (`sentry.appchance...`) — error tracking / crash reporting
- **ipify** (`api.ipify.org`) — usługa zwracająca publiczny adres IP urządzenia

TwojaStokrotka robi dokładnie to, czego potrzebujesz — bez zbędnej telemetrii i trackerów kierowanych do stron trzecich.

## Instalacja

### Opcja 1 — gotowy APK

1. Przejdź do zakładki [Releases](../../releases)
2. Pobierz najnowszy plik `.apk`
3. Zainstaluj na telefonie (może być wymagane zezwolenie na instalację z nieznanych źródeł)

### Opcja 2 — build ze źródeł

Wymagania: Android Studio (najnowsza stabilna wersja), JDK 11+.

```bash
git clone https://github.com/ZorinOnTop/TwojaStokrotka.git
cd TwojaStokrotka
```

Następnie otwórz projekt w Android Studio i:
- poczekaj aż zakończy się Gradle sync
- podłącz telefon/emulator
- uruchom przez Run ▶ (moduł `app`)

Albo z linii poleceń:

```bash
./gradlew assembleDebug
```

Gotowy plik APK znajdziesz w `app/build/outputs/apk/debug/`.

## Wymagania

- Android 6.0 (API 23) lub nowszy
- Brak wymaganych usług Google Play

## Stack technologiczny

- Kotlin
- Jetpack Compose (UI)
- Retrofit + Gson (sieć)
- Coil (ładowanie obrazów)
- DataStore Preferences (lokalne dane)
- ZXing (kody QR)
- Bez zależności od Google Play Services / Firebase

## Uwaga prawna

Ten projekt **nie jest** oficjalną aplikacją sieci Stokrotka ani z nią powiązany. Nazwa i szata graficzna mają jedynie nawiązywać do funkcji aplikacji (kwiat/stokrotka), a nie ją imitować 1:1 pod kątem znaku towarowego. Jeśli coś budzi wątpliwości prawne, zgłoś to przez issues.

## Wkład w projekt

Chcesz pomóc? Śmiało otwórz issue lub pull request. Przed większymi zmianami najlepiej najpierw otworzyć issue, żeby uniknąć dublowania pracy.

## Licencja

MIT — zobacz plik [LICENSE](LICENSE).
