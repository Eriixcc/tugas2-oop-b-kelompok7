# REST API Manajemen Event dan Ticketing

## 1. Deskripsi Singkat Proyek

Proyek ini adalah tugas Pemrograman Berorientasi Obyek (PBO) berupa REST API sederhana untuk **Ticketing System** menggunakan bahasa pemrograman Java. Sistem ini dibangun tanpa framework eksternal besar untuk server (menggunakan `com.sun.net.httpserver`), menggunakan database SQLite, serta Jackson untuk parsing JSON. Sistem dapat digunakan untuk mengelola user, venue, event, pembelian tiket, refund tiket, dan melihat laporan penjualan.

Beberapa konsep OOP yang diimplementasikan:
- **Abstract class**: `Event` sebagai class induk.
- **Inheritance**: `Concert`, `Seminar`, dan `SportMatch` mewarisi `Event`.
- **Interface**: `Refundable` yang diimplementasikan secara spesifik (misal pada `Concert`).
- **Polymorphism**: Metode seperti perhitungan harga tiket di-override pada setiap child class.
- **Encapsulation**: Atribut model diproteksi dengan *access modifier* `private` dan diakses melalui *getter/setter*.
- **Exception handling**: Custom exception untuk menangani kasus spesifik (contoh: tiket sold out, not found, dsb).

## 2. Cara Menjalankan Server

Berikut adalah langkah-langkah untuk menjalankan server mulai dari proses *clone* repositori hingga server berjalan.

1. **Clone Repositori**
   Buka terminal atau command prompt dan jalankan perintah berikut:
   ```bash
   git clone <URL_REPOSITORY>
   cd tugas2-oop-b-kelompok7
   ```

2. **Pindah ke Direktori `src`**
   Pastikan Anda berada di direktori `src` sebelum melakukan kompilasi.
   ```bash
   cd src
   ```

3. **Kompilasi Kode Program**
   Jalankan perintah kompilasi sesuai sistem operasi Anda (pastikan folder `lib` sejajar dengan `src` dan berisi file JAR yang dibutuhkan):

   **Windows:**
   ```bash
   javac -cp ".;../lib/*" App.java model/*.java service/*.java repository/*.java handler/*.java exception/*.java database/*.java server/*.java
   ```

   **Linux / Mac:**
   ```bash
   javac -cp ".:../lib/*" App.java model/*.java service/*.java repository/*.java handler/*.java exception/*.java database/*.java server/*.java
   ```

4. **Jalankan Server**
   **Windows:**
   ```bash
   java -cp ".;../lib/*" App
   ```

   **Linux / Mac:**
   ```bash
   java -cp ".:../lib/*" App
   ```

5. **Server Berjalan**
   Server akan berjalan secara lokal. Anda dapat mengujinya melalui browser, Curl, atau Postman.
   ```text
   http://localhost:8080
   ```

## 3. Daftar Endpoint API Lengkap Beserta Contoh Request & Response

Base URL: `http://localhost:8080`

| Method | Endpoint | Keterangan |
|--------|----------|------------|
| **GET** | `/api/users` | Mendapatkan semua user |
| **GET** | `/api/users/{id}` | Mendapatkan detail user (termasuk tiket/event) |
| **POST** | `/api/users` | Membuat user baru |
| **PUT** | `/api/users/{id}` | Memperbarui user |
| **GET** | `/api/venues` | Mendapatkan semua venue |
| **GET** | `/api/venues/{id}` | Mendapatkan detail venue |
| **POST** | `/api/venues` | Membuat venue baru |
| **PUT** | `/api/venues/{id}` | Memperbarui venue |
| **GET** | `/api/events` | Mendapatkan semua event |
| **GET** | `/api/events/price-summary` | Mendapatkan ringkasan harga event |
| **GET** | `/api/events/{id}` | Mendapatkan detail event |
| **POST** | `/api/events` | Membuat event baru |
| **PUT** | `/api/events/{id}` | Memperbarui event |
| **GET** | `/api/events/{id}/remaining-capacity`| Mendapatkan sisa kapasitas event |
| **GET** | `/api/tickets` | Mendapatkan semua tiket |
| **GET** | `/api/tickets/{id}` | Mendapatkan detail tiket |
| **POST** | `/api/tickets` | Membeli tiket |
| **PUT** | `/api/tickets/{id}/refund` | Melakukan refund tiket |
| **GET** | `/api/reports/sales` | Laporan penjualan event |

### Contoh Request & Response

#### A. Membuat User (Buyer)
**Request (POST `/api/users`)**
```json
{
  "name": "Kadek Surya",
  "email": "kadek.surya@email.com",
  "phone": "081234567890",
  "role": "buyer"
}
```
**Response (201 Created)**
```json
{
  "success": true,
  "data": {
    "id": "USR-12345",
    "name": "Kadek Surya",
    "email": "kadek.surya@email.com",
    "phone": "081234567890",
    "role": "buyer"
  }
}
```

#### B. Membuat Venue
**Request (POST `/api/venues`)**
```json
{
  "name": "GWK Cultural Park",
  "address": "Jl. Raya Uluwatu, Ungasan, Badung, Bali",
  "maxCapacity": 8000
}
```
**Response (201 Created)**
```json
{
  "success": true,
  "data": {
    "id": "VNU-12345",
    "name": "GWK Cultural Park",
    "address": "Jl. Raya Uluwatu, Ungasan, Badung, Bali",
    "maxCapacity": 8000
  }
}
```

#### C. Membuat Event (Concert)
**Request (POST `/api/events`)**
```json
{
  "type": "concert",
  "name": "Bali Music Festival 2026",
  "venueId": "VNU-12345",
  "organizerId": "USR-67890",
  "date": "2026-08-15",
  "basePrice": 250000,
  "capacity": {
    "vip": 100,
    "regular": 500,
    "festival": 1000
  }
}
```
**Response (201 Created)**
```json
{
  "success": true,
  "data": {
    "id": "EVT-12345",
    "type": "concert",
    "name": "Bali Music Festival 2026",
    "venueId": "VNU-12345",
    "organizerId": "USR-67890",
    "date": "2026-08-15",
    "basePrice": 250000
  }
}
```

#### D. Membeli Tiket
**Request (POST `/api/tickets`)**
```json
{
  "eventId": "EVT-12345",
  "userId": "USR-12345",
  "category": "vip",
  "quantity": 2
}
```
**Response (201 Created)**
```json
{
  "success": true,
  "data": {
    "id": "TKT-12345",
    "eventId": "EVT-12345",
    "userId": "USR-12345",
    "category": "vip",
    "quantity": 2,
    "totalPrice": 500000,
    "status": "active"
  }
}
```

#### E. Refund Tiket
**Request (PUT `/api/tickets/{id}/refund`)**
*(Body kosong)*
**Response (200 OK)**
```json
{
  "success": true,
  "message": "Tiket berhasil di-refund.",
  "data": {
    "id": "TKT-12345",
    "status": "refunded"
  }
}
```

## 4. Struktur Proyek

```text
tugas2-oop-b-kelompok7/
├── .git/
├── .idea/
├── .postman/
├── lib/                             # Folder berisi dependency library (.jar)
│   ├── jackson-annotations-2.13.3.jar
│   ├── jackson-core-2.13.3.jar
│   ├── jackson-databind-2.13.3.jar
│   └── sqlite-jdbc-3.49.1.0.jar
├── postman/
├── src/                             # Folder source code utama
│   ├── App.java                     # Main class penyedia web server
│   ├── database/                    # Konfigurasi & koneksi SQLite
│   ├── exception/                   # Custom class exception
│   ├── handler/                     # Controller / Router handler API
│   ├── model/                       # Data model OOP
│   ├── repository/                  # Akses & manipulasi data database
│   ├── server/                      # HTTP Server inti
│   └── service/                     # Business logic
├── database.db                      # File SQLite database
├── readme.md                        # Dokumentasi proyek (file ini)
└── tugas2_oop.postman_collection.json # File collection Postman untuk testing
```

## 5. Tabel Pembagian Tugas Anggota

| Nama | NIM | Tugas |
|------|-----|-------|
| Erick Fedryano Tenora | 2505551033 |  |
| Dewa Nyoman Prabu Wijaya Kusuma | 2505551040 | Events.java, Refundable.java, TicketRepository.java, VenueService.java, VenueHandler.java, TicketHandler.java |
| Muhamad Brian Alfiansyah | 2505551081 | - |
| Putu Aryadi Darma Kusuma | 250551148 | - | Venue.java, Ticket.java, eventrepository.java, Eventhandler.java, update Eventhandler.java
| Putu Wahyu Dinata | 2505551150 | - |
