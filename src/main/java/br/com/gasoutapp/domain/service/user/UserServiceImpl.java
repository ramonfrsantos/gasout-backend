package br.com.gasoutapp.domain.service.user;

import static br.com.gasoutapp.infrastructure.utils.JsonUtil.convertToObjectArray;
import static br.com.gasoutapp.infrastructure.utils.StringUtils.createRandomCode;
import static br.com.gasoutapp.infrastructure.utils.StringUtils.normalizeString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.application.dto.audit.RevisionDTO;
import br.com.gasoutapp.application.dto.user.LoginDTO;
import br.com.gasoutapp.application.dto.user.UserDTO;
import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.domain.exception.UserAlreadyRegisteredException;
import br.com.gasoutapp.infrastructure.config.security.EncryptorCustom;
import br.com.gasoutapp.application.dto.LoginResultDTO;
import br.com.gasoutapp.infrastructure.config.security.TokenService;
import br.com.gasoutapp.infrastructure.db.entity.enums.UserTypeEnum;
import br.com.gasoutapp.infrastructure.db.entity.notification.Notification;
import br.com.gasoutapp.infrastructure.db.entity.room.Room;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import br.com.gasoutapp.infrastructure.db.repository.UserRepository;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private EntityManagerFactory factory;

	@Value("${spring.mail.username}")
	private String companyEmail;

	@Value("${user.admin.email}")
	private String adminEmail;

    @Transactional
	public UserDTO register(UserDTO userDTO) {
		var newUser = create(userDTO);

		return parseToDTO(newUser);
	}

	@Override
	public User create(UserDTO userDTO) {
		var optUser = repository.findByEmail(userDTO.getEmail());

		if (optUser.isPresent()) {
			throw new UserAlreadyRegisteredException("Usuário com esse email já foi cadastrado.");
		}

		return repository.save(parseDTOToEntity(userDTO));
	}

	@Override
	public String delete(String login) {
		var user = findByLogin(login);
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
		var user = findByEmail(login);

		return user.getVerificationCode();
	}

	@Override
	public boolean checkIfCodesAreEqual(String login, String newCode) {
		var user = findByEmail(login);

		return user.getVerificationCode().equals(newCode);	
	}

	@Override
	public String sendVerificationMail(String login) {
		log.info("Preparando para enviar a mensagem...");

		User newUser = generateNewCodeForUser(login);

		mailSender.send(getEmailMessage(newUser));

		log.info("A mensagem foi enviada.");

		return newUser.getVerificationCode();
	}

	@Override
	public UserDTO refreshPassword(LoginDTO dto) {
		var newUser = findByEmail(dto.getLogin());

		if (dto.getPassword() != null) {
			newUser.setPassword(EncryptorCustom.encrypt(dto.getPassword()));
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
	public LoginResultDTO getDtoByUser(User user) {
		var dto = this.tokenService.createTokenForUser(user);

		dto.setUserId(user.getId());
		if (user.getName() != null && !user.getName().isEmpty()) {
			dto.setUserName(normalizeString(user.getName()));
		}

		user.setTokenFirebase(EncryptorCustom.encrypt(""));

		repository.save(user);

		return dto;
	}

	@Override
	public User findByLogin(String login) {
		var optUser = repository.findByEmail(login);

		if (optUser.isEmpty()) {
			throw new NotFoundException(String.format("Usuario com login [%s] nao foi encontrado.", login));
		}

		return optUser.get();
	}
	
	@Override
	public User findByLoginAndPassword(String login, String password) {
		var optUser = repository.findByLoginAndPassword(login, password);

		if (optUser.isEmpty()) {
			throw new NotFoundException("Usuario com senha e login informados nao foi encontrado.");
		}

		return optUser.get();

	}

	@Override
	public User findByEmail(String email) {
		var optUser = repository.findByEmail(email);

		if (optUser.isEmpty()) {
			throw new NotFoundException(String.format("Usuario com email [%s] nao foi encontrado.", email));
		}

		return optUser.get();
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
		var auditQuery = getAuditQuery(id);

		List<RevisionDTO> details = new ArrayList<>();

		for (Object revision : auditQuery.getResultList()) {
			var r = new RevisionDTO();
			var objArray = convertToObjectArray(revision);

			r.setEntity(objArray[0]);
			r.setRevisionDetails(objArray[1]);
			r.setRevisionType(objArray[2]);
			r.setUpdatedAttributes(objArray[3]);
			
			details.add(r);
		}

		return details;
	}

	private AuditQuery getAuditQuery(String id) {
		var auditReader = AuditReaderFactory.get(factory.createEntityManager());

		return auditReader.createQuery().forRevisionsOfEntityWithChanges(User.class, true)
				.add(AuditEntity.id().eq(id));
	}

	public List<UserDTO> parseToDTO(List<User> list) {
		return list.stream().map(this::parseToDTO).toList();
	}

	public UserDTO parseToDTO(User user) {
		return new UserDTO(user);
	}

	public User parseDTOToEntity(UserDTO userDTO) {
		var newUser = new User();
		newUser.setName(normalizeString(userDTO.getName()));
		newUser.setEmail(userDTO.getEmail());
		newUser.setLogin(userDTO.getEmail());
		newUser.setLastUpdate(new Date());

		var password = EncryptorCustom.encrypt(userDTO.getPassword());
		newUser.setPassword(password);

		if (userDTO.getEmail().equals(adminEmail)) {
			newUser.getRoles().add(UserTypeEnum.ADMIN);
		} else {
			newUser.getRoles().add(UserTypeEnum.CLIENTE);
		}

		return newUser;
	}

	private User generateNewCodeForUser(String login) {
		var verificationCode = createRandomCode(6, "0123456789");

		User newUser;
		newUser = findByEmail(login);
		newUser.setVerificationCode(verificationCode);
		return repository.save(newUser);
	}

	private SimpleMailMessage getEmailMessage(User newUser) {
		var fullName = newUser.getName();
		var firstName = fullName.split(" ", 0)[0];

		var message = new SimpleMailMessage();
		message.setFrom(companyEmail);
		message.setTo(newUser.getEmail());
		message.setText("Olá, " + firstName + "! Seu código de verificação para a alteração da senha é:\n\n"
				+ newUser.getVerificationCode()
				+ "\n\nAgora é só entrar no aplicativo GasOut e escolher uma nova senha.");
		message.setSubject("Alteração de senha no aplicativo GasOut");

		return message;
	}
}