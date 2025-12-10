package bimobile.security;

import bimobile.dao.UserRepository;
import bimobile.model.Facility;
import bimobile.model.User;
import bimobile.model.RoleType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Author: Lasse
 * Description: Role and facility checks.
 */
@Component
public class AuthorizationUtils {

	private static UserRepository userRepository;

	public AuthorizationUtils(UserRepository repo) {
		userRepository = repo;
	}

	public static String getCurrentUsername() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) return null;

		Object principal = auth.getPrincipal();
		if (principal instanceof UserDetails u) {
			return u.getUsername();
		}
		return principal.toString();
	}

	public static RoleType getCurrentRoleType() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) return null;

		for (GrantedAuthority a : auth.getAuthorities()) {
			String role = a.getAuthority().replace("ROLE_", "");
			try {
				return RoleType.valueOf(role);
			} catch (Exception ignored) {}
		}
		return null;
	}

	// MANAGEMENT
	public static boolean isManagement() {
		return getCurrentRoleType() == RoleType.MANAGEMENT;
	}

	// BRANCH_MANAGER
	public static boolean isBranchManager() {
		return getCurrentRoleType() == RoleType.BRANCH_MANAGER;
	}

	// EMPLOYEE
	public static boolean isEmployee() {
		return getCurrentRoleType() == RoleType.EMPLOYEE;
	}

	// Current User loaded from DB
	public static Optional<User> getCurrentUser() {
		if (userRepository == null) return Optional.empty();
		String email = getCurrentUsername();
		if (email == null) return Optional.empty();
		return userRepository.findByEmail(email);
	}

	public static Facility getCurrentUserFacility() {
		return getCurrentUser()
				.map(User::getFacility)
				.orElse(null);
	}

	public static boolean isSameFacility(Facility facility) {
		Facility current = getCurrentUserFacility();
		return current != null && facility != null &&
				current.getId().equals(facility.getId());
	}
}