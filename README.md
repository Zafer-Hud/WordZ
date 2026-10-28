# WordZ Kelime Bulmaca

Kotlin + Jetpack Compose ile geliştirilen Android Türkçe kelime bulmaca oyunu.

## Güncel yapı

- 4 tematik dünya
  - 🐾 Hayvanlar — Bölüm 1–30
  - 🏙️ Şehirler — Bölüm 31–60
  - 🍽️ Yemekler — Bölüm 61–90
  - 🏆 Spor — Bölüm 91–120
- Toplam 120 yerel Türkçe bölüm
- Harf çarkı + crossword grid
- Klasik Mod
- 90 saniyelik Hız Modu
- Günlük Görev + streak
- XP / altın / skor / hint ekonomisi
- Market
- Profil / başarılar / 24 parçalık koleksiyon
- 4 oyun teması
- Ses + haptic feedback
- Kalıcı save migration

## Yeni sohbetten projeye devam etmek

Yeni bir ChatGPT sohbetinde eski konuşmayı taşımaya gerek yoktur.

Önce şu dosyalar okunmalıdır:

1. [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md) — güncel ürün kararları, mimari, kurallar ve çalışma biçimi
2. [`NEXT_STEPS.md`](NEXT_STEPS.md) — sıradaki geliştirme işleri
3. [`CHANGELOG.md`](CHANGELOG.md) — sürüm ve mimari geçmişi

Yeni sohbet için önerilen komut:

> `Zafer-Hud/WordZ projesine devam et. Önce PROJECT_CONTEXT.md ve NEXT_STEPS.md dosyalarını, sonra main branch kodunu oku. Yalnızca WordZ reposunda çalış. Son durumdan devam et; uygulama değişikliğinde test + APK build + artifact doğrulaması yap.`

## Teknik

- Kotlin
- Jetpack Compose
- applicationId: `com.zafer.wordz`
- minSdk 26
- target/compileSdk 35
- Java 17

## APK

`main` dalına yapılan güncellemelerde GitHub Actions core testleri çalıştırır ve debug APK üretir. Başarılı build sonrasında artifact `WordZ-debug-apk` adıyla workflow çalışmasında bulunur.

Bir sürüm başarılı kabul edilmeden önce **tests + Build debug APK + Rename APK + Upload APK** adımları doğrulanmalıdır.
