package br.com.gasoutapp.domain.service.user;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.user.LoginDTO;
import br.com.gasoutapp.application.dto.user.UserDTO;
import br.com.gasoutapp.domain.exception.UserAlreadyRegisteredException;
import br.com.gasoutapp.infrastructure.config.security.EncryptorCustom;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    String expectedUserId = "1";
    String expectedUserEmail = "user@test.com";
    String expectedVerificationCode = "000000";

    @Mock
    User expectedUser;

    @Mock
    UserDTO expectedUserDTO;

    @Mock
    List<RevisionDTO> expectedRevisionsList;

    @Mock
    UserRepository userRepository;

    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        expectedUserDTO = new UserDTO();
        expectedUserDTO.setId(expectedUserId);
        expectedUserDTO.setName("User Test");
        expectedUserDTO.setEmail(expectedUserEmail);
        expectedUserDTO.setPassword(EncryptorCustom.encrypt("password"));
        expectedUserDTO.setVerificationCode(expectedVerificationCode);

        expectedUser = new User();
        expectedUser.setId(expectedUserId);
        expectedUser.setName("User Test");
        expectedUser.setEmail(expectedUserEmail);
        expectedUser.setPassword(EncryptorCustom.encrypt("password"));
        expectedUser.setVerificationCode(expectedVerificationCode);
        expectedUser.setDeleted(false);

        expectedRevisionsList = new ArrayList<>();
        expectedRevisionsList.add(new RevisionDTO());
    }

    @Test
    void findAllTest() {
        List<UserDTO> expectedUserList = List.of(expectedUserDTO);

        when(userRepository.findAll()).thenReturn(List.of(expectedUser));

        assertEquals(expectedUserList, userService.findAll());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getVerificationCodeTest() {
        when(userRepository.findByEmail(expectedUserEmail)).thenReturn(Optional.of(expectedUser));

        assertEquals(expectedVerificationCode, userService.getVerificationCode("user@test.com"));

        verify(userRepository, times(1)).findByEmail(expectedUserEmail);
    }

    @Test
    void checkIfCodesAreEqualTest() {
        when(userRepository.findByEmail(expectedUserEmail)).thenReturn(Optional.of(expectedUser));

        assertTrue(userService.checkIfCodesAreEqual("user@test.com", "000000"));

        verify(userRepository, times(1)).findByEmail(expectedUserEmail);
    }

    @Test
    void findUserByIdTest() {
        String id = expectedUserId;

        when(userRepository.findById(id)).thenReturn(Optional.of(expectedUser));

        assertEquals(Optional.of(expectedUser), userService.findUserById(id));

        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void registerTest() {
        var newUserDTO = new UserDTO("User Test", expectedUserEmail, "password");

        when(userRepository.save(any())).thenReturn(expectedUser);

        assertEquals(expectedUserDTO, userService.register(newUserDTO));

        verify(userRepository, times(1)).findByEmail(expectedUserEmail);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void registerTestUserAlreadyExists() {
        var newUserDTO = new UserDTO("User Test", expectedUserEmail, "password");

        when(userRepository.findByEmail(expectedUserEmail)).thenReturn(Optional.of(expectedUser));

        var ex = assertThrows(UserAlreadyRegisteredException.class, () -> userService.register(newUserDTO));

        assertEquals("Usuário com esse email já foi cadastrado.", ex.getMessage());

        verify(userRepository, times(1)).findByEmail(expectedUserEmail);
        verify(userRepository, never()).save(any());
    }

    @Test
    void refreshPasswordTest() {
        var loginDTO = new LoginDTO();
        loginDTO.setLogin(expectedUserEmail);
        loginDTO.setPassword("password");

        when(userRepository.findByEmail(expectedUserEmail)).thenReturn(Optional.of(expectedUser));

        assertEquals(expectedUserDTO, userService.refreshPassword(loginDTO));
    }

    @Test
    void sendVerificationMailTest() {
        when(userRepository.findByEmail(expectedUserEmail)).thenReturn(Optional.of(expectedUser));
        when(userRepository.save(any())).thenReturn(expectedUser);

        assertNotNull(expectedVerificationCode, userService.sendVerificationMail("user@test.com"));

        verify(userRepository, times(1)).findByEmail(expectedUserEmail);
        verify(userRepository, times(1)).save(any());
        verify(mailSender, times(1)).send((SimpleMailMessage) any());
    }

    @Test
    void deleteTest() {
        when(userRepository.findByEmail(expectedUserEmail)).thenReturn(Optional.of(expectedUser));

        assertEquals("Registro excluido com sucesso.", userService.delete("user@test.com"));
        assertTrue(expectedUser.isDeleted());

        verify(userRepository, times(1)).findByEmail(expectedUserEmail);
        verify(userRepository, times(1)).save(any());
    }
}