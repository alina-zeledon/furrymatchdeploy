package furrymatch.web.rest;

import furrymatch.domain.PersistentToken;
import furrymatch.domain.User;
import furrymatch.repository.PersistentTokenRepository;
import furrymatch.repository.UserRepository;
import furrymatch.security.AuthoritiesConstants;
import furrymatch.security.SecurityUtils;
import furrymatch.service.MailService;
import furrymatch.service.UserService;
import furrymatch.service.dto.AdminUserDTO;
import furrymatch.service.dto.PasswordChangeDTO;
import furrymatch.web.rest.errors.*;
import furrymatch.web.rest.vm.KeyAndPasswordVM;
import furrymatch.web.rest.vm.ManagedUserVM;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;

    private final PersistentTokenRepository persistentTokenRepository;
    private String applicationName;

    public AccountResource(
        UserRepository userRepository,
        UserService userService,
        MailService mailService,
        PersistentTokenRepository persistentTokenRepository
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
        this.persistentTokenRepository = persistentTokenRepository;
    }

    /**
     * {@code POST  /register} : register the user.
     *
     * @param managedUserVM the managed user View Model.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already used.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        if (isPasswordLengthInvalid(managedUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        User user = userService.registerUser(
            managedUserVM,
            managedUserVM.getPassword(),
            managedUserVM.getFirstName(),
            managedUserVM.getSecondName(),
            managedUserVM.getFirstLastName(),
            managedUserVM.getSecondLastName(),
            managedUserVM.getPhoneNumber(),
            managedUserVM.getPhoto(),
            managedUserVM.getIdentityNumber(),
            managedUserVM.getAddress(),
            managedUserVM.getProvince(),
            managedUserVM.getCanton(),
            managedUserVM.getDistrict()
        );
        //mailService.sendActivationEmail(user);
    }

    /**
     * {@code GET  /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */
    @GetMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this activation key");
        }
    }

    /**
     * {@code GET  /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @GetMapping("/account")
    public AdminUserDTO getAccount() {
        return userService
            .getUserWithAuthorities()
            .map(AdminUserDTO::new)
            .orElseThrow(() -> new AccountResourceException("User could not be found"));
    }

    /**
     * {@code POST  /account} : update the current user information.
     *
     * @param managedUserVM the current user information.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user login wasn't found.
     */
    @PostMapping("/account")
    public void saveAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        String userLogin = SecurityUtils
            .getCurrentUserLogin()
            .orElseThrow(() -> new AccountResourceException("Current user login not found"));
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
            throw new EmailAlreadyUsedException();
        }
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new AccountResourceException("User could not be found");
        }
        userService.updateUser(
            managedUserVM.getFirstName(),
            managedUserVM.getLastName(),
            managedUserVM.getEmail(),
            managedUserVM.getLangKey(),
            managedUserVM.getImageUrl(),
            managedUserVM.getPassword(),
            managedUserVM.getSecondName(),
            managedUserVM.getFirstLastName(),
            managedUserVM.getSecondLastName(),
            managedUserVM.getPhoneNumber(),
            managedUserVM.getPhoto(),
            managedUserVM.getIdentityNumber(),
            managedUserVM.getAddress(),
            managedUserVM.getProvince(),
            managedUserVM.getCanton(),
            managedUserVM.getDistrict()
        );
    }

    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (isPasswordLengthInvalid(passwordChangeDto.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code GET  /account/sessions} : get the current open sessions.
     *
     * @return the current open sessions.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the current open sessions couldn't be retrieved.
     */
    @GetMapping("/account/sessions")
    public List<PersistentToken> getCurrentSessions() {
        return persistentTokenRepository.findByUser(
            userRepository
                .findOneByLogin(
                    SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccountResourceException("Current user login not found"))
                )
                .orElseThrow(() -> new AccountResourceException("User could not be found"))
        );
    }

    /**
     * {@code DELETE  /account/sessions?series={series}} : invalidate an existing session.
     *
     * - You can only delete your own sessions, not any other user's session
     * - If you delete one of your existing sessions, and that you are currently logged in on that session, you will
     *   still be able to use that session, until you quit your browser: it does not work in real time (there is
     *   no API for that), it only removes the "remember me" cookie
     * - This is also true if you invalidate your current session: you will still be able to use it until you close
     *   your browser or that the session times out. But automatic login (the "remember me" cookie) will not work
     *   anymore.
     *   There is an API to invalidate the current session, but there is no API to check which session uses which
     *   cookie.
     *
     * @param series the series of an existing session.
     * @throws IllegalArgumentException if the series couldn't be URL decoded.
     */
    @DeleteMapping("/account/sessions/{series}")
    public void invalidateSession(@PathVariable String series) {
        String decodedSeries = URLDecoder.decode(series, StandardCharsets.UTF_8);
        SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .ifPresent(u ->
                persistentTokenRepository
                    .findByUser(u)
                    .stream()
                    .filter(persistentToken -> StringUtils.equals(persistentToken.getSeries(), decodedSeries))
                    .findAny()
                    .ifPresent(t -> persistentTokenRepository.deleteById(decodedSeries))
            );
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     */
    @PostMapping(path = "/account/reset-password/init")
    public ResponseEntity<PasswordResetResponse> requestPasswordReset(@RequestBody String mail) {
        Optional<User> user = userService.requestPasswordReset(mail);
        if (user.isPresent()) {
            mailService.sendPasswordResetMail(user.get());
            PasswordResetResponse response = new PasswordResetResponse("Success");
            return ResponseEntity.ok(response);
        } else {
            log.warn("Password reset requested for non existing mail");
            PasswordResetResponse response = new PasswordResetResponse("Error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    class PasswordResetResponse {

        private String status;

        public PasswordResetResponse(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        Optional<User> user = userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            throw new AccountResourceException("No user was found for this reset key");
        }
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (
            StringUtils.isEmpty(password) ||
            password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
            password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH
        );
    }

    @PostMapping("/account/selectedPet/{petId}")
    public ResponseEntity<Void> updateUserPetId(@PathVariable(value = "petId", required = false) final Long petId) {
        Long id = userService.getUserWithAuthorities().get().getId();
        userService.updateUserSelectedPet(petId, id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, "user", id.toString()))
            .build();
    }

    @PostMapping("/account/saveMatchPet/{matchPetId}")
    public ResponseEntity<Void> updateMatchPet(@PathVariable(value = "matchPetId", required = false) final String matchPetId) {
        Long id = userService.getUserWithAuthorities().get().getId();
        userService.updateUserMatchPet(matchPetId, id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, "user", id.toString()))
            .build();
    }

    @PostMapping("/account/saveMatch/{matchId}")
    public ResponseEntity<Void> updateUserMatch(@PathVariable(value = "matchId", required = false) final String matchId) {
        Long id = userService.getUserWithAuthorities().get().getId();
        userService.updateUserMatch(matchId, id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, "user", id.toString()))
            .build();
    }
}
