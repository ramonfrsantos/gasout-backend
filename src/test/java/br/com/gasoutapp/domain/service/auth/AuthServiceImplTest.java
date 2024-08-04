package br.com.gasoutapp.domain.service.auth;

import br.com.gasoutapp.application.dto.LoginResultDTO;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.exception.WrongPasswordException;
import br.com.gasoutapp.domain.service.user.UserService;
import br.com.gasoutapp.infrastructure.config.security.EncryptorCustom;
import br.com.gasoutapp.infrastructure.config.security.SecurityFilter;
import br.com.gasoutapp.infrastructure.db.entity.enums.UserTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    private static final int YEAR_COUNT = 1;
    private static final int MONTH_COUNT = 4;
    private static final int DAY_COUNT = 30;
    private static final int HOUR_COUNT = 24;

    private static final  int HORAS_TIMEOUT = YEAR_COUNT * MONTH_COUNT * DAY_COUNT * HOUR_COUNT;

    String expectedUserId = "1";
    String expectedUserEmail = "user@test.com";
    String expectedVerificationCode = "000000";
    String expectedPassword = EncryptorCustom.encrypt("password");
    String invalidPassword = EncryptorCustom.encrypt("invalid");

    @Mock
    User expectedUser;

    @Mock
    LoginResultDTO loginResultDTO;

    @Mock
    UserService userService;

    @InjectMocks
    AuthServiceImpl authServiceImpl;

    @BeforeEach
    void setUp() {
        expectedUser = new User();
        expectedUser.setId(expectedUserId);
        expectedUser.setName("User Test");
        expectedUser.setEmail(expectedUserEmail);
        expectedUser.setLogin(expectedUserEmail);
        expectedUser.setPassword(expectedPassword);
        expectedUser.setVerificationCode(expectedVerificationCode);
        expectedUser.setDeleted(false);
        expectedUser.setRoles(List.of(UserTypeEnum.ADMIN));

        loginResultDTO = new LoginResultDTO();
        loginResultDTO.setToken(createNewToken(expectedUser));
        loginResultDTO.setRefreshToken(createNewToken(expectedUser));
    }

    @Test
    void checkIfAdminExistsTest() {
        when(userService.findAllByRoles(any())).thenReturn(List.of());
        when(userService.create(any())).thenReturn(expectedUser);
        when(userService.findByLogin(any())).thenReturn(expectedUser);
        when(userService.getDtoByUser(any())).thenReturn(loginResultDTO);

        var result = authServiceImpl.checkIfAdminExists();

        assertNotNull(result);
        assertEquals(loginResultDTO.getToken(), result);
    }

    @Test
    void checkIfAdminExistsUserIsNullTest() {
        when(userService.findAllByRoles(any())).thenReturn(List.of());
        when(userService.create(any())).thenReturn(expectedUser);
        when(userService.findByLogin(any())).thenReturn(null);

        var ex = assertThrows(NotFoundException.class, () -> authServiceImpl.checkIfAdminExists());
        assertEquals("Dados de login incorretos.", ex.getMessage());
    }

    @Test
    void checkIfAdminExistsWrongPasswordTest() {
        var invalidUser = new User();
        invalidUser.setPassword(invalidPassword);

        when(userService.findAllByRoles(any())).thenReturn(List.of());
        when(userService.create(any())).thenReturn(invalidUser);
        when(userService.findByLogin(any())).thenReturn(expectedUser);

        var ex = assertThrows(WrongPasswordException.class, () -> authServiceImpl.checkIfAdminExists());
        assertEquals("Senha incorreta.", ex.getMessage());
    }

    @Test
    void adminAlreadyExistsTest() {
        when(userService.findAllByRoles(any())).thenReturn(List.of(expectedUser));

        var result = authServiceImpl.checkIfAdminExists();

        assertNotNull(result);
        assertEquals("Usuário [ADMIN] já existe no sistema.", result);
    }

    static String createNewToken(User user){
        var calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        calendar.add(Calendar.HOUR, HORAS_TIMEOUT);

        return Jwts.builder().claim("id", user.getId()).claim("roles", user.getRoles())
                .setSubject(user.getLogin()).setExpiration(calendar.getTime())
                .signWith(SignatureAlgorithm.HS512, SecurityFilter.getSecretKey()).compact();
    }
}