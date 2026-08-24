import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class TestYaml {
    public static void main(String[] args) {
        try {
            YamlConfiguration.loadConfiguration(new File("C:\\Users\\L900m\\Downloads\\Shopkeepers-master\\shops.yml"));
            System.out.println("shops.yml is valid");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            YamlConfiguration.loadConfiguration(new File("C:\\Users\\L900m\\Downloads\\Shopkeepers-master\\npcs.yml"));
            System.out.println("npcs.yml is valid");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
