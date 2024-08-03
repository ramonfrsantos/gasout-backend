package br.com.gasoutapp.application.dto.user;

import br.com.gasoutapp.infrastructure.db.entity.user.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor public class UserRegisterDTO {
	@Size(min = 2, message = "O nome deve conter no minimo 2 caracteres.")
	private String name;
	@Email(regexp = ".+[@].+[\\.].+")
	private String email;
	private String password;
}