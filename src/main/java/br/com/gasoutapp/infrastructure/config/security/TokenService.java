package br.com.gasoutapp.infrastructure.config.security;

import static br.com.gasoutapp.infrastructure.utils.DateUtils.differenceInSeconds;

import java.util.Calendar;
import java.util.Date;

import br.com.gasoutapp.application.dto.LoginResultDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.domain.exception.NotFoundException;
import br.com.gasoutapp.infrastructure.db.entity.user.User;
import br.com.gasoutapp.infrastructure.db.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenService {
	private static final int YEAR_COUNT = 1;
	private static final int MONTH_COUNT = 4;
	private static final int DAY_COUNT = 30;
	private static final int HOUR_COUNT = 24;

	private static final  int HORAS_TIMEOUT = YEAR_COUNT * MONTH_COUNT * DAY_COUNT * HOUR_COUNT;

	@Autowired
	private UserRepository repository;

    public LoginResultDTO createTokenForUser(User user) {

		var dto = new LoginResultDTO();
		dto.setUserId(user.getId());
		dto.setUserName(user.getName());
		dto.setLogin(user.getLogin());

		var calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		calendar.add(Calendar.HOUR, HORAS_TIMEOUT);

		var token = Jwts.builder().claim("id", user.getId()).claim("roles", user.getRoles())
				.setSubject(user.getLogin()).setExpiration(calendar.getTime())
				.signWith(SignatureAlgorithm.HS512, SecurityFilter.getSecretKey()).compact();
		dto.setToken(EncryptorCustom.encrypt(token));

		var refreshToken = Jwts.builder().claim("id", user.getId()).setSubject(user.getLogin())
				.signWith(SignatureAlgorithm.HS512, SecurityFilter.getSecretKey()).compact();
		dto.setRefreshToken(EncryptorCustom.encrypt(refreshToken));

		dto.setTokenExpiresIn(calendar.getTime());
		dto.setTokenType("Bearer");

		return dto;
	}

	public LoginResultDTO refreshToken(String refreshToken) {
		refreshToken = refreshToken.replace("Bearer ", "");
		refreshToken = EncryptorCustom.decrypt(refreshToken);
		var claim = Jwts.parser().setSigningKey(SecurityFilter.getSecretKey()).parseClaimsJws(refreshToken).getBody();
		var usuario = repository.findById(claim.get("id", String.class));

		if (usuario.isPresent()) {
			return createTokenForUser(usuario.get());
		}

		throw new NotFoundException("Usuario não encontrado.");
	}

	public UserJWT getUserJWTFromToken(String token) {
		token = token.replace("Bearer ", "");
		token = EncryptorCustom.decrypt(token);

		var claim = Jwts.parser().setSigningKey(SecurityFilter.getSecretKey()).parseClaimsJws(token).getBody();
		var expiresIn = differenceInSeconds(new Date(), claim.getExpiration());

		return new UserJWT(claim.get("id", String.class), claim.getSubject(), expiresIn, isValidToken(claim));
	}

	public boolean isValidToken(Claims claim) {
		var user = repository.findByEmail(claim.getSubject());

		return user.isPresent();
	}

	public UserJWT getUserByToken(String token) {
		return this.getUserJWTFromToken(token);
	}
}