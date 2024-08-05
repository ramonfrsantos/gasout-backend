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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
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
    AuthServiceImpl authServiceImpl = new AuthServiceImpl(expectedUserEmail, expectedPassword,"ADMIN");

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
        when(userService.findAllByRoles(any())).thenReturn(new ArrayList<>());
        when(userService.create(any())).thenReturn(expectedUser);
        when(userService.findByEmail(any())).thenReturn(expectedUser);
        when(userService.getDtoByUser(any())).thenReturn(loginResultDTO);

        var result = authServiceImpl.checkIfAdminExists();

        assertNotNull(result);
        assertEquals(loginResultDTO.getToken(), result);

        verify(userService, times(1)).findAllByRoles(any());
        verify(userService, times(1)).create(any());
        verify(userService, times(1)).findByEmail(any());
        verify(userService, times(1)).getDtoByUser(any());
    }

    @Test
    void checkIfAdminExistsThrowsUserNotFoundException() {
        when(userService.findAllByRoles(any())).thenReturn(new ArrayList<>());
        when(userService.create(any())).thenReturn(expectedUser);
        when(userService.findByEmail(any())).thenReturn(null);

        var ex = assertThrows(NotFoundException.class, this::invokeCheckIfAdminExistsThrowsUserNotFoundException);
        assertEquals("Dados de login incorretos.", ex.getMessage());

        verify(userService, times(1)).findAllByRoles(any());
        verify(userService, times(1)).create(any());
        verify(userService, times(1)).findByEmail(any());
    }

    @Test
    void checkIfAdminExistsThrowsWrongPasswordException() {
        var invalidUser = new User();
        invalidUser.setPassword(invalidPassword);

        when(userService.findAllByRoles(any())).thenReturn(new ArrayList<>());
        when(userService.create(any())).thenReturn(invalidUser);
        when(userService.findByEmail(any())).thenReturn(expectedUser);

        var ex = assertThrows(WrongPasswordException.class, this::invokeCheckIfAdminExistsThrowsWrongPasswordException);
        assertEquals("Senha incorreta.", ex.getMessage());

        verify(userService, times(1)).findAllByRoles(any());
        verify(userService, times(1)).create(any());
        verify(userService, times(1)).findByEmail(any());
    }

    @Test
    void adminAlreadyExistsTest() {
        when(userService.findAllByRoles(any())).thenReturn(List.of(expectedUser));

        var result = authServiceImpl.checkIfAdminExists();

        assertNotNull(result);
        assertEquals("Usuário [ADMIN] já existe no sistema.", result);

        verify(userService, times(1)).findAllByRoles(any());
    }

    static String createNewToken(User user){
        int yearCount = 1;
        int monthCount = 4;
        int dayCount = 30;
        int hourCount = 24;

        int hoursTimeout = yearCount * monthCount * dayCount * hourCount;

        var calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        calendar.add(Calendar.HOUR, hoursTimeout);

        return Jwts.builder().claim("id", user.getId()).claim("roles", user.getRoles())
                .setSubject(user.getLogin()).setExpiration(calendar.getTime())
                .signWith(SignatureAlgorithm.HS512, SecurityFilter.getSecretKey()).compact();
    }

    private void invokeCheckIfAdminExistsThrowsUserNotFoundException() {
        authServiceImpl.checkIfAdminExists();
    }

    private void invokeCheckIfAdminExistsThrowsWrongPasswordException() {
        authServiceImpl.checkIfAdminExists();
    }
}