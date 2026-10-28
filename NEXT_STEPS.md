# WordZ Next Steps

> Bu dosya yeni sohbetlerde uygulanacak **güncel geliştirme sırasıdır**. Bir madde tamamlandığında bu dosya güncellenmelidir.

## P0 — 120 bölüm kelime kalite kontrolü

Mevcut uygulamada 120 tematik bölüm vardır. Canonical validator teknik olarak gridleri doğrular; ancak dil kalitesi ayrı kontroldür.

Yapılacaklar:

- `WordZThemedLevels.kt` içindeki 120 seed'i tek tek gözden geçir.
- Ana kelimeler tema ile uyumlu kalsın:
  - 1–30 Hayvanlar
  - 31–60 Şehirler
  - 61–90 Yemekler
  - 91–120 Spor
- Alt kelimeler tematik olmak zorunda değil; fakat **anlamlı ve doğal Türkçe** olmalı.
- Kısaltma, anlamsız iki harfli kombinasyon, çok yapay çekim veya oyuncuya hatalı görünecek kelimeleri değiştir.
- Türkçe karakterleri koru.
- Gerekirse `https://kelimedehasi.com/kelime-ciftligi-cevaplari/` kelime havuzu için referans/ilham olarak kullanılabilir.
- Kaynaktaki seviye düzenlerini birebir kopyalama; WordZ gridleri özgün kalmalı.
- Her değişiklik sonrası canonical validator ve unit testleri çalıştır.

### Bu iş için eklenmesi gereken testler

- tam 120 level
- her world tam 30 level
- levelNumber tam 1..120
- her level `isActive == true`
- her target length >= 2
- her target yalnız Türk alfabesi harflerinden oluşuyor
- her title doğru world prefix'i taşıyor
- tüm `validateBase44WordLevel()` sonuçları boş

---

## P1 — Country/Base44 legacy isimlerini temizleme

Üründe artık ülke yok. Aşağıdaki isimler yalnız compatibility kalıntısı:

- `Base44CountryCatalog.kt`
- `B44Country`
- `base44CanonicalCountries`
- `base44CountryForLevel`
- `base44CountryUnlocked`
- `Base44*` isimlerinin önemli bir kısmı

Plan:

1. Önce kullanıcıya görünen hiçbir davranışı değiştirmeden `WordZWorld` modeli oluştur.
2. `WorldCatalog.kt` oluştur.
3. Harita/Home/Game kodunu yeni isimlere geçir.
4. Test + build yeşil olduktan sonra eski wrapper isimlerini kaldır.
5. Persistence key'lerini gereksiz yere değiştirme.

Bu refactor tek seferde çok büyük yapılmamalı; her aşamada CI yeşil kalmalı.

---

## P1 — Dünyalar ekranı polish

- Başlık tamamen "Dünyalar" dilini kullanmalı; kullanıcıya "ülke/country" göstermemeli.
- 4 dünya kartı:
  - 🐾 Hayvanlar
  - 🏙️ Şehirler
  - 🍽️ Yemekler
  - 🏆 Spor
- Dünya başına `tamamlanan / 30` ve yüzde gösterimi.
- Aktif dünyanın glow vurgusunu güçlendir.
- Kilitli dünya metni: önceki dünyanın %50 şartını açık anlatmalı.
- Dünya bottom sheet / detay kartında 30 bölüm ve progression net olmalı.

---

## P1 — Oyun içi görsel polish

- Crossword reveal flip cihazlar arasında tutarlı mı kontrol et.
- Doğru kelimede glow ve küçük success pulse iyileştir.
- Yanlış kelime shake gücünü küçük ekranlarda test et.
- Hint reveal animasyonunu sıralı hale getirme değerlendir.
- LetterWheel seçili harf bağlantısı ve shuffle animasyonunu polish et.
- Haptic ve ses ayarlarının birlikte beklenen şekilde davranıp davranmadığını kontrol et.
- Completion ekranında confetti/reward timing'i QA et.

---

## P1 — Profile / Collection polish

`WordZProfileScreen.kt` artık 4 dünyalık koleksiyonu kullanıyor.

Yapılacaklar:

- Her dünya için 6 artifact = toplam 24 artifact korunmalı.
- Artifact unlock hesabını 120-level progression ile tekrar doğrula.
- Koleksiyon filtrelerinin 4 world ID ile çalıştığını test et.
- Legendary/epic rarity glow ve detail sheet polish.
- İstatistiklere isteğe bağlı olarak şunlar eklenebilir:
  - en hızlı bölüm
  - günlük streak
  - dünya bazlı tamamlanma
  - toplam bulunan kelime

---

## P1 — Market temizliği

Market aktif kodunun bir kısmı hâlâ `Base44V5Meta.kt` içindedir.

Plan:

- Market bileşenlerini `WordZMarketScreen.kt` dosyasına taşı.
- Mevcut ekonomi davranışını değiştirme.
- Fallback ürünler:
  - 5 İpucu
  - 10 İpucu
  - 500 Altın (mevcut davranış/coming soon kontrol edilmeli)
  - Süper İpucu
- `b44_super_hint` satın alımının oyunda gerçekten tüketilip tüketilmediğini kontrol et; kullanılmıyorsa ya gerçek kullanım ekle ya da Market'te yanıltıcı ürünü kaldır.
- Tema ürünleri ile Game ThemeStore arasındaki veri modelini tekleştirme değerlendir.

---

## P2 — Oyun modlarını derinleştirme

### Hız Modu

Mevcut:
- 90 saniye
- timeout overlay
- input kilidi
- en iyi süre

Eklenebilir:
- combo sistemi
- art arda doğru kelime bonusu
- ayrı speed score
- sonuç ekranında best/new record badge

### Günlük Görev

Mevcut:
- tarihe göre deterministik bölüm
- günlük completion
- streak

Eklenebilir:
- 3 / 7 / 30 günlük streak ödülleri
- günlük reward chest
- günlük geçmiş göstergesi

Yeni ekonomi eklerken klasik Base44 reward sistemini bozma.

---

## P2 — Save/persistence QA

- v0.28/v0.29'dan güncellemede coin korunuyor mu?
- hints = 0 korunuyor mu?
- coin = 0 korunuyor mu?
- XP / level korunuyor mu?
- completed set korunuyor mu?
- score migration bir kere mi çalışıyor?
- active theme / owned themes korunuyor mu?
- daily streak ve speed best korunuyor mu?
- 120-level dünyalara geçiş eski 0-based completed indexleriyle crash üretmiyor mu?

Save reset yalnız kullanıcı açıkça isterse yapılmalı.

---

## P2 — Test kapsamı

Mevcut core testlere ekle:

- world unlock %50 sınır testi
- 4 world x 30 level testi
- all 120 level canonical validation testi
- Türkçe karakter testi
- hint kaynak tüketim testi
- replay reward testi zaten korunmalı
- daily deterministic selection testi
- speed best update testi
- save migration idempotency testi

Daha sonra Compose UI testleri:

- Home → Klasik → Game
- Home → Hız → timeout
- Home → Günlük
- World unlock
- Market purchase
- Profile/Collection filter

---

## P2 — Görsel varlıklar ve fontlar

Kullanıcı resim üretilmesini istemiyor.

- AI ile ülke/dünya resmi üretme.
- Kullanıcı gerçek asset sağlarsa native resource olarak entegre edilebilir.
- Baloo 2 / Space Grotesk kullanılacaksa lisanslı/uygun font kaynakları projeye kullanıcı tarafından sağlanmalı veya güvenilir kaynakla açıkça eklenmeli.
- Font dosyalarını sohbetten paylaşma.

---

## P3 — Online backend (yalnız kullanıcı isterse)

Şu an local profile yeterlidir.

İleride istenirse:

- auth
- cloud save
- gerçek Top 10 leaderboard
- user profile sync
- ban/role backend

Sahte oyuncularla online leaderboard görüntüsü oluşturma.

---

# Her uygulama değişikliğinin Definition of Done'ı

1. Kod `Zafer-Hud/WordZ` main üzerinde.
2. `Zafer-Hud/KelimeYildiz` değişmedi.
3. Unit/core testler geçti.
4. Debug APK build geçti.
5. Rename geçti.
6. Artifact upload geçti.
7. Sürüm değiştiyse `CHANGELOG.md` güncellendi.
8. Yol haritasında durum değiştiyse `NEXT_STEPS.md` güncellendi.
9. Mimari/ürün kararı değiştiyse `PROJECT_CONTEXT.md` güncellendi.
10. Kullanıcıya yalnız doğrulanmış APK verildi.
