package co.edu.unbosque.webscraper.dto;

/** Mensaje en el formato de chat de OpenAI (role: system | user | assistant). */
public record ChatMessageDTO(String role, String content) {
    public static ChatMessageDTO system(String c) { return new ChatMessageDTO("system", c); }
    public static ChatMessageDTO user(String c) { return new ChatMessageDTO("user", c); }
    public static ChatMessageDTO assistant(String c) { return new ChatMessageDTO("assistant", c); }
}
