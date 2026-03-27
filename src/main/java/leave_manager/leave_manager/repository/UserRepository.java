package leave_manager.leave_manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import leave_manager.leave_manager.enums.Role;
import leave_manager.leave_manager.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    // Find all employees reporting to a specific manager
    List<User> findByManagerId(Long managerId);

    // Find all active users
    List<User> findByIsActiveTrue();

    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId AND u.isActive = true")
    List<User> findActiveTeamMembersByManagerId(Long managerId);
}