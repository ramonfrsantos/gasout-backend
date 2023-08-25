package br.com.gasoutapp.config.security;

import static br.com.gasoutapp.utils.DateUtils.differenceInSeconds;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.exception.NotFoundException;
import br.com.gasoutapp.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class TokenService {

	final int HORAS_TIMEOUT = 1 * 4 * 30 * 24;

	@Autowired
	private UserRepository repository;

	public LoginResultDTO createTokenForUser(User user) {

		LoginResultDTO dto = new LoginResultDTO();
		dto.setUserId(user.getId());
		dto.setUserName(user.getName());
		dto.setLogin(user.getLogin());

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		calendar.add(Calendar.HOUR, HORAS_TIMEOUT);

		String token = Jwts.builder().claim("id", user.getId()).claim("roles", user.getRoles())
				.setSubject(user.getLogin()).setExpiration(calendar.getTime())
				.signWith(SignatureAlgorithm.HS512, SecurityFilter.SECRET).compact();
		dto.setToken(CriptexCustom.encrypt(token));

		String refreshToken = Jwts.builder().claim("id", user.getId()).setSubject(user.getLogin())
				.signWith(SignatureAlgorithm.HS512, SecurityFilter.SECRET).compact();
		dto.setRefreshToken(CriptexCustom.encrypt(refreshToken));

		dto.setTokenExpiresIn(calendar.getTime());
		dto.setTokenType("Bearer");

		return dto;
	}

	public LoginResultDTO refreshToken(String refreshToken) {
		refreshToken = refreshToken.replace("Bearer ", "");
		refreshToken = CriptexCustom.decrypt(refreshToken);
		Claims claim = Jwts.parser().setSigningKey(SecurityFilter.SECRET).parseClaimsJws(refreshToken).getBody();
		Optional<User> usuario = repository.findById(claim.get("id", String.class));
		if (usuario.isPresent()) {
			return createTokenForUser(usuario.get());
		}

		throw new NotFoundException("Usuario não encontrado.");
	}

	public UserJWT getUserJWTFromToken(String token) {
		token = token.replace("Bearer ", "");
		token = CriptexCustom.decrypt(token);
		Claims claim = Jwts.parser().setSigningKey(SecurityFilter.SECRET).parseClaimsJws(token).getBody();

		Long expiresIn = differenceInSeconds(new Date(), claim.getExpiration());

		return new UserJWT(claim.get("id", String.class), claim.getSubject(), expiresIn, isValidToken(claim));
	}

	public boolean isValidToken(Claims claim) {
		Optional<User> user = repository.findByLogin(claim.getSubject());

		if (user.isPresent()) {
			return true;
		}

		return false;
	}
}
