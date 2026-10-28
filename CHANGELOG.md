# WordZ Changelog

Bu dosya yalnızca önemli ürün/mimari kilometre taşlarını tutar. Küçük görsel düzeltmelerin tamamını listelemek zorunlu değildir.

## Unreleased

- Kalıcı proje devam sistemi eklendi:
  - `PROJECT_CONTEXT.md`
  - `NEXT_STEPS.md`
  - `CHANGELOG.md`
- Yeni sohbetlerde geçmiş konuşmaya ihtiyaç duymadan GitHub reposundan devam edilebilmesi hedeflendi.

## 0.29.0

### Ürün yönü değişikliği

- Ülke sistemi kaldırıldı.
- WordZ dört tematik dünyaya geçirildi:
  - Hayvanlar 1–30
  - Şehirler 31–60
  - Yemekler 61–90
  - Spor 91–120
- Toplam **120 yerel Türkçe bölüm** native olarak eklendi.
- Base44 DB/export bağımlılığı ana içerik gereksinimi olmaktan çıkarıldı.
- `GameData.kt` artık `wordzThemedLevels` kaynağını kullanıyor.
- `Base44WordLevel.country` alanı compatibility nedeniyle world ID taşımaya devam ediyor.

### UI / navigasyon

- Aktif launcher `Base44V8MainActivity` oldu.
- Home tematik dünya bilgisini göstermeye başladı.
- Harita/Map deneyimi "Dünyalar" yaklaşımına dönüştürüldü.
- Klasik kart 4 tema / 120 bölüm yapısını gösteriyor.
- Country-era V7 launcher kaldırıldı.

### Profil / koleksiyon

- `WordZProfileScreen.kt` eklendi/aktif edildi.
- Koleksiyon 4 dünyaya göre yeniden düzenlendi.
- 24 themed artifact: her dünya için 6 artifact.

### Teknik

- versionCode 31
- versionName 0.29.0
- launcher `Base44V8MainActivity`
- Son doğrulanmış uygulama head'i: `8a8712a9031e84f0f908197a02e0801c5b28b71b`
- GitHub Actions run 172 başarılı.

## 0.28.0

- Canonical `Base44WordLevel` modeli ana puzzle modeli haline getirildi.
- Eski `Level/Placement` runtime bağımlılığı kaldırıldı.
- Save schema migration altyapısı eklendi.
- UI design token sistemi merkezileştirildi.
- Haptic feedback eklendi.
- Hız Modu aktif edildi:
  - 90 saniye
  - timeout
  - input lock
  - best time
- Günlük Görev aktif edildi:
  - deterministik günlük bölüm
  - daily completion
  - streak
- World/country unlock kuralı state seviyesinde %50 olarak zorlandı.
- V7 launcher aktif edilmişti; daha sonra v0.29.0 ile V8'e geçildi.

## 0.27.0

- V6 launcher ve tek `WordZAppState` yaklaşımı eklendi.
- Oyun motoru canonical `gridCells / wordsToFind.cells` verisini doğrudan kullanmaya başladı.
- Splash/Home/Map/Game motion ve glass parity geliştirildi.
- Crossword gerçek 3D reveal flip kullanmaya başladı.
- Sürüm metni `BuildConfig.VERSION_NAME` kaynağına bağlandı.
- Eski çok sayıda Activity/UI dosyası fiziksel olarak temizlendi.
- Regression core test altyapısı kuruldu.

## 0.26.0

- Canonical level schema ve JSON importer altyapısı eklendi.
- Güçlü level validator eklendi.
- Global GameToast altyapısı eklendi.
- Tek local profile modeli eklendi.
- Fake leaderboard kaldırılmaya başlandı; local profile/skor kullanıldı.
- Market/Profile/Collection ortak profile state'e yaklaştırıldı.

## 0.25.0

- Completion callback'teki çift ödül / çift skor riski kaldırıldı.
- Manifest yalnız aktif launcher'ı dışarı açacak şekilde temizlendi.

## 0.24.0

- Eski ülke döneminde map/game country katalogları tekleştirildi.
- Bu yapı v0.29.0'da tamamen tematik dünyalarla değiştirildi.

## 0.23.0

- Ödül hesaplama doğrudan canonical progression kaynağına bağlandı.
- Hız bonusu coin sonucu ile bakiye aynı hale getirildi.
- Profil gerçek kalıcı skoru göstermeye başladı.

## 0.22.0

- XP sistemi `500 XP / oyuncu seviyesi` olarak sabitlendi.
- Base44 reward/score kuralları canonical helper'a taşındı.
- Skor persistence/migration eklendi.

## 0.21.0

- 4 oyun teması ve ThemeStore eklendi:
  - Kozmik Mor
  - Nostalji
  - Pastel Dünya
  - Gece Modu
- Sound/music persistence ve `WordZAudio` entegrasyonu.
- Completion ekranı geliştirildi.

## 0.20.0

- Market, Profile, Achievements ve Collection temel ekranları eklendi.

## 0.19.0

- Native Compose WorldMap ve native Compose Game ekranı oluşturuldu.
- Crossword, LetterWheel, Hint, Settings ve completion akışı uygulandı.

## 0.18.0

- İlk Base44 görsel referanslı Compose shell oluşturuldu:
  - Splash
  - Home
  - BottomNav
  - temel dark/glass tasarım

---

# Changelog güncelleme kuralı

Yeni sürüm çıkarıldığında:

1. versionCode/versionName değişikliğini kaydet.
2. Kullanıcıya görünen önemli değişiklikleri yaz.
3. Veri modeli/persistence değişikliklerini mutlaka yaz.
4. Launcher değiştiyse belirt.
5. Son doğrulanmış CI head/run bilgisini `PROJECT_CONTEXT.md` ile birlikte güncelle.
