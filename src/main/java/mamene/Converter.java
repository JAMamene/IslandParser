package mamene;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Converter {

    public static void main(String[] args) {
        try {
            JSONObject json = new JSONObject(new String(Files.readAllBytes(Paths.get(args[0]))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
