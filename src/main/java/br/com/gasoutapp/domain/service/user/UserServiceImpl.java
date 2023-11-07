package br.com.gasoutapp.domain.service.user;

import static br.com.gasoutapp.infrastructure.utils.JsonUtil.convertToObjectArray;
import static br.com.gasoutapp.infrastructure.utils.StringUtils.createRandomCode;
import static br.com.gasoutapp.infrastructure.utils.StringUtils.normalizeString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.transaction.Transactional;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.user.LoginDTO;
import br.com.gasoutapp.application.dto.user.UserDTO;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.exception.UserAlreadyRegisteredException;
import br.com.gasoutapp.infrastructure.config.security.CriptexCustom;
import br.com.gasoutapp.infrastructure.config.security.LoginResultDTO;
import br.com.gasoutapp.infrastructure.config.security.TokenService;
import br.com.gasoutapp.infrastructure.db.entity.enums.UserTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import br.com.gasoutapp.infrastructure.db.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
	
    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

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

	@Autowired
	private AuditReader auditReader;

	@Transactional
	public UserDTO register(UserDTO userDTO) {
		User newUser = create(userDTO);

		return parseToDTO(newUser);
	}

	@Override
	public User create(UserDTO userDTO) {
		Optional<User> optUser = repository.findByEmail(userDTO.getEmail());

		if (optUser.isPresent()) {
			throw new UserAlreadyRegisteredException();
		}

		return repository.save(parseDTOToEntity(userDTO));
	}

	@Override
	public String delete(String login) {
		User user = findByLogin(login);
		user.setDeleted(true);

		repository.save(user);

		return "Registro excluido com sucesso.";
	}

	@Override
	public List<UserDTO> findAll() {
		return parseToDTO(repository.findAll());
	}

	@Override
	public String getVerificationCode(String login) {
		User user = findByEmail(login);

		return user.getVerificationCode();
	}

	@Override
	public boolean checkIfCodesAreEqual(String login, String newCode) {
		User user = findByEmail(login);

		return user.getVerificationCode().equals(newCode);	
	}

	@Override
	public String sendVerificationMail(String login) {
		User newUser;
		User user = findByEmail(login);

		newUser = user;

		logger.info("Preparando para enviar a mensagem...");

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

		logger.info("A Mensagem foi enviada.");

		return newUser.getVerificationCode();
	}

	@Override
	public UserDTO refreshPassword(LoginDTO dto) {
		User newUser = findByEmail(dto.getLogin());
		if (dto.getPassword() != null) {
			newUser.setPassword(CriptexCustom.encrypt(dto.getPassword()));
			repository.save(newUser);
		}

		return parseToDTO(newUser);
	}

	@Override
	public Optional<User> findUserById(String id) {
		return repository.findById(id);
	}

	@Override
	public List<User> findAllByRoles(UserTypeEnum userType) {
		return repository.findAllByRoles(userType);
	}

	@Override
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

	@Override
	public User findByLogin(String login) {
		Optional<User> optUser = repository.findByLogin(login);
		if (optUser.isPresent()) {
			return optUser.get();
		} else {
			throw new NotFoundException(String.format("Usuario com login [%s] nao foi encontrado.", login));
		}
	}
	
	@Override
	public User findByLoginAndPassword(String login, String password) {
		Optional<User> optUser = repository.findByLoginAndPassword(login, password);
		if (optUser.isPresent()) {
			return optUser.get();
		} else {
			throw new NotFoundException("Usuario com senha e login informados nao foi encontrado.");
		}
	}

	@Override
	public User findByEmail(String email) {
		Optional<User> optUser = repository.findByEmail(email);
		if (optUser.isPresent()) {
			return optUser.get();
		} else {
			throw new NotFoundException(String.format("Usuario com email [%s] nao foi encontrado.", email));
		}
	}

	@Override
	public void setUserRooms(List<Room> newUserRooms, User user) {
		user.setRooms(newUserRooms);
		repository.save(user);
	}

	@Override
	public void setUserNotifications(List<Notification> newUserNotifications, User user) {
		user.setNotifications(newUserNotifications);
		repository.save(user);
	}

	@Override
	public List<RevisionDTO> getRevisions(String id) {
		AuditQuery auditQuery = auditReader.createQuery().forRevisionsOfEntityWithChanges(User.class, true)
				.add(AuditEntity.id().eq(id));

		List<RevisionDTO> details = new ArrayList<>();

		for (Object revision : auditQuery.getResultList()) {
			RevisionDTO r = new RevisionDTO();

			Object[] objArray = convertToObjectArray(revision);

			r.setEntity(objArray[0]);
			r.setRevisionDetails(objArray[1]);
			r.setRevisionType(objArray[2]);
			r.setUpdatedAttributes(objArray[3]);
			
			details.add(r);
		}

		return details;
	}
	
	public List<UserDTO> parseToDTO(List<User> list) {
		return list.stream().map(this::parseToDTO).toList();
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
}