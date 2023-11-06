package br.com.gasoutapp.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.gasoutapp.domain.entity.enums.UserTypeEnum;
import br.com.gasoutapp.domain.entity.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

	public Optional<User> findByLoginAndPassword(String login, String password);

	public Optional<User> findByLogin(String login);

	public Optional<User> findByPassword(String password);

	public Optional<User> findByEmail(String email);

	public List<User> findAllByEmail(String login);

	public List<User> findAllByRoles(UserTypeEnum role);
}