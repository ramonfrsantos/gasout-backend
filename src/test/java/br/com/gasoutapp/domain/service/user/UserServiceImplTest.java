package br.com.gasoutapp.domain.service.user;

import br.com.gasoutapp.application.dto.LoginResultDTO;
import br.com.gasoutapp.application.dto.user.LoginDTO;
import br.com.gasoutapp.application.dto.user.UserDTO;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.exception.UserAlreadyRegisteredException;
import br.com.gasoutapp.infrastructure.config.security.EncryptorCustom;
import br.com.gasoutapp.infrastructure.config.security.TokenService;
import br.com.gasoutapp.infrastructure.db.entity.enums.UserTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import br.com.gasoutapp.infrastructure.db.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    String adminEmail = "admintest@mail.com";
    String expectedUserId = "1";
    String expectedUserName = "User Test";
    String expectedUserEmail = "user@test.com";
    String expectedVerificationCode = "000000";
    String expectedPassword = EncryptorCustom.encrypt("password");
    User expectedUser;
    UserDTO expectedUserDTO;

    @Mock
    UserRepository userRepository;

    @Mock
    TokenService tokenService;

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    UserServiceImpl userService = new UserServiceImpl(adminEmail, adminEmail);

    @BeforeEach
    void setUp() {
        expectedUserDTO = new UserDTO();
        expectedUserDTO.setId(expectedUserId);
        expectedUserDTO.setName(expectedUserName);
        expectedUserDTO.setEmail(expectedUserEmail);
        expectedUserDTO.setPassword(expectedPassword);
        expectedUserDTO.setVerificationCode(expectedVerificationCode);

        expectedUser = new User();
        expectedUser.setId(expectedUserId);
        expectedUser.setName(expectedUserName);
        expectedUser.setEmail(expectedUserEmail);
        expectedUser.setPassword(expectedPassword);
        expectedUser.setVerificationCode(expectedVerificationCode);
        expectedUser.setRoles(List.of(UserTypeEnum.CLIENTE));
        expectedUser.setDeleted(false);
    }

    @Test
    void findAllTest() {
        var expectedUserList = List.of(expectedUserDTO);

        when(userRepository.findAll()).thenReturn(List.of(expectedUser));

        assertEquals(expectedUserList, userService.findAll());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findAllByRolesTest() {
        var expectedUserList = List.of(expectedUser);

        when(userRepository.findAllByRoles(any())).thenReturn(List.of(expectedUser));

        assertEquals(expectedUserList, userService.findAllByRoles(UserTypeEnum.CLIENTE));

        verify(userRepository, times(1)).findAllByRoles(any());
    }

    @Test
    void getVerificationCodeTest() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(expectedUser));

        assertEquals(expectedVerificationCode, userService.getVerificationCode("user@test.com"));

        verify(userRepository, times(1)).findByEmail(any());
    }

    @Test
    void getDtoByUserTest() {
        var loginResult = new LoginResultDTO();

        when(tokenService.createTokenForUser(any())).thenReturn(loginResult);

        assertEquals(loginResult, userService.getDtoByUser(new User()));

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void getDtoByUserAndUserNotNullNorEmptyNameTest() {
        var user = new User();
        user.setName(expectedUserName);

        var loginResult = new LoginResultDTO();

        when(tokenService.createTokenForUser(any())).thenReturn(loginResult);

        assertEquals(loginResult, userService.getDtoByUser(user));

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void checkIfCodesAreEqualTest() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(expectedUser));

        assertTrue(userService.checkIfCodesAreEqual("user@test.com", "000000"));

        verify(userRepository, times(1)).findByEmail(any());
    }

    @Test
    void findUserByIdTest() {
        var id = "1";

        when(userRepository.findById(any())).thenReturn(Optional.of(expectedUser));

        assertEquals(Optional.of(expectedUser), userService.findUserById(id));

        verify(userRepository, times(1)).findById(any());
    }

    @Test
    void registerTest() {
        var newUserDTO = new UserDTO("User Test", expectedUserEmail, "password");

        when(userRepository.save(any())).thenReturn(expectedUser);

        assertEquals(expectedUserDTO, userService.register(newUserDTO));

        verify(userRepository, times(1)).findByEmail(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void registerAdminUserTest() {
        var newUserDTO = new UserDTO("User Test", adminEmail, "password");

        var userAdmin = expectedUser;
        userAdmin.setEmail(adminEmail);
        userAdmin.setLogin(adminEmail);
        userAdmin.setRoles(List.of(UserTypeEnum.ADMIN));

        when(userRepository.save(any())).thenReturn(userAdmin);

        var newUser = userService.register(newUserDTO);

        assertEquals(adminEmail, newUser.getEmail());

        verify(userRepository, times(1)).findByEmail(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void registerThrowsUserAlreadyExistsException() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(expectedUser));

        var ex = assertThrows(UserAlreadyRegisteredException.class, this::invokeRegisterThrowsUserAlreadyExistsException);
        assertEquals("Usuário com esse email já foi cadastrado.", ex.getMessage());

        verify(userRepository, times(1)).findByEmail(any());
        verify(userRepository, never()).save(any());
    }

    private void invokeRegisterThrowsUserAlreadyExistsException() {
        var newUserDTO = new UserDTO("User Test", expectedUserEmail, "password");
        userService.register(newUserDTO);
    }

    @Test
    void refreshPasswordTest() {
        var loginDTO = new LoginDTO();
        loginDTO.setLogin(expectedUserEmail);
        loginDTO.setPassword("password");

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(expectedUser));

        assertEquals(expectedPassword, userService.refreshPassword(loginDTO).getPassword());
    }

    @Test
    void sendVerificationMailTest() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(expectedUser));
        when(userRepository.save(any())).thenReturn(expectedUser);

        assertNotNull(expectedVerificationCode, userService.sendVerificationMail("user@test.com"));

        verify(userRepository, times(1)).findByEmail(any());
        verify(userRepository, times(1)).save(any());
        verify(mailSender, times(1)).send((SimpleMailMessage) any());
    }

    @Test
    void deleteTest() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(expectedUser));

        assertEquals("Registro excluido com sucesso.", userService.delete("user@test.com"));
        assertTrue(expectedUser.isDeleted());

        verify(userRepository, times(1)).findByEmail(any());
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void findByEmailThrowsNotFoundException() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, this::invokeFindByEmailThrowsNotFoundException);
        assertEquals("Usuario com email [invalid@test.com] nao foi encontrado.", ex.getMessage());

        verify(userRepository, times(1)).findByEmail(any());
    }

    private void invokeFindByEmailThrowsNotFoundException() {
        userService.findByEmail("invalid@test.com");
    }

    @Test
    void setUserRoomsTest() {
        var newUser = new User();

        userService.setUserRooms(List.of(new Room()), newUser);

        assertEquals(1, newUser.getRooms().size());

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void setUserNotificationsTest() {
        var newUser = new User();

        userService.setUserNotifications(List.of(new Notification()), newUser);

        assertEquals(1, newUser.getNotifications().size());

        verify(userRepository, times(1)).save(any());
    }

}