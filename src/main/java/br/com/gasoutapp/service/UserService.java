package br.com.gasoutapp.service;

import static br.com.gasoutapp.utils.StringUtils.createRandomCode;
import static br.com.gasoutapp.utils.StringUtils.normalizeString;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.gasoutapp.domain.Notification;
import br.com.gasoutapp.domain.Room;
import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.domain.enums.UserTypeEnum;
import br.com.gasoutapp.dto.LoginDTO;
import br.com.gasoutapp.dto.UserDTO;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.exception.UserAlreadyRegisteredException;
import br.com.gasoutapp.repository.UserRepository;
import br.com.gasoutapp.security.CriptexCustom;
import br.com.gasoutapp.security.LoginResultDTO;
import br.com.gasoutapp.security.TokenService;

@Service
public class UserService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String companyEmail;

	@Value("${user.admin.email}")
	private String adminEmail;

	@Value("${user.admin.password}")
	private String adminPassword;

	@Value("${user.admin.name}")
	private String adminName;

	public List<UserDTO> parseToDTO(List<User> list) {
		return list.stream().map(v -> parseToDTO(v)).collect(Collectors.toList());
	}

	public Page<UserDTO> parseToDTO(Page<User> page) {
		return page.map(UserDTO::new);
	}

	public UserDTO parseToDTO(User user) {
		return new UserDTO(user);
	}

	public User parseDTOToEntity(UserDTO userDTO) {
		User newUser = new User();

		newUser.setName(normalizeString(userDTO.getName()));
		newUser.setEmail(userDTO.getEmail());
		newUser.setLogin(userDTO.getEmail());
		newUser.setLastUpdate(new Date());

		String password = CriptexCustom.encrypt(userDTO.getPassword());

		newUser.setPassword(password);

		if (userDTO.getEmail().equals(adminEmail)) {
			newUser.getRoles().add(UserTypeEnum.ADMIN);
		} else {
			newUser.getRoles().add(UserTypeEnum.CLIENTE);
		}

		return newUser;
	}

	@Transactional
	public ResponseEntity<UserDTO> register(UserDTO userDTO) {
		User newUser = create(userDTO);

		URI locationUser = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newUser.getId()).toUri();

		return ResponseEntity.created(locationUser).body(parseToDTO(newUser));
	}

	public User create(UserDTO userDTO) {
		Optional<User> optUser = repository.findByEmail(userDTO.getEmail());

		if (optUser.isPresent()) {
			throw new UserAlreadyRegisteredException();
		}

		return repository.save(parseDTOToEntity(userDTO));
	}

	@Transactional
	public String delete(String login) {
		User user = findByLogin(login);
		user.setDeleted(true);

		repository.save(user);

		return "Registro excluido com sucesso.";
	}

	public List<UserDTO> findAll() {
		return parseToDTO(repository.findAll());
	}

	public String getVerificationCode(String login) {
		User user = findByEmail(login);

		return user.getVerificationCode();
	}

	public boolean checkIfCodesAreEqual(String login, String newCode) {
		User user = findByEmail(login);

		if (user.getVerificationCode().equals(newCode)) {
			return true;
		} else {
			return false;
		}
	}

	public String sendVerificationMail(String login) {
		User newUser;
		User user = findByEmail(login);

		newUser = user;

		System.out.println("Preparando para enviar a mensagem...");

		String verificationCode = createRandomCode(6, "0123456789");

		newUser.setVerificationCode(verificationCode);
		newUser = repository.save(newUser);

		String fullName = newUser.getName();
		String firstName = fullName.split(" ", 0)[0];

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(companyEmail);
		message.setTo(newUser.getEmail());
		message.setText("Olá, " + firstName + "! Seu código de verificação para a alteração da senha é:\n\n"
				+ newUser.getVerificationCode()
				+ "\n\nAgora é só entrar no aplicativo GasOut e escolher uma nova senha.");
		message.setSubject("Alteração de senha no aplicativo GasOut");

		mailSender.send(message);

		System.out.println("A Mensagem foi enviada.");

		return newUser.getVerificationCode();
	}

	public UserDTO refreshPassword(LoginDTO dto) {
		User newUser = findByEmail(dto.getLogin());
		newUser.setPassword(CriptexCustom.encrypt(dto.getPassword()));

		return parseToDTO(newUser);
	}

	public Optional<User> findUserById(String id) {
		return repository.findById(id);
	}

	public List<User> findAllByRoles(UserTypeEnum userType) {
		return repository.findAllByRoles(userType);
	}

	public LoginResultDTO getDtoByUser(User user, String tokenFirebase) {
		LoginResultDTO dto = this.tokenService.createTokenForUser(user);

		dto.setUserId(user.getId());
		if (user.getName() != null && !user.getName().equals("")) {
			dto.setUserName(normalizeString(user.getName()));
		}

		user.setTokenFirebase(CriptexCustom.encrypt(tokenFirebase));

		repository.save(user);

		return dto;
	}

	public User findByLogin(String login) {
		Optional<User> optUser = repository.findByLogin(login);
		if (optUser.isPresent()) {
			return optUser.get();
		} else {
			throw new NotFoundException("Usuario nao encontrado.");
		}
	}

	public User findByLoginAndPassword(String login, String password) {
		Optional<User> optUser = repository.findByLoginAndPassword(login, password);
		if (optUser.isPresent()) {
			return optUser.get();
		} else {
			throw new NotFoundException("Usuario nao encontrado.");
		}
	}

	private User findByEmail(String email) {
		Optional<User> optUser = repository.findByEmail(email);
		if (optUser.isPresent()) {
			return optUser.get();
		} else {
			throw new NotFoundException("Usuario nao encontrado.");
		}
	}

	public void setUserRooms(List<Room> newUserRooms, User user) {
		user.setRooms(newUserRooms);
		repository.save(user);
	}

	public void setUserNotifications(List<Notification> newUserNotifications, User user) {
		user.setNotifications(newUserNotifications);
		repository.save(user);
	}
}