package net.evershell.everpartyspigot.Manager;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public class LanguageManager {
    private final Map<String, Map<String, String>> messages;

    public LanguageManager() {
        // Charger le fichier lang.yml
        InputStream inputStream = getClass().getResourceAsStream("/lang.yml");
        Yaml yaml = new Yaml();
        messages = yaml.load(inputStream);
    }

    public String getMessage(String lang, String key) {
        // Vérifier si la langue est disponible, sinon utiliser l'anglais par défaut
        Map<String, String> langMessages = messages.getOrDefault(lang, messages.get("en"));
        return langMessages.getOrDefault(key, "Message not found");
    }
}
