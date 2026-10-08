import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class passwordGen {
  public static void main(String[] args) {
    final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    System.out.print(passwordEncoder.encode("P@ssw0rd!"));
  }
}
