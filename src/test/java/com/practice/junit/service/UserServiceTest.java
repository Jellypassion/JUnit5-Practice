package com.practice.junit.service;

import com.practice.junit.extension.UserServiceParamResolver;
import org.example.UserService;
import org.example.dao.UserDao;
import org.example.dto.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("user")
//When using this mode, a new test instance will be created once per test class.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith({
        UserServiceParamResolver.class,
//        GlobalExtension.class,
//        PostProcessingExtension.class
//        ThrowableExtension.class
        MockitoExtension.class
})
public class UserServiceTest {
    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETRO = User.of(2, "Petro", "345");

    @InjectMocks
    private UserService userService;// = new UserService(new UserDao());
    @Mock
    private UserDao userDao;

    UserServiceTest(TestInfo testInfo) {
        System.out.println();
    }

    static Stream<Arguments> getArgumentsForLoginTest() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petro", "345", Optional.of(PETRO)),
                Arguments.of("Petro", "dummy", Optional.empty()),
                Arguments.of("dummy", "345", Optional.empty())
        );
    }

    @BeforeAll
    void init() {
        System.out.println("Before All: " + this + "\n");
    }

    @BeforeEach
    void prepare() {
        System.out.println(("Before Each: " + this));
        // This is a mock object
//        this.userDao = Mockito.spy(new UserDao());
//        this.userService = new UserService(userDao);
    }

    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
        //this is a Stub
        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());

//        Mockito.when(userDao.delete(IVAN.getId())).thenReturn(true);

        //Mockito has a lot of standard methods for cases when we don't need to use the exact data, e.g. some dummy object
        //Mockito.doReturn(true).when(userDao).delete(Mockito.any());

        var deleteResult = userService.delete(IVAN.getId());
        assertThat(deleteResult).isTrue();
    }

    @Test
    @Order(1)
    void usersEmptyIfNoUserAdded(UserService userService) throws IOException {
        System.out.println("Test 1: " + this);
        var users = userService.getAll();
        assertTrue(users.isEmpty(), "User list should be empty");
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2: " + this);
        userService.add(IVAN, PETRO);

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

        //@Test
        @RepeatedTest(5)
        void loginFailedIfUserDoesNotExist() {
            System.out.println("Test 5: " + this);
            userService.add(IVAN);
            var noUser = userService.login("abc", IVAN.getPassword());
            assertTrue(noUser.isEmpty());
        }

        @Test
        void checkLoginFunctionalityPerformance() {
            assertTimeout(Duration.ofMillis(300), () -> {
//                Thread.sleep(300);
                return userService.login("abc", IVAN.getPassword());
            });
        }

        @ParameterizedTest(name = "{arguments} test")
        @MethodSource("com.practice.junit.service.UserServiceTest#getArgumentsForLoginTest")
        @DisplayName("Login Parametrized Test")
        void loginParametrizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, PETRO);
            var maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(user);
        }

    }

}
