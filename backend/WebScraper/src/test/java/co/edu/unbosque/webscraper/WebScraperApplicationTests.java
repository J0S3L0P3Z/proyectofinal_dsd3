package co.edu.unbosque.webscraper;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Prueba de integración: levanta TODA la aplicación (Spring, JPA, seguridad, Swagger) sobre una
 * base H2 en memoria y comprueba las reglas básicas de autenticación y autorización. No abre
 * Chrome ni llama a las IAs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:ittest;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "jwt.secret=integration-test-secret-0123456789-abcdefgh",
      "aes.key=llave-de-16-chrs",
      "aes.iv=iv-de-16-caract!",
      "app.mail.enabled=false",
      "app.seed.admin-username=admin",
      "app.seed.admin-email=admin@test.local",
      "app.seed.admin-password=Admin#12345",
      "app.seed.demo-username=normaluser",
      "app.seed.demo-email=normaluser@test.local",
      "app.seed.demo-password=Usuario#12345"
    })
class WebScraperApplicationTests {

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper mapper;
  @Autowired private JdbcTemplate jdbc;

  private String login(String user, String pass) throws Exception {
    String body =
        mvc.perform(
                post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode json = mapper.readTree(body);
    return json.get("token").asText();
  }

  @Test
  void healthIsPublic() throws Exception {
    mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void protectedEndpointWithoutTokenIs401() throws Exception {
    mvc.perform(get("/scrape/getall"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  void wrongPasswordIs401() throws Exception {
    mvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginByEmailWorksAndUserCannotUseAdminEndpoints() throws Exception {
    String token = login("normaluser@test.local", "Usuario#12345");
    mvc.perform(get("/user/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("normaluser"))
        .andExpect(jsonPath("$.password").doesNotExist());
    mvc.perform(get("/admin/stats").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCanReadStatsAndHistoryIsEmpty() throws Exception {
    String token = login("admin", "Admin#12345");
    mvc.perform(get("/admin/stats").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalUsers").isNumber());
    mvc.perform(get("/scrape/getall").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray());
  }

  @Test
  void registerForcesUserRoleAndRejectsWeakPassword() throws Exception {
    mvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"debil\",\"email\":\"debil@test.local\",\"password\":\"123\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"username\":\"intruso\",\"email\":\"intruso@test.local\",\"password\":\"Clave#2026\",\"role\":\"ADMIN\"}"))
        .andExpect(status().isCreated());
    String token = login("intruso", "Clave#2026");
    mvc.perform(get("/user/me").header("Authorization", "Bearer " + token))
        .andExpect(jsonPath("$.role").value("USER"));
  }

  @Test
  void ssrfUrlIsRejectedImmediately() throws Exception {
    String token = login("normaluser", "Usuario#12345");
    mvc.perform(
            post("/scrape/create")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"http://169.254.169.254/latest/meta-data/\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("URL_NOT_ALLOWED")));
  }

  @Test
  void emailIsStoredEncryptedButLoginByEmailStillWorks() throws Exception {
    String stored =
        jdbc.queryForObject("select email from useraccount where username = ?", String.class, "normaluser");
    assertFalse(stored.contains("@"), "el correo debe quedar cifrado en la base");
    assertEquals("normaluser@test.local", co.edu.unbosque.webscraper.util.AESUtil.decrypt(stored));
    String token = login("normaluser@test.local", "Usuario#12345");
    mvc.perform(get("/user/me").header("Authorization", "Bearer " + token))
        .andExpect(jsonPath("$.email").value("normaluser@test.local"));
  }
}
