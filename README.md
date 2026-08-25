# LinkResolver

A general-purpose Android URL Resolver / Short-Link Expander built with
Kotlin, Jetpack Compose, and MVVM + Repository architecture.

LinkResolver resolves shortened URLs by following **normal, publicly
accessible HTTP/HTTPS redirect behavior only** — the same thing a
regular browser does when it follows a `Location` header or an HTML
meta-refresh tag. It is intentionally **not** a CAPTCHA solver, an
authentication-bypass tool, or an anti-bot circumvention tool. When a
destination requires real browser interaction or human verification,
the app reports that honestly (`PROTECTED_REDIRECT`) and offers an
"Open in Browser" fallback instead of faking a result.

## Features

- Paste, share-sheet, or clipboard-paste a shortened URL
- Automatic provider/domain detection (proper hostname parsing, not
  substring matching)
- Manual redirect-chain walking (301/302/303/307/308 + HTML
  meta-refresh), with every hop displayed
- Redirect loop and redirect-limit protection
- Copy / Open / Share the final URL
- Local, on-device history (Room), with an option to strip sensitive
  query parameters before saving
- Batch resolver (multiple URLs, bounded concurrency)
- Settings for redirect limits, timeouts, browser fallback, and privacy
- Material 3 UI with dynamic color and light/dark/system theming
- No accounts, no analytics, no unnecessary permissions — only
  `INTERNET` is requested

## Architecture

```
app/
├── data/
│   ├── local/         Room database, DAO, entity, SharedPreferences-backed settings
│   ├── network/        OkHttp client, URL validator, redirect-following engine
│   └── repository/     ResolverRepository — ties providers + history + settings together
│
├── domain/
│   ├── model/           ResolveResult, RedirectStep, ResolverStatus
│   └── resolver/        ResolverProvider interface, 9 named providers, ProviderRegistry
│
├── ui/
│   ├── home/            Home screen + ResolverViewModel + UI state
│   ├── result/          Result screen (success + failure states)
│   ├── history/         History screen
│   ├── settings/        Settings screen
│   ├── batch/            Batch Resolve screen + BatchViewModel
│   ├── nav/              Navigation graph + bottom nav
│   ├── components/       Shared Compose components (StatusBadge, etc.)
│   └── theme/            Material 3 theme (dynamic color, light/dark)
│
├── MainActivity.kt
└── LinkResolverApplication.kt
```

## Supported providers

The provider registry currently includes:

- IndianShortner (`indianshortner.in`)
- VIPShort (`vipshort.in`)
- Link.VIPShort (`link.vipshort.in` — a distinct subdomain, handled separately)
- AroLinks (`arolinks.com`)
- VPLink (`vplink.in`)
- TPI (`tpi.li`)
- CPMShort (`cpmshort.com`)
- NowShort (`nowshort.com`)
- AlphaLinks (`alpha-links.in`)
- Generic HTTP Redirect (fallback for any other domain)

Every named provider is a thin wrapper around the generic redirect
engine, because that's genuinely how these services resolve — a normal
redirect chain. A provider only diverges from that when a destination
requires something the app **won't** do (JS execution, CAPTCHA
solving, bot-check bypass); in that case it reports
`PROTECTED_REDIRECT` rather than fabricating a result.

### Adding a new provider

```kotlin
class ExampleProvider : ResolverProvider {
    override val displayName = "Example"

    override fun supports(uri: Uri): Boolean =
        uri.matchesHost("example.com")

    override suspend fun resolve(uri: Uri, context: ResolverContext): ResolveResult =
        context.genericResolver.resolve(uri.toString(), displayName)
}
```

Then add an instance to `ProviderRegistry.providers`. No other file
needs to change — UI, history, settings, and navigation are all
provider-agnostic.

## Security notes

- Only `http://` and `https://` schemes are ever accepted;
  `javascript:`, `file:`, `content:`, `intent:`, and `data:` are
  rejected outright.
- Requests to loopback / private / link-local addresses are blocked
  before any network call is made.
- TLS certificate validation is never bypassed (no custom
  `TrustManager`/`HostnameVerifier`).
- Redirects are followed manually with a hard cap
  (`MAX_REDIRECTS`, default 20) and loop detection via a normalized
  visited-URL set.
- History can optionally strip common sensitive query-parameter names
  (`token`, `auth`, `session`, etc.) before saving.
- Release builds never log full URLs.

## Requirements

- Android Studio (Koala or newer recommended)
- JDK 17
- Android SDK 35 installed, minSdk 26

## Build & run

```bash
# From the project root
./gradlew assembleDebug

# Install on a connected device/emulator
./gradlew installDebug

# Run unit tests (URL validation, redirect handling, provider detection, etc.)
./gradlew testDebugUnitTest

# Run instrumented tests (Room DAO)
./gradlew connectedDebugAndroidTest
```

Or open the project root in Android Studio and use **Run ▸ app**.

## Testing coverage

- **URL validation** — accepted/rejected schemes, blank input
- **URL normalization** — case, trailing slash, default ports
- **Redirect handling** — 301/302/303/307/308 chains via MockWebServer
- **Redirect loops** — detected and reported, never followed forever
- **Redirect limit** — chains longer than the configured max are stopped
- **Protected-page detection** — CAPTCHA/verification pages are
  reported honestly, never faked as resolved
- **Provider detection** — all nine initial domains, subdomain
  separation (`vipshort.in` vs `link.vipshort.in`), and rejection of
  lookalike domains
- **History** — insert, delete, clear (Room instrumented tests)
- **Share-intent URL extraction**

## What this app intentionally does NOT do

- Solve CAPTCHAs or "I'm not a robot" challenges
- Execute arbitrary page JavaScript
- Bypass authentication, paywalls, or access controls
- Claim a URL was resolved when it wasn't
