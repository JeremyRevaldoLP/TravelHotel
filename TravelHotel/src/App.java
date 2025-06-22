import java.sql.Connection;
import database.Database;
import ui.LoginUI;

public class App {
    public static void main(String[] args) {
        new LoginUI().setVisible(true); // Menampilkan UI Login
        
        try {
            Connection conn = Database.connect();
            System.out.println("Koneksi ke database berhasil");
        } catch (Exception e) {
            System.out.println("Gagal koneksi: " + e.getMessage());
        }
    }
}
