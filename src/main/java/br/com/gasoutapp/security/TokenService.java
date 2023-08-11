package br.com.gasoutapp.security;

import static br.com.gasoutapp.utils.DateUtils.differenceInSeconds;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.gasoutapp.domain.User;
import br.com.gasoutapp.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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

		String token = Jwts.builder().claim("id", user.getId()).claim("login", user.getLogin())
				.setExpiration(calendar.getTime()).signWith(SignatureAlgorithm.HS512, SecurityFilter.SECRET).compact();

		dto.setToken(CriptexCustom.encrypt(token));

		String refreshToken = Jwts.builder().claim("id", user.getId()).setSubject(user.getLogin())
				.signWith(SignatureAlgorithm.HS512, SecurityFilter.SECRET).compact();

		dto.setRefreshToken(CriptexCustom.encrypt(refreshToken));

		return dto;
	}

	public LoginResultDTO refreshToken(String refreshToken) {
		refreshToken = refreshToken.replace("Bearer ", "");
		refreshToken = CriptexCustom.decrypt(refreshToken);

		Claims claim = Jwts.parser().setSigningKey(SecurityFilter.SECRET).parseClaimsJws(refreshToken).getBody();

		User usuario = repository.getById(claim.get("id", String.class));
		LoginResultDTO dto = createTokenForUser(usuario);

		return dto;
	}

	public UserJWT getUserJWTFromToken(String token) {
		token = token.replace("Bearer ", "");
		token = CriptexCustom.decrypt(token);
		Claims claim = Jwts.parser().setSigningKey(SecurityFilter.SECRET).parseClaimsJws(token).getBody();
		return new UserJWT(claim.get("id", String.class), claim.get("login", String.class),
				differenceInSeconds(new Date(), claim.getExpiration()));
	}

	public boolean isValidToken(String token) {
		try {
			token = CriptexCustom.decrypt(token);
			Jwts.parser().setSigningKey(SecurityFilter.SECRET).parseClaimsJws(token);
			return true;
		} catch (ExpiredJwtException e) {
			return false;
		}
	}
}
