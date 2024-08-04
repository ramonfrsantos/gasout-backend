package br.com.gasoutapp.application.dto.user;

import br.com.gasoutapp.infrastructure.db.entity.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class UserDTO {
	private String id;
	private String name;
	private String email;
	@JsonIgnore
	private String password;
	private String verificationCode;

	public UserDTO(String name, String email, String password) {
		this.name = name;
		this.email = email;
		this.password = password;
	}

	public UserDTO(User user) {
		this.id = user.getId();
		this.name = user.getName();
		this.email = user.getEmail();
		this.password = user.getPassword();
		this.verificationCode = user.getVerificationCode();
	}

	public UserDTO(String email) {
		this.email = email;
	}
}