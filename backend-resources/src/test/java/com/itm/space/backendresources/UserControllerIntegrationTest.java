package com.itm.space.backendresources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.controller.UserController;
import com.itm.space.backendresources.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void testCreateWithStatus201() throws Exception {
        UserRequest userRequest = new UserRequest(
                "Bob",
                "userName@fackexample.com",
                "7894",
                "NameOne",
                "NameTwo"
        );


        // Выполняем POST запрос для создания пользователя
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated()); // Ожидаем статус 201 Created

    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void testNotCreateWithStatus409() throws Exception {
        UserRequest userRequest = new UserRequest(
                "user",
                "username@example.com",
                "12345",
                "FirstN",
                "LastN"
        );

        // Настраиваем мок, чтобы метод createUser ничего не делал (успешное создание)
        doNothing().when(userService).createUser(any(UserRequest.class)); // Используйте any() для проверки

        // Выполняем POST запрос для создания пользователя
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isConflict());

    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void testGetUserById() throws Exception {
        UUID userId = UUID.fromString("23d64adb-3702-4a10-bc9a-a8df36cc76b4");

        // Создаем ожидаемый объект UserResponse
        UserResponse userResponse = new UserResponse("FirstName", "LastName", "user@example.com",
                Arrays.asList("ROLE_USER"),
                Arrays.asList("Group1", "Group2"));

        // Мокируем поведение userService
        when(userService.getUserById(userId)).thenReturn(userResponse);

        // Сериализуем объект и выводим в консоль
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonResponse = objectMapper.writeValueAsString(userResponse);
        System.out.println("Serialized UserResponse: " + jsonResponse);

        // Выполняем запрос и проверяем ответ
        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(""))
                .andExpect(jsonPath("$.lastName").value(""))
                .andExpect(jsonPath("$.email").value("samasiuk1986@gmail.com"));

    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    void getUserByIdShould404WhenNotFound() throws Exception {
        String uuid = "user";

        mockMvc.perform(get("/api/users/{uuid}", uuid)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    public void testHello() throws Exception {
        mockMvc.perform(get("/api/users/hello")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("user"));
    }
}