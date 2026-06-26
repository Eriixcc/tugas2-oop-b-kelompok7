# REST API Manajemen Event dan Ticketing

Proyek ini adalah tugas Pemrograman Berorientasi Obyek berupa REST API sederhana menggunakan Java, SQLite, Jackson, dan `com.sun.net.httpserver`. Sistem digunakan untuk mengelola user, venue, event, pembelian tiket, refund tiket, dan laporan penjualan.

## Konsep OOP yang Digunakan

- **Abstract class**: `Event` menjadi class induk untuk semua jenis event dan memiliki method abstract `calculateTicketPrice(String category)`.
- **Inheritance**: `Concert`, `Seminar`, dan `SportMatch` mewarisi class `Event`.
- **Interface**: `Refundable` hanya diimplementasikan oleh `Concert` dan `Seminar`.
- **Polymorphism**: Harga tiket dihitung melalui pemanggilan `event.calculateTicketPrice(category)` sehingga hasilnya berbeda sesuai objek aslinya.
- **Encapsulation**: Semua atribut model dibuat `private` dan diakses melalui getter dan setter.
- **Exception handling**: Terdapat custom exception untuk data tidak ditemukan, tiket sold out, dan refund tidak diizinkan.

## Struktur Proyek

```text
src/
├── App.java
├── database/DatabaseManager.java
├── exception/
├── handler/
├── model/
├── repository/
├── server/
└── service/
```

## Cara Menjalankan

Jalankan perintah berikut dari folder `src`.

### Windows

```bash
javac -cp ".;../lib/*" App.java model/*.java service/*.java repository/*.java handler/*.java exception/*.java database/*.java server/*.java
java -cp ".;../lib/*" App
```

### Linux / Mac

```bash
javac -cp ".:../lib/*" App.java model/*.java service/*.java repository/*.java handler/*.java exception/*.java database/*.java server/*.java
java -cp ".:../lib/*" App
```

Server berjalan di:

```text
http://localhost:8080
```

## Daftar Endpoint

| Method | Endpoint | Fungsi |
|---|---|---|
| GET | `/api/users` | Menampilkan semua user |
| GET | `/api/users/{id}` | Detail user dan ringkasan aktivitas |
| POST | `/api/users` | Membuat user baru |
| PUT | `/api/users/{id}` | Update user |
| GET | `/api/venues` | Menampilkan semua venue |
| GET | `/api/venues/{id}` | Detail venue dan daftar event |
| POST | `/api/venues` | Membuat venue baru |
| PUT | `/api/venues/{id}` | Update venue |
| GET | `/api/events` | Menampilkan semua event |
| GET | `/api/events/{id}` | Detail event |
| POST | `/api/events` | Membuat event baru |
| PUT | `/api/events/{id}` | Update event |
| GET | `/api/tickets` | Menampilkan semua tiket |
| GET | `/api/tickets/{id}` | Detail tiket |
| POST | `/api/tickets` | Membeli tiket |
| PUT | `/api/tickets/{id}/refund` | Refund tiket |
| GET | `/api/events/price-summary` | Ringkasan harga tiket |
| GET | `/api/reports/sales?eventId={id}` | Laporan penjualan event |

## Contoh Request Postman

### 1. Membuat Organizer

`POST /api/users`

```json
{
  "name": "Bali Event Organizer",
  "email": "info@balievent.id",
  "phone": "081987654321",
  "role": "organizer"
}
```

### 2. Membuat Buyer

`POST /api/users`

```json
{
  "name": "Kadek Surya",
  "email": "kadek.surya@email.com",
  "phone": "081234567890",
  "role": "buyer"
}
```

### 3. Membuat Venue

`POST /api/venues`

```json
{
  "name": "GWK Cultural Park",
  "address": "Jl. Raya Uluwatu, Ungasan, Badung, Bali",
  "maxCapacity": 8000
}
```

### 4. Membuat Event Concert

`POST /api/events`

```json
{
  "type": "concert",
  "name": "Bali Music Festival 2026",
  "venueId": "VNU-ISI_ID_VENUE",
  "organizerId": "USR-ISI_ID_ORGANIZER",
  "date": "2026-08-15",
  "basePrice": 250000,
  "capacity": {
    "vip": 100,
    "regular": 500,
    "festival": 1000
  }
}
```

### 5. Membeli Tiket

`POST /api/tickets`

```json
{
  "eventId": "EVT-ISI_ID_EVENT",
  "userId": "USR-ISI_ID_BUYER",
  "category": "vip",
  "quantity": 2
}
```

### 6. Refund Tiket

`PUT /api/tickets/{id}/refund`

Tidak perlu body JSON.

## Skema Database

Database menggunakan SQLite dengan tabel:

- `users`
- `venues`
- `events`
- `capacities`
- `tickets`

Relasinya adalah user organizer dapat membuat banyak event, venue dapat memiliki banyak event, event memiliki banyak kategori kapasitas, event memiliki banyak tiket, dan user buyer dapat membeli banyak tiket.

## Pembagian Tugas

| Anggota | NIM | Tanggung Jawab |
|---|---|---|
| Erick | - | Model, service, repository, handler, database, README |
| Anggota 2 | - | Sesuaikan dengan kelompok |
| Anggota 3 | - | Sesuaikan dengan kelompok |
| Anggota 4 | - | Sesuaikan dengan kelompok |

## Skenario Demo

1. Buat user organizer.
2. Buat user buyer.
3. Buat venue.
4. Buat event concert.
5. Cek detail event dan price list.
6. Beli tiket kategori VIP.
7. Cek laporan penjualan.
8. Refund tiket concert.
9. Buat sport match lalu coba refund untuk menunjukkan error karena `SportMatch` tidak mengimplementasikan `Refundable`.
