# TravelHotel
Aplikasi desktop sederhana untuk manajemen hotel dan pemesanan kamar berbasis Java Swing dan MySQL.  
Dibuat sebagai tugas akhir mata kuliah Pemrograman Berorientasi Objek.

## Fitur Utama
- Login & Register (role admin/user)
- Booking kamar hotel dengan tanggal check-in & check-out
- Riwayat booking untuk user
- Upload & preview gambar kamar
- Filter kamar berdasarkan tipe dan harga
- Admin dapat mengubah status kamar tersedia
- Otomatis update status kamar setelah checkout
- UI modern dan responsif

## Cara Menjalankan
1. **Import database** dari file `travelhotel.sql` ke MySQL.
2. **Pastikan file library** berikut ada di folder `lib/`:
   - `jcalendar-1.4.jar`
   - `mysql-connector-j-9.3.0.jar`
3. **Compile dan jalankan aplikasi** dengan menambahkan kedua file JAR di `lib/` ke classpath.

### Contoh Compile & Run (Windows, Command Prompt)
```sh
javac -cp ".;lib/*" -d bin src\**\*.java
java -cp ".;bin;lib/*" App
```
> Jika menggunakan IDE (NetBeans, IntelliJ, VS Code), tambahkan kedua file JAR di `lib/` ke project dependencies/classpath.

## Database
- Menggunakan MySQL.
- Struktur dan data awal tersedia di file `travelhotel.sql`.

## Struktur Folder
```
img/                # Gambar kamar yang digunakan di app
lib/                # Library eksternal 
src/                # Source code Java
database/           # Koneksi database
ui/                 # Semua tampilan UI
App.java            # Main
travelhotel.sql     # File SQL untuk import database
Dokumentasi.pdf     # Dokumentasi singkat
```

## Kontributor
- Jeremy Revaldo Latuperisa
