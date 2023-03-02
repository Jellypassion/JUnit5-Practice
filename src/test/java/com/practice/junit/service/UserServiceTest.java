package com.practice.junit.service;

import org.example.UserService;
import org.example.dto.User;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("user")
//When using this mode, a new test instance will be created once per test class.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {
    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETRO = User.of(2, "Petro", "345");
    private UserService userService = new UserService();

    @BeforeAll
    void init() {
        System.out.println("Before All: " + this + "\n");
    }

    @BeforeEach
    void prepare() {
        System.out.println(("Before Each: " + this));
        userService = new UserService();
    }

    @Test
    @Order(1)
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1: " + this);
        var users = userService.getAll();
        assertTrue(users.isEmpty(), "User list should be empty");
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN);
        userService.add(PETRO);

        var users = userService.getAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, PETRO);
        Map<Integer, User> users = userService.getAllConvertedById();
        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETRO.getId()),
                () -> assertThat(users).containsValues(IVAN, PETRO)
        );

    }

    @AfterEach
    void deleteDataFromDatabase() {
        System.out.println("After Each: " + this + "\n");
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After All: " + this);
    }

    @Tag("login")
    @Nested
    class LoginTest {
        @Test
        void loginSuccessIfUserExists() {
            System.out.println("Test 3: " + this);
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());
            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
        }

        @Test
        void throwExceptionsIfUserOrPasswordIsnull() {
            assertAll(
                    () -> {
                        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy"));
                        assertThat(exception.getMessage()).isEqualTo("Username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login(IVAN.getUsername(), null))
            );
        }

        @Test
        void loginFailIfPasswordIsNotCorrect() {
            System.out.println("Test 4: " + this);
            userService.add(IVAN);
            var noUser = userService.login(IVAN.getUsername(), "wrongPass");
            assertTrue(noUser.isEmpty());
        }

        @Test
        void loginFailedIfUserDoesNotExist() {
            System.out.println("Test 5: " + this);
            userService.add(IVAN);
            var noUser = userService.login("abc", IVAN.getPassword());
            assertTrue(noUser.isEmpty());
        }
    }

}
