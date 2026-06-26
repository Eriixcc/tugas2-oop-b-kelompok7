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
   git clone https://github.com/Eriixcc/tugas2-oop-b-kelompok7.git
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
├── .postman/
├── lib/                             # Folder berisi dependency library (.jar)
│   ├── jackson-annotations-2.13.3.jar
│   ├── jackson-core-2.13.3.jar
│   ├── jackson-databind-2.13.3.jar
│   └── sqlite-jdbc-3.49.1.0.jar
├── postman/
├── src/  
│   ├── .idea/                       # Folder source code utama
│   ├── App.java                     # Main class penyedia web server
│   ├── database/                    # Konfigurasi & koneksi SQLite
│   ├── exception/                   # Custom class exception
│   ├── handler/                     # Controller / Router handler API
│   ├── model/                       # Data model OOP
│   ├── repository/                  # Akses & manipulasi data database
│   ├── server/                      # HTTP Server inti
│   ├── service/                     # Business logic
│   └── tugas2.iml
├── database.db                      # File SQLite database
├── readme.md                        # Dokumentasi proyek (file ini)
└── tugas2_oop.postman_collection.json # File collection Postman untuk testing
```

## 5. Tabel Pembagian Tugas Anggota

| Nama | NIM | Tugas |
|------|-----|-------|
| Erick Fedryano Tenora | 2505551033 | Database, Concert.java, Seminar.java, UserHandler.java, UserService.java, Readme.md |
| Dewa Nyoman Prabu Wijaya Kusuma | 2505551040 | Events.java, Refundable.java, TicketRepository.java, VenueService.java, VenueHandler.java, TicketHandler.java |
| Muhamad Brian Alfiansyah | 2505551081 | create table capacities and tickets, fix bug, SportMatch.java, VenueRepository.java, EventService.java, Route on App.java, Test postman |
| Putu Aryadi Darma Kusuma | 250551148 | Venue.java, Ticket.java, EventRepository.java, EventHandler.java |
| Putu Wahyu Dinata | 2505551150 | User.java, EventNotFoundException.java, UserRepository.java, TicketHandler.java |

## 6. Dokumentasi Postman

// [GET] /api/users
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 210024" src="https://github.com/user-attachments/assets/d55a1b48-19d7-4f0b-980a-b1c8f0e0e8cd" />

// [GET] /api/users/{id}
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 211550" src="https://github.com/user-attachments/assets/b22f3501-43c9-422a-9d74-ae048b53adf3" />

// [GET] /api/venues
<img width="958" height="598" alt="Cuplikan layar 2026-06-26 210058" src="https://github.com/user-attachments/assets/34d1322b-54cb-4d18-b68b-ea14ea33a13d" />

// [GET] /api/venues/{id}
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 211638" src="https://github.com/user-attachments/assets/0d4b2ac2-ba6a-433b-ba62-7b0b2c1fcfe4" />

// [GET] /api/events
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 210844" src="https://github.com/user-attachments/assets/0dc49e58-75ea-4226-bf68-f1fb94518c79" />

// [GET] /api/events/{id}
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 212028" src="https://github.com/user-attachments/assets/5258c222-2934-40db-8037-c6cf1fa26a1d" />

// [GET] /api/events/{id}/remaining-capacity
<img width="956" height="595" alt="Cuplikan layar 2026-06-26 214639" src="https://github.com/user-attachments/assets/c7eeb494-f7fe-4f3c-ba3a-97399ea29d6a" />

// [GET] /api/events/{id}/sales-report
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 220142" src="https://github.com/user-attachments/assets/8fc8084e-6ef2-48c9-b712-c38556f76a16" />

// [GET] /api/tickets
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 211031" src="https://github.com/user-attachments/assets/433f5815-bfba-45eb-a1d6-b2ff038744fe" />

// [GET] /api/tickets/{id}
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 212721" src="https://github.com/user-attachments/assets/40d6536a-46ff-43c4-912e-63f25c74cb8d" />


// [POST] /api/users
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 211338" src="https://github.com/user-attachments/assets/ad4a754e-9f67-47c5-97ca-0cbd7756bba0" />

// [POST] /api/venues
<img width="960" height="600" alt="Cuplikan layar 2026-06-26 213611" src="https://github.com/user-attachments/assets/cc966dbc-917c-43ef-b913-615826a86800" />

// [POST] /api/events
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 223422" src="https://github.com/user-attachments/assets/5f61f9b5-7be0-4e25-9a25-b0158f39f192" />

// [POST] /api/tickets
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 223633" src="https://github.com/user-attachments/assets/99e2bb1c-0aeb-4d23-9a69-47513bc6443c" />


// [PUT] /api/users/{id}
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 224050" src="https://github.com/user-attachments/assets/1aed2193-7f86-4d86-a68e-32b0cf46e416" />

// [PUT] /api/venues/{id}
<img width="959" height="599" alt="Cuplikan layar 2026-06-26 213710" src="https://github.com/user-attachments/assets/5f5c6129-cf4d-4c4d-a25c-e1aa2bc4162c" />

// [PUT] /api/events/{id}
<img width="960" height="600" alt="image" src="https://github.com/user-attachments/assets/eafef790-3cbe-448b-8897-b8fdcaf16391" />

// [PUT] /api/tickets/{id}/refund
<img width="960" height="600" alt="Cuplikan layar 2026-06-26 220036" src="https://github.com/user-attachments/assets/fabe55b3-570f-4976-83df-7862937edb6e" />


