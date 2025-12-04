package bimobile.model;

/**
 * Customer.java
 *
 * @author Ben Berlin
 */
import jakarta.persistence.*;

@Entity
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Anrede, z.B. "Herr", "Frau", "Divers".
	 */
	@Column(length = 20)
	private String salutation;

	@Column(nullable = false, length = 100)
	private String firstName;

	@Column(nullable = false, length = 100)
	private String lastName;

	@Column(nullable = false, unique = true, length = 200)
	private String email;

	@Column(length = 50)
	private String phone;

	// --- Konstruktoren ---

	public Customer() {
	}

	public Customer(String salutation, String firstName, String lastName, String email, String phone) {
		this.salutation = salutation;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phone = phone;
	}

	// --- Getter & Setter ---

	public Long getId() {
		return id;
	}

	public String getSalutation() {
		return salutation;
	}

	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * Vollständiger Name für UI-Anzeige (z.B. ComboBox/ Grid).
	 */
	public String getFullName() {
		String fn = firstName != null ? firstName : "";
		String ln = lastName  != null ? lastName  : "";
		return (fn + " " + ln).trim();
	}
}
