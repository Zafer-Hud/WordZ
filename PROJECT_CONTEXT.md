# WordZ Project Context

> Yeni bir sohbet, geliştirici veya asistan **önce bu dosyayı okumalıdır**. Bu dosya, eski sohbet geçmişinden daha güncel ve kalıcı proje bağlamıdır.

## Hızlı durum

- Repo: `Zafer-Hud/WordZ`
- Ana branch: `main`
- Uygulama: **WordZ Kelime Bulmaca**
- Android package/applicationId: `com.zafer.wordz`
- Teknoloji: **Kotlin + Jetpack Compose**
- minSdk: 26
- target/compileSdk: 35
- Java/JVM: 17
- Güncel uygulama sürümü: **0.29.0**
- versionCode: **31**
- Aktif launcher: **`Base44V8MainActivity`**
- Son doğrulanmış main head: `8a8712a9031e84f0f908197a02e0801c5b28b71b`
- Son doğrulanan GitHub Actions koşusu: **run 172 / success**
- Son doğrulamada core test + APK build akışı başarılıdır.

## Kaynak önceliği

Bir konuda çelişki varsa şu sırayı kullan:

1. `main` branch üzerindeki güncel kod
2. `PROJECT_CONTEXT.md`
3. `NEXT_STEPS.md`
4. `CHANGELOG.md`
5. Eski sohbet notları / eski Base44 kaynakları

**Base44 artık oyun içeriği için kaynak değildir.** Eski Base44 kodu yalnızca başlangıçtaki görsel/UX referansı olarak kullanılmıştır. Ülke sistemi ve Base44 bölüm verileri artık ürün yönü değildir.

---

# Ürün yönü

## Ülke sistemi kaldırıldı

WordZ'de artık Mısır, Fransa, Şili, İspanya, Türkiye, Japonya gibi ülke dünyaları kullanılmayacak.

Yeni kalıcı yapı dört tematik dünyadır:

| Dünya ID | Görünen ad | Bölümler | Renk yaklaşımı |
| --- | --- | ---: | --- |
| `hayvanlar` | Hayvanlar | 1–30 | Emerald/yeşil |
| `sehirler` | Şehirler | 31–60 | Mavi |
| `yemekler` | Yemekler | 61–90 | Turuncu |
| `spor` | Spor | 91–120 | Mor |

Toplam hedef ve mevcut yerel içerik: **120 bölüm**.

Dünya açılma kuralı: ilk dünya açık; sonraki dünya, önceki dünyanın yüklenmiş bölümlerinin en az **%50'si** tamamlandığında açılır.

## Kelime politikası

- Kelimeler **anlamlı Türkçe kelimeler** olmalı.
- Saçma, uydurma, rastgele harf dizileri kullanılmamalı.
- Ana bölüm kelimesinin temayla ilişkili olması tercih edilir.
- Alt kelimelerin her zaman temaya ait olması zorunlu değildir; anlamlı ve doğal Türkçe olmaları yeterlidir.
- Türkçe karakterler doğru kullanılmalı: `Ç Ğ I İ Ö Ş Ü`.
- Kullanıcı, kelime havuzu için şu sayfanın referans olarak kullanılabileceğini belirtti: `https://kelimedehasi.com/kelime-ciftligi-cevaplari/`.
- Bu site zorunlu değildir. Gerekirse kelime doğrulama/ilham kaynağı olarak kullanılabilir; WordZ gridleri ve seviye düzeni özgün kalmalıdır.

## 120 bölümün mevcut kaynağı

- `WordZThemedLevels.kt`: 120 yerel tematik seed ve canonical grid üretimi.
- `GameData.kt`: `base44CanonicalLevels = wordzThemedLevels` üzerinden aktif yerel içeriği sunar.
- 30 Hayvanlar + 30 Şehirler + 30 Yemekler + 30 Spor.

`Base44WordLevel.country` alan adı teknik olarak hâlâ şemada bulunur; **semantik olarak artık country değil world/theme ID taşır**. Bu, compatibility nedeniyle korunmaktadır.

---

# Ana mimari

## Launcher / uygulama kabuğu

- `Base44V8MainActivity.kt`
  - Aktif launcher.
  - Home, Dünyalar, Game, Market, Profile navigasyonunu yönetir.
  - Splash, global toast ve BottomNav akışını içerir.
  - Home'da Klasik, Hız ve Günlük modları bulunur.

## Level modeli

- `Base44LevelSchema.kt`
  - Canonical puzzle modeli.
  - Ana yapılar: `Base44WordLevel`, `Base44GridCell`, `Base44TargetWord`, `Base44CellPosition`.
  - Validator grid/cell/harf eşleşmelerini kontrol eder.
- `WordZThemedLevels.kt`
  - WordZ'ün 120 yerel tematik bölümü.
- `GameData.kt`
  - Yerel canonical level listesini dışarı verir.
- `Base44RuntimeLevels.kt`
  - Runtime level kaynağı.
  - `word_levels.json` import yolu compatibility olarak durabilir; WordZ artık çalışmak için Base44 export'una ihtiyaç duymaz.

## Oyun motoru

- `Base44CanonicalGame.kt`
  - Aktif crossword + letter wheel oyun motoru.
  - Grid doğrudan canonical `gridCells` ve `wordsToFind.cells` verisini kullanır.
  - Hint, shuffle, settings, themes, sonuç ekranı, doğru/yanlış akışı burada yoğunlaşır.
- `Base44ModeGame.kt`
  - Classic dışı mode wrapper.
  - Hız Modu: 90 saniye.
  - Günlük Görev overlay/akışı.
- `Base44Modes.kt`
  - Hız rekoru ve günlük streak/state deposu.

## State / progression

- `Base44AppState.kt`
  - coin, hints, XP, completed levels, selected level ve progression state.
- `PlayerProgress.kt`
  - XP sistemi.
  - Sabit `500 XP = 1 oyuncu seviyesi` mantığı.
- `Base44Progress.kt`
  - Bölüm ödülleri ve skor.
- `Base44SaveMigration.kt`
  - save schema migration.
- `Base44UserProfile.kt`
  - local profil snapshot ve kalıcı profil alanları.

### Ödül ekonomisi

İlk tamamlamada:

- `< 30 saniye`: **250 XP, 70 altın, 500 skor**
- `30–59.999 saniye`: **150 XP, 70 altın, 300 skor**
- `>= 60 saniye`: **100 XP, 50 altın, 200 skor**
- Replay: **0 XP, 0 altın, 0 skor**

Hint:

- elde hint varsa `-1 hint`
- hint yoksa ve coin >= 100 ise `-100 coin`
- aksi halde hint kullanılamaz

**Zero coin / zero hint değerleri korunmalıdır; eski JS `||` davranışı taklit edilmemelidir.**

## Dünyalar

- `Base44CountryCatalog.kt`
  - Dosya adı legacy'dir.
  - İçindeki kayıtlar artık ülke değil **WordZ dünyalarıdır**.
  - `B44Country` data class da compatibility shell olarak kullanılmaktadır.
  - Yeni kod yazarken mümkünse kullanıcıya "ülke" kelimesi gösterme.

## Görsel sistem

- `Base44DesignSystem.kt`
  - renkler, glass, radius, spacing ve animasyon tokenları.
- Genel stil:
  - koyu navy arka plan
  - emerald ana vurgu
  - gold ikincil vurgu
  - glass kartlar
  - glow/spring/pulse animasyonları
- Kullanıcı **resim üretilmesini/gönderilmesini istemiyor**. Gerekmedikçe image generation kullanma.

## Profil / koleksiyon

- `WordZProfileScreen.kt`
  - stats, achievements, themed collection.
  - Koleksiyon artık 4 dünyaya göre 24 artifact içerir; eski ülke artifactleri kullanılmaz.

## Market

- `Base44V5Meta.kt`
  - Aktif Market bileşenlerinin bir kısmı burada kalmaktadır.
  - Dosya adı legacy'dir; davranış korunurken ileride WordZ isimlendirmesine taşınabilir.

## Toast / audio / haptic

- `GameToast.kt`: global success/error/warning/info toast; maksimum 5 görünür toast.
- `WordZAudio.kt`: ses efektleri ve müzik.
- `WordZHaptics.kt`: dokunsal geri bildirim.
- Manifestte `VIBRATE` izni bulunur.

---

# Oyun modları

## Klasik

Normal 120 bölümlük progression.

## Hız Modu

- 90 saniyelik sayaç.
- Süre dolunca puzzle input kilitlenir.
- En iyi süre kalıcı saklanır.

## Günlük Görev

- Cihaz tarihine göre deterministik olarak erişilebilir bir bölüm seçilir.
- Günlük tamamlama tarihi tutulur.
- Günlük streak tutulur.

---

# Test ve CI kuralları

Workflow: `.github/workflows/build-apk.yml`

Her gerçek uygulama değişikliğinden sonra final head için şu adımlar doğrulanmalıdır:

1. Base44/WordZ core unit tests
2. Debug APK build
3. APK rename
4. Artifact upload

**Build + Rename + Upload başarılı olmadan APK başarılı denmemelidir.**

APK adı: `WordZ-debug.apk` / kullanıcıya sürümlü kopya olarak `WordZ-vX.Y.Z.apk` verilebilir.

Signing:

- GitHub Actions cache ile stabil debug/test signing key kullanılır.
- Keystore/base64 secret repoya gömülmemelidir.
- Stabil signing korunmalıdır; yeni APK önceki sürüm üzerine kurulabilmelidir.

Core tests: `app/src/test/java/com/zafer/wordz/Base44CoreTest.kt`.

Yeni içerik/refactor yapılırken en az şu invariantlar test edilmelidir:

- toplam 120 level
- world başına 30 level
- level numbers 1..120
- duplicate level number yok
- canonical validator tüm level'larda temiz
- reward thresholdları doğru
- replay ödülü sıfır
- wheel maximum character multiplicity doğru
- daily seçim range içinde

---

# Kullanıcı tercihleri ve çalışma kuralları

- Kullanıcıyla iletişim dili: **Türkçe**.
- Gereksiz soru sorma; yeterli bağlam varsa doğrudan uygula.
- Kullanıcı genellikle "devam et" dediğinde mevcut roadmap'in sıradaki güvenli işini uygula.
- Yalnızca **`Zafer-Hud/WordZ`** üzerinde çalış.
- **`Zafer-Hud/KelimeYildiz` projesine asla dokunma.**
- **TinyFish kullanma.** Kullanıcı ücretli olduğu için istemiyor.
- Android kodu **Kotlin + Jetpack Compose** olmalı; JSX uygulama kodu yazma.
- Kullanıcı istemedikçe resim üretme/gönderme.
- Anlamlı Türkçe kelimeler kullan; içerik sayısını doldurmak için anlamsız kelime ekleme.
- Mevcut save/coin/XP/skor/completed state'i sıfırlama.
- Teknik refactor sırasında persistence key'lerini sebepsiz değiştirme.
- Uygulama değişikliğinden sonra final CI doğrulaması yap ve başarılı artifact varsa APK paylaş.

---

# Güncel bilinen teknik borç

1. Bazı sınıf/dosya isimlerinde hâlâ `Base44` ve `Country` geçmişi var. Üründe ülke yok; bunlar compatibility isimleridir.
2. `Base44CountryCatalog.kt` ve `B44Country` ileride güvenli bir refactor ile `WorldCatalog` / `WordZWorld` yapılabilir.
3. `Base44V5Meta.kt` Market tarafında legacy isimlendirme taşımaktadır.
4. 120 seed canonical validator'dan geçse bile **kelime kalitesi ayrıca insan gözüyle QA edilmelidir**. Özellikle az kullanılan, kısaltma gibi duran veya doğal Türkçe hissi vermeyen alt kelimeler temizlenmelidir.
5. Online auth / gerçek multiplayer leaderboard backend'i yoktur; şu an local profil mantığı önceliklidir. Kullanıcı istemeden sahte online backend ekleme.
6. Baloo 2 / Space Grotesk gibi eski Base44 font referansları zorunlu değildir; font assetleri kullanıcı tarafından sağlanmadan binary font paylaşma.
7. `word_levels.template.json` compatibility örneği olarak kalabilir; 120 yerel bölüm artık uygulamada native olarak vardır.

---

# Yeni sohbet için önerilen başlangıç

Kullanıcı yeni sohbette şu kısa komutu verebilir:

> `Zafer-Hud/WordZ projesine devam et. Önce PROJECT_CONTEXT.md ve NEXT_STEPS.md dosyalarını, sonra main branch kodunu oku. Yalnızca WordZ reposunda çalış. Son durumdan devam et; uygulama değişikliğinde test + APK build + artifact doğrulaması yap.`

Yeni sohbet **eski konuşma geçmişini istemeden** bu dosya üzerinden devam etmelidir.
