package com.example.demo;

import com.example.demo.controller.UserController.CreateUserRequest;
import com.example.demo.controller.UserController.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("GET /api/users - should return all users")
    void getAllUsers_shouldReturnList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].email").exists());
    }

    @Test
    @DisplayName("GET /api/users/1 - should return existing user")
    void getUserById_whenExists_shouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("GET /api/users/999 - should return 404")
    void getUserById_whenNotExists_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/users - should create user and return 201")
    void createUser_withValidData_shouldReturn201() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Charlie", "charlie@example.com");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Charlie"))
                .andExpect(jsonPath("$.email").value("charlie@example.com"))
                .andReturn();

        // Optional: parse response body
        UserResponse body = objectMapper.readValue(
                result.getResponse().getContentAsString(), UserResponse.class);
        assertThat(body.id()).isNotNull();
        assertThat(body.name()).isEqualTo("Charlie");
    }

    @Test
    @DisplayName("POST /api/users - should return 400 on validation error")
    void createUser_withInvalidData_shouldReturn400() throws Exception {
        // Missing name and invalid email
        String invalidJson = """
                {
                  "name": "",
                  "email": "not-an-email"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("PUT /api/users/2 - should update existing user")
    void updateUser_whenExists_shouldReturnUpdatedUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Bob Updated", "bob.updated@example.com");

        mockMvc.perform(put("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Bob Updated"))
                .andExpect(jsonPath("$.email").value("bob.updated@example.com"));
    }

    @Test
    @DisplayName("PUT /api/users/999 - should return 404")
    void updateUser_whenNotExists_shouldReturn404() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Ghost", "ghost@example.com");

        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - should delete and return 204")
    void deleteUser_whenExists_shouldReturn204() throws Exception {
        // First create a temporary user to delete
        CreateUserRequest request = new CreateUserRequest("Temp", "temp@example.com");
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        UserResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), UserResponse.class);

        mockMvc.perform(delete("/api/users/" + created.id()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/api/users/" + created.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/users/999 - should return 404")
    void deleteUser_whenNotExists_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/users/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
