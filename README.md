# VA-BBW — REST API Virtual Account

## 1. Deskripsi Project

VA-BBW adalah aplikasi backend berbasis REST API yang dikembangkan menggunakan Java dan Spring Boot untuk menangani transaksi Virtual Account (VA).

Aplikasi ini menyediakan tiga layanan utama:

1. **Authentication** — memvalidasi kredensial client dan menghasilkan token autentikasi.
2. **Inquiry Virtual Account** — memvalidasi permintaan dan mengambil informasi pemilik Virtual Account.
3. **Payment Virtual Account** — memvalidasi permintaan pembayaran dan menyimpan transaksi ke database.

Aplikasi menggunakan PostgreSQL untuk menyimpan informasi client API, Virtual Account, token autentikasi, dan transaksi pembayaran.

## 2. Teknologi yang Digunakan

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven
- Thunder Client untuk pengujian REST API

## 3. Persyaratan Sistem

Pastikan perangkat sudah memiliki:

- JDK 21
- PostgreSQL
- Visual Studio Code atau IDE Java lainnya
- Maven Wrapper yang tersedia di project
- Thunder Client atau REST client sejenis

## 4. Konfigurasi Database

Buat database PostgreSQL dengan nama `va_bbw` jika belum tersedia.

```sql
CREATE DATABASE va_bbw;
```

Konfigurasi koneksi database dapat diatur melalui `src/main/resources/application.properties`.

Contoh konfigurasi:

```properties
spring.application.name=va-bbw
server.port=7070

spring.datasource.url=jdbc:postgresql://localhost:5432/va_bbw
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Sesuaikan konfigurasi tersebut dengan file `application.properties` yang digunakan oleh project.

Pastikan data client API dan Virtual Account yang diperlukan untuk pengujian sudah tersedia di database.

## 5. Konfigurasi Environment Variable

Jika project menggunakan konfigurasi berikut:

```properties
bbw.client-password=${BBW_CLIENT_PASSWORD}
bbw.signature-secret=${BBW_SIGNATURE_SECRET}
```

Maka environment variable terkait harus tersedia saat aplikasi dijalankan.

Contoh pengaturan melalui PowerShell:

```powershell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "password_database"
$env:BBW_CLIENT_PASSWORD = "password_client"
$env:BBW_SIGNATURE_SECRET = "signature_secret_client"
```

Ganti nilai contoh tersebut dengan konfigurasi lokal yang benar.

**Penting:**
- Jangan menggunakan contoh password di atas sebagai kredensial sungguhan.
- Environment variable yang dibuat menggunakan `$env:` berlaku untuk sesi PowerShell tersebut dan proses turunannya.
- Nilai `BBW_SIGNATURE_SECRET` tidak otomatis menjadi secret yang dipakai oleh setiap request. Pada implementasi `SignatureService` yang diperiksa, validasi menggunakan `client.getSignatureSecret()`, yaitu secret milik client yang terhubung dengan token.
- Pastikan secret yang digunakan untuk membuat signature sesuai dengan client di database yang terkait dengan token tersebut.

## 6. Menjalankan Aplikasi

Buka terminal pada direktori utama project.

Jalankan aplikasi menggunakan Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run
```

Alternatif jika Maven sudah terpasang:

```powershell
mvn spring-boot:run
```

Untuk melakukan build project:

```powershell
.\mvnw.cmd clean package
```

Jika aplikasi berhasil berjalan, base URL yang diharapkan adalah:

```text
http://localhost:7070
```

Periksa log startup untuk memastikan koneksi PostgreSQL berhasil dan aplikasi tidak mengalami error konfigurasi.

## 7. Dokumentasi Endpoint API

Path berikut mengikuti rancangan dokumentasi VA-BBW. Pastikan path final sesuai dengan mapping controller di source code.

### 7.1 Authentication

**Endpoint**

```http
POST /api/auth/token
```

**URL lengkap**

```text
http://localhost:7070/api/auth/token
```

**Header**

```http
Authorization: Basic <base64(client_id:client_secret)>
Content-Type: application/x-www-form-urlencoded
```

HTTP Basic Authentication menggunakan format kredensial:

```text
client_id:client_secret
```

Kredensial tersebut di-encode menggunakan Base64.

**Request Body**

Pilih `x-www-form-urlencoded` di Thunder Client/postman:

| Key | Value |
|---|---|
| `grant_type` | `client_credentials` |

**Contoh response berhasil**

```json
{
  "auth_token": "contoh_auth_token",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

Response tersebut hanya ilustrasi. Gunakan token aktual yang dihasilkan aplikasi.

**Langkah pengujian di Thunder Client**

1. Buat request baru dengan method `POST`.
2. Masukkan URL Authentication.
3. Buka tab **Auth** dan pilih **Basic Auth**.
4. Masukkan `client_id` sebagai username dan `client_secret` sebagai password.
5. Buka tab **Body**, pilih `Form` atau `x-www-form-urlencoded`.
6. Tambahkan `grant_type` dengan nilai `client_credentials`.
7. Kirim request.
8. Simpan nilai `auth_token` dari response untuk pengujian berikutnya.

### 7.2 Inquiry Virtual Account

**Endpoint**

```http
POST /api/va/inquiry
```

**URL lengkap**

```text
http://localhost:7070/api/va/inquiry
```

**Header**

```http
Authorization: Bearer <auth_token>
Content-Type: application/json
X-BBW-Timestamp: <timestamp>
X-BBW-Signature: <signature>
```

**Contoh request body**

```json
{
  "client_id": "001",
  "reference_number": "000000000001",
  "virtual_account": "7771234567890"
}
```

**Contoh response berhasil**

```json
{
  "rc": "000",
  "message": "Success",
  "data": {
    "account_name": "Febri Haryadi"
  }
}
```

Inquiry digunakan untuk memeriksa Virtual Account dan memperoleh informasi pemilik akun.

### 7.3 Payment Virtual Account

**Endpoint**

```http
POST /api/va/payment
```

**URL lengkap**

```text
http://localhost:7070/api/va/payment
```

**Header**

```http
Authorization: Bearer <auth_token>
Content-Type: application/json
X-BBW-Timestamp: <timestamp>
X-BBW-Signature: <signature>
```

**Contoh request body**

```json
{
  "client_id": "001",
  "reference_number": "000000000002",
  "virtual_account": "7771234567890",
  "amount": "15000",
  "note": "buy a coffee"
}
```

**Contoh response berhasil**

```json
{
  "rc": "000",
  "message": "Success",
  "data": {
    "transaction_number": "contoh_nomor_transaksi"
  }
}
```

Nomor transaksi di atas hanya contoh. Nomor aktual dihasilkan oleh aplikasi.

Pastikan tipe data setiap field sesuai dengan DTO yang digunakan oleh project. Contoh di atas menggunakan string untuk `amount`.

## 8. Mekanisme Validasi Signature

Endpoint Inquiry dan Payment menggunakan signature untuk membantu memvalidasi integritas request.

Rumus yang digunakan:

```text
Signature = HMAC-SHA256(signature_secret, StringToSign)

StringToSign = AuthToken + ":" + HexEncode(SHA-256(RequestBody)) + ":" + Timestamp
```

Secara umum, prosesnya adalah:

1. Mengambil token autentikasi.
2. Membaca timestamp dari header `X-BBW-Timestamp`.
3. Melakukan canonicalization pada request body dengan menghapus karakter carriage return (`\r`), newline (`\n`), tab (`\t`), dan spasi.
4. Menghitung SHA-256 dari body yang sudah di-canonicalize.
5. Membentuk `StringToSign`.
6. Menghitung HMAC-SHA256 menggunakan signature secret milik client.
7. Membandingkan signature yang dihitung dengan header `X-BBW-Signature`.

Pada `SignatureService` yang diperiksa, timestamp ditolak jika selisih waktunya lebih dari 300 detik atau lima menit.

**Catatan kompatibilitas:** implementasi Java menserialisasi request body menggunakan Jackson sebelum menghitung hash. Urutan properti JSON, tipe data, dan canonicalization dapat memengaruhi hasil hash. Karena itu, signature yang dibuat oleh script harus cocok dengan cara aplikasi menserialisasi dan memproses body.

## 9. Inline Script PowerShell untuk Membuat Signature

Script berikut membantu developer membuat timestamp dan signature saat menguji Inquiry atau Payment melalui Thunder Client.

Script akan meminta:

- `auth_token` yang diperoleh dari Authentication;
- `signature_secret` milik client yang terkait dengan token.

Kemudian script menghitung SHA-256 dari body dan HMAC-SHA256 untuk menghasilkan header yang diperlukan.

### 9.1 Script

Salin seluruh kode berikut ke PowerShell:

```powershell
# VA-BBW - Signature Generator
# Gunakan hanya untuk pengujian lokal.
# Sesuaikan body dengan request yang akan dikirim.

$token = Read-Host "Masukkan auth_token"
$secret = Read-Host "Masukkan signature_secret milik client"

if ([string]::IsNullOrWhiteSpace($token)) {
    throw "Token tidak boleh kosong. Jalankan ulang script."
}

if ([string]::IsNullOrWhiteSpace($secret)) {
    throw "Signature secret tidak boleh kosong. Jalankan ulang script."
}

# Contoh body Payment.
# Ubah nilainya jika request Thunder Client berbeda.
$body = '{"client_id":"001","reference_number":"000000000002","virtual_account":"7771234567890","amount":"15000","note":"buy a coffee"}'

# Buat timestamp UTC.
$timestamp = [DateTime]::UtcNow.ToString("yyyy-MM-ddTHH:mm:ss.fffZ")

# Canonicalization sesuai aturan dokumentasi.
$canonicalBody = $body -replace '[\r\n\t ]', ''

# Hitung SHA-256 body.
$sha = [System.Security.Cryptography.SHA256]::Create()

$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($canonicalBody)

$bodyHash = [BitConverter]::ToString(
    $sha.ComputeHash($bodyBytes)
).Replace("-", "").ToLowerInvariant()

# Bentuk StringToSign.
$stringToSign = "${token}:${bodyHash}:${timestamp}"

# Hitung HMAC-SHA256.
$hmac = New-Object System.Security.Cryptography.HMACSHA256

$hmac.Key = [System.Text.Encoding]::UTF8.GetBytes($secret)

$signatureBytes = $hmac.ComputeHash(
    [System.Text.Encoding]::UTF8.GetBytes($stringToSign)
)

$signature = [BitConverter]::ToString(
    $signatureBytes
).Replace("-", "").ToLowerInvariant()

# Tampilkan hasil.
Write-Host ""
Write-Host "========== HASIL =========="
Write-Host "X-BBW-Timestamp:"
Write-Host $timestamp
Write-Host ""
Write-Host "X-BBW-Signature:"
Write-Host $signature
Write-Host "============================"
Write-Host "Salin kedua nilai tersebut ke header Thunder Client."
```

### 9.2 Cara Menggunakan Script

1. Jalankan script di PowerShell.
2. Masukkan token aktual dari endpoint Authentication.
3. Masukkan signature secret yang sesuai dengan client pada token tersebut.
4. Salin timestamp dan signature yang dihasilkan.
5. Buka request Inquiry atau Payment di Thunder Client.
6. Pastikan request body sama dengan nilai `$body` pada script.
7. Masukkan header sesuai tabel berikut.

| Header | Value |
|---|---|
| `Authorization` | `Bearer <auth_token>` |
| `Content-Type` | `application/json` |
| `X-BBW-Timestamp` | Timestamp hasil script |
| `X-BBW-Signature` | Signature hasil script |

### 9.3 Hal yang Harus Diperhatikan

- Buat signature baru setiap kali body request berubah.
- Gunakan timestamp terbaru yang dihasilkan script.
- Jangan menggunakan password PostgreSQL sebagai signature secret.
- Jangan menganggap `BBW_SIGNATURE_SECRET` pasti sama dengan secret milik client di database.
- Jika script menampilkan error karena secret kosong, jangan gunakan signature yang dihasilkan.
- Signature bisa tetap ditolak jika urutan properti atau serialisasi JSON tidak cocok dengan implementasi Java.

**Peringatan keamanan:** input `Read-Host` biasa terlihat ketika diketik. Jalankan script hanya di lingkungan yang aman dan jangan membagikan token atau signature secret ke chat, screenshot, log publik, atau repository.

## 10. Memeriksa Transaksi di PostgreSQL

Setelah Payment berhasil, periksa database untuk memastikan transaksi tersimpan.

Nama tabel dan kolom harus disesuaikan dengan entity serta skema database aktual.

Contoh kerangka query:

```sql
SELECT *
FROM <nama_tabel_transaksi>
WHERE reference_number = '000000000002';
```

Ganti `<nama_tabel_transaksi>` dengan nama tabel sebenarnya. Query di atas merupakan template, bukan query yang dapat langsung dijalankan sebelum nama tabel ditentukan.

Periksa apakah transaksi menyimpan data yang sesuai, seperti:

- reference number;
- Virtual Account;
- amount;
- transaction number;
- waktu transaksi.

Hindari mengirim request Payment berulang kali dengan reference number yang sama kecuali aplikasi memang mendukung mekanisme idempotency atau request duplikat.

## 11. Response Code

Daftar berikut mengikuti rancangan dokumentasi technical test. Pastikan pemetaan response code cocok dengan implementasi controller dan exception handler.

| Response Code | Keterangan |
|---|---|
| `000` | Permintaan berhasil |
| `001` | Header tidak lengkap |
| `002` | Token tidak valid |
| `003` | Virtual Account tidak ditemukan |
| `004` | Signature tidak valid |
| `005` | Client ID tidak ditemukan |
| `006` | Terjadi kesalahan umum |

Response aktual dapat berbeda tergantung implementasi masing-masing endpoint.

## 12. Troubleshooting

### Aplikasi gagal tersambung ke PostgreSQL

- Pastikan PostgreSQL berjalan.
- Periksa host, port, dan nama database.
- Periksa username dan password.
- Pastikan environment variable tersedia pada terminal yang menjalankan aplikasi.

### Authentication gagal

- Periksa `client_id` dan `client_secret`.
- Pastikan format Basic Authentication benar.
- Pastikan `grant_type=client_credentials` dikirim sebagai form data.
- Periksa log aplikasi.

### Token tidak valid

- Gunakan token terbaru.
- Pastikan token belum kedaluwarsa.
- Pastikan client terkait masih aktif.

### Timestamp tidak valid

- Buat timestamp baru menggunakan script.
- Pastikan jam komputer benar.
- Pastikan format timestamp mengikuti ISO-8601.
- Perhatikan batas waktu yang diterapkan server.

### Signature tidak valid

- Pastikan signature secret benar dan sesuai dengan client pada token.
- Pastikan body pada script sama dengan body di Thunder Client.
- Pastikan tipe data dan urutan properti JSON cocok.
- Pastikan timestamp dan signature dibuat pada waktu pengujian yang sama.
- Periksa canonicalization pada client dan server.

### Virtual Account tidak ditemukan

- Pastikan Virtual Account tersedia di database.
- Periksa nilai Virtual Account dan status datanya.
- Pastikan data client yang mengirim request sesuai dengan aturan aplikasi.

### Payment gagal

- Periksa response API dan log aplikasi.
- Pastikan request lolos validasi token, timestamp, signature, dan Virtual Account.
- Periksa database untuk memastikan transaksi tidak tersimpan atau tidak tercatat sebagai duplikat.

## 13. Keamanan

- Jangan menyimpan password, token, client secret, atau signature secret di repository.
- Jangan menggunakan kredensial asli di README.
- Gunakan kredensial khusus untuk pengujian.
- Jangan membagikan token atau secret melalui chat atau screenshot.
- Jika kredensial terlanjur terekspos, cabut atau rotasi sesuai mekanisme aplikasi.
- Untuk penggunaan produksi, gunakan pengelolaan secret yang sesuai dan hindari logging data sensitif.

## 14. Catatan Akhir

README ini menjelaskan cara menjalankan dan menguji VA-BBW. Sebelum digunakan sebagai dokumentasi final atau diserahkan untuk technical test.