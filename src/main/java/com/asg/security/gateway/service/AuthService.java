package com.asg.security.gateway.service;

import com.asg.security.gateway.aad.AzureADClient;
import com.asg.security.gateway.dto.MenuItemDto;
import com.asg.security.gateway.dto.TimeZoneDto;
import com.asg.security.gateway.entity.*;
import com.asg.security.gateway.exception.AsgException;
import com.asg.security.gateway.exception.AzureAuthenticationException;
import com.asg.security.gateway.exception.EmailNotFoundException;
import com.asg.security.gateway.exception.ResourceNotFoundException;
import com.asg.security.gateway.model.AuthenticationRequest;
import com.asg.security.gateway.model.AuthenticationResponse;
import com.asg.security.gateway.model.AzureADResponse;
import com.asg.security.gateway.model.LoginRequest;
import com.asg.security.gateway.repository.*;
import com.asg.security.gateway.util.JwtUtils;
import com.asg.security.gateway.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static com.asg.security.gateway.util.ApiResponse.*;
import static com.asg.security.gateway.util.Base64Util.decode;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final RoleService roleService;
    private final AzureADClient azureADClient;
    private final CacheService cacheService;
    private final DraftRepository draftRepository;
    private final UsersCompanyRepository usersCompanyRepository;
    private final CompanyRepository companyRepository;
    private final StateRepository stateRepository;
    private final TimeZoneRepository timeZoneRepository;
    private final MenuRepository menuRepository;

    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public AuthenticationResponse login(LoginRequest request) {
        String sanitizedUserId = request.getUserId().trim();
        String password = decode(request.getPassword().trim());

        User user = userRepository.findByUserIdIgnoreCaseAndActive(sanitizedUserId, "Y")
                .orElseThrow(() -> new IllegalArgumentException("Incorrect credentials. Please check your userId and password and try again"));

        if ("Y".equalsIgnoreCase(user.getUserLocked())) {
            String lockReason = user.getUserLockedReason();
            String errorMessage = "User account is locked";
            if (StringUtils.isNotBlank(lockReason)) {
                errorMessage += ": " + lockReason;
            }
            throw new IllegalStateException(errorMessage);
        }

        String hashPassword = getSecureString(password, "salt");
        if (!hashPassword.equals(user.getPwd())) {
            throw new IllegalArgumentException("Incorrect credentials. Please check your userId and password and try again");
        }

        return generateAuthenticationResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse ssoLogin(AuthenticationRequest authenticationRequest) {
        try {
            AzureADResponse azureResponse = azureADClient.validate(authenticationRequest.getUsername(),
                    authenticationRequest.getAzureAccessToken());
            if (azureResponse != null && HttpStatus.OK.value() == azureResponse.getStatusCode()) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(), null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                throw new AzureAuthenticationException("No Response from Azure");
            }
        } catch (EmailNotFoundException e) {
            throw e;
        } catch (DisabledException e) {
            throw new DisabledException(e.getMessage(), e);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(e.getMessage(), e);
        } catch (AzureAuthenticationException e) {
            throw e;
        }

        User user = userRepository.findByEmailIgnoreCaseAndActive(authenticationRequest.getUsername(), "Y")
                .orElseThrow(() -> new EmailNotFoundException("Active user not found with email " + authenticationRequest.getUsername()));

        if ("Y".equalsIgnoreCase(user.getUserLocked())) {
            String lockReason = user.getUserLockedReason();
            String errorMessage = "User account is locked";
            if (StringUtils.isNotBlank(lockReason)) {
                errorMessage += ": " + lockReason;
            }
            throw new IllegalStateException(errorMessage);
        }

        return generateAuthenticationResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthenticationResponse refreshToken(String userId) {
        User user = userRepository.findByUserIdIgnoreCaseAndActive(userId, "Y")
                .orElseThrow(() -> new IllegalArgumentException("Active user not found with userId " + userId));
        return generateAuthenticationResponse(user);
    }

    private AuthenticationResponse generateAuthenticationResponse(User user) {
        try {


        List<String> roleNames = roleService.getUserRoleNames(user.getUserPoid());
        String token = jwtUtils.generateToken(user, roleNames);
        String refreshToken = jwtUtils.generateRefreshToken(user.getUserId());

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("userName", user.getUserName());
        userDetails.put("userId", user.getUserId());
        userDetails.put("userPoid", user.getUserPoid());
        userDetails.put("userEmail", user.getEmail());
        userDetails.put("groupPoid", user.getGroupPoid());
        userDetails.put("joinedDate", user.getCreatedDate());
        userDetails.put("resetPasswordNextLogin", user.getResetPasswordNextLogin());
        userDetails.put("defaultCompanyPoid", user.getDefaultCompanyPoid());
        userDetails.put("roles", roleNames);

        AuthenticationResponse response = new AuthenticationResponse();
        response.setAuthToken(token);
        response.setRefreshToken(refreshToken);
        response.setUserDetails(userDetails);
        response.setTokenExpiry(jwtUtils.getExpirationDate(token));
        return response;}catch (Exception e){e.printStackTrace();
        throw new RuntimeException(e)
      ;  }
    }

    public static String getSecureString(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }

    public ResponseEntity<?> createNewUserPassword(Long userPoid) {
        try {
            if (userPoid == null) {
                return badRequest("userPoid is required");
            }

            User user = userRepository.findById(userPoid)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "userPoid", userPoid));

            String email = user.getEmail();
            if (StringUtils.isBlank(email) || email.trim().equals(".")) {
                return badRequest("User's email must not be empty.");
            }

            if (!"Y".equalsIgnoreCase(user.getActive())) {
                return badRequest("User is not active. Cannot create password.");
            }

            String newPassword = generateRandomPassword(10);
            String hashedPassword = getSecureString(newPassword, "salt");

            String procedure = "{ ? = call FUNC_USER_PSWD_CREATE(?, ?, ?) }";

            String result = jdbcTemplate.execute(procedure, (CallableStatementCallback<String>) cs -> {
                cs.registerOutParameter(1, java.sql.Types.VARCHAR);
                cs.setLong(2, userPoid);
                cs.setString(3, newPassword);
                cs.setString(4, hashedPassword);
                cs.execute();
                return cs.getString(1);
            });

            if (StringUtils.isNotBlank(result) && result.toUpperCase().startsWith("TRUE")) {
                log.info("Password created for userPoid: {} and email sent to: {}", userPoid, email);
                return success("Password created and email sent.", result);
            } else {
                log.warn("Password creation failed for userPoid: {} | DB function returned: {}", userPoid, result);
                return internalServerError(result);
            }

        } catch (Exception e) {
            log.error("Exception while creating password for userPoid {}: {}", userPoid, e.getMessage(), e);
            return internalServerError(e.getMessage());
        }
    }


    public String changeUserPassword(String userId, String oldPassword, String newPassword) {
        try {
            // Validate user is active and not deleted
            User user = validateActiveUser(userId);

            // Decode passwords from base64
            String decodedOldPassword = decode(oldPassword.trim());
            String decodedNewPassword = decode(newPassword.trim());

            // Validate decoded new password size
            if (decodedNewPassword.length() < 8 || decodedNewPassword.length() > 24) {
                throw new AsgException("New password must be between 8 and 24 characters", 400);
            }

            // Validate decoded new password pattern
            if (!decodedNewPassword.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$*_~])[^&:;%',?+\"]{8,24}$")) {
                throw new AsgException("New password must contain at least one lowercase letter, one uppercase letter, one number, one special character (!@#$*_~) and should not contain &:;%',?+\" characters", 400);
            }

            // Verify old password
            String hashedOldPassword = getSecureString(decodedOldPassword, "salt");
            if (!user.getPwd().equals(hashedOldPassword)) {
                throw new AsgException("Old password is incorrect", 400);
            }

            String hashedNewPassword = getSecureString(decodedNewPassword, "salt");
            String function = "{ ? = call FUNC_USER_PSWD_CHANGE(?, ?, ?, ?, ?, ?, ?) }";

            return jdbcTemplate.execute(function, (CallableStatementCallback<String>) cs -> {
                cs.registerOutParameter(1, java.sql.Types.VARCHAR);
                cs.setString(2, user.getUserId());
                cs.setString(3, hashedNewPassword);
                cs.setString(4, "FALSE"); // P_SECURITY_CHANGE = FALSE for first-time password change
                cs.setString(5, null); // P_SECURITY_QUESTION1
                cs.setString(6, null); // P_SECURITY_QUESTION2
                cs.setString(7, null); // P_SECURITY_ANSWER1
                cs.setString(8, null); // P_SECURITY_ANSWER2
                cs.execute();
                return cs.getString(1);
            });
        } catch (AsgException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception while changing password for userId {}: {}", userId, e.getMessage(), e);
            throw new AsgException("Exception while changing password: " + e.getMessage(), 500);
        }
    }

    public String sendOtp(String userId) {
        try {
            User user = validateActiveUser(userId);
            String otp = generateRandomOtp();
            // Store OTP in cache with 10 minutes expiry
            cacheService.put("otpCache", userId, otp, 10);
            String function = "{ ? = call FUNC_USER_PSWD_OTP(?, ?) }";
            return jdbcTemplate.execute(function, (CallableStatementCallback<String>) cs -> {
                cs.registerOutParameter(1, java.sql.Types.VARCHAR);
                cs.setLong(2, user.getUserPoid());
                cs.setString(3, otp);
                cs.execute();
                return cs.getString(1);
            });
        } catch (AsgException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception while sending OTP for userId {}: {}", userId, e.getMessage(), e);
            throw new AsgException("Exception while sending OTP: " + e.getMessage(), 500);
        }
    }

    public String forgotPassword(String userId, String otp) {
        try {
            User user = validateActiveUser(userId);
            String cachedOtp = cacheService.get("otpCache", userId, String.class);
            if (cachedOtp == null) {
                throw new AsgException("Invalid OTP. Please check and try again", 400);
            }
            if (!cachedOtp.equals(otp)) {
                throw new AsgException("Invalid OTP. Please check and try again", 400);
            }
            cacheService.evict("otpCache", userId);
            return resetUserPassword(userId);
        } catch (AsgException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception while processing forgot password for userId {}: {}", userId, e.getMessage(), e);
            throw new AsgException("Exception while processing forgot password: " + e.getMessage(), 500);
        }
    }

    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            password.append(chars.charAt(randomIndex));
        }
        return password.toString();
    }

    private User validateActiveUser(String userId) {
        User user = userRepository.findByActiveUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        if (!"Y".equalsIgnoreCase(user.getActive())) {
            throw new AsgException("User is not active", 400);
        }

        if ("Y".equalsIgnoreCase(user.getDeleted())) {
            throw new AsgException("User is deleted", 400);
        }

        String email = user.getEmail();
        ValidationUtil.validateEmailRequiredField(email);

        return user;
    }

    private String generateRandomOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    public String resetUserPassword(String userId) {
        try {
            User user = validateActiveUser(userId);

            String newPassword = generateRandomPassword(10);
            String hashedPassword = getSecureString(newPassword, "salt");

            String function = "{ ? = call FUNC_USER_PSWD_RESET(?, ?, ?) }";

            return jdbcTemplate.execute(function, (CallableStatementCallback<String>) cs -> {
                cs.registerOutParameter(1, java.sql.Types.VARCHAR);
                cs.setLong(2, user.getUserPoid());
                cs.setString(3, newPassword);
                cs.setString(4, hashedPassword);
                cs.execute();
                return cs.getString(1);
            });
        } catch (AsgException e) {
            throw e;
        } catch (Exception e) {
            log.error("Exception while resetting password for userId {}: {}", userId, e.getMessage(), e);
            throw new AsgException("Exception while resetting password: " + e.getMessage(), 500);
        }
    }

    @Transactional
    public int clearAllDraftsByUser(Long userPoid) {
        long count = draftRepository.deleteByUserPoid(userPoid);
        log.debug("Cleared {} drafts for userPoid={}", count, userPoid);
        return (int) count;
    }

    @Transactional
    public List<Company> getCompanies(Long userPoid) {
        try {
            List<UsersCompanyEntity> companyList = usersCompanyRepository.findCompanyAccess(userPoid);
            List<Company> companies = new ArrayList<>();

            for (UsersCompanyEntity usersCompanyEntity : companyList) {
                Long companyPoid = usersCompanyEntity.getId().getCompanyPoid();
                Company company = companyRepository.findByCompanyPoid(companyPoid);

                company.setLabel(company.getCompanyName()); // for dropdown UI
                company.setValue(company.getCompanyPoid()); // for dropdown UI

                if (company.getDateFormat() != null) {
                    company.setDateFormat(company.getDateFormat());
                }

                if (company.getCountryId() != null) {
                    String countryCode = getCountryCodeForCompany(company.getCompanyPoid());
                    company.setCountryCode(countryCode);

                    if (company.getStateId() != null) {
                        State state = getStateForCompany(company.getCountryId(), company.getStateId());
                        company.setStateName(state != null ? state.getStateName() : null);
                    }
                }

                if (company.getTimezoneId() != null) {
                    TimeZoneEntity timeZoneEntity = timeZoneRepository.findByTimezoneId(company.getTimezoneId());
                    if (timeZoneEntity != null) {
                        company.setTimeZone(new TimeZoneDto(timeZoneEntity.getTimezoneId(), timeZoneEntity.getTimezoneCode(), timeZoneEntity.getTimezoneName()));
                    }
                }

                companies.add(company);
            }

            return companies;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getCountryCodeForCompany(Long companyPoid) {
        String countryPoid = companyRepository.findCountryIdByCompanyPoid(companyPoid);
        if (countryPoid != null) {

            return companyRepository.findCountryCodeByCountryPoid(Long.valueOf(countryPoid));
        }
        return null;
    }

    public State getStateForCompany(String countryPoid, String statePoid) {
        if (countryPoid != null && statePoid != null) {
            Long countryId = Long.valueOf(countryPoid);
            Long stateId = Long.valueOf(statePoid);
            if (stateRepository.existsByCountryPoidAndStatePoid(countryId, stateId)) {
                return stateRepository.findByCountryPoidAndStatePoid(countryId, stateId);
            }
        }
        return null;
    }

    public List<MenuItemDto> getUserMenu(Long userPoid) {
        List<Object[]> results = menuRepository.findMenuItemsByUserPoid(userPoid);

        List<MenuItemDto> menuItems = results.stream().map(row -> new MenuItemDto(
                (String) row[0], // MENU_ID
                (String) row[1], // MENU_NAME
                ((Number) row[2]).intValue(), // MENU_LEVEL
                (String) row[3], // MENU_GROUP
                (String) row[4], // TASKFLOW_URL
                (String) row[5], // USER_ID
                (String) row[6], // DOC_TYPE
                (String) row[7], // MODULE_ID
                row[8] != null ? row[8].toString() : null, // HIDE_IN_MAIN_MENU
                new ArrayList<>()
        )).collect(Collectors.toList());

        return buildMenuHierarchy(menuItems);
    }

    private List<MenuItemDto> buildMenuHierarchy(List<MenuItemDto> menuItems) {

        // Step 1: Group by level
        Map<Integer, List<MenuItemDto>> levels = menuItems.stream()
                .collect(Collectors.groupingBy(MenuItemDto::getMenuLevel));

        List<MenuItemDto> level0 = levels.getOrDefault(0, List.of());
        List<MenuItemDto> level1 = levels.getOrDefault(1, List.of());
        List<MenuItemDto> level2 = levels.getOrDefault(2, List.of());

        // Step 2: Map level 1 by menuGroup
        Map<String, List<MenuItemDto>> level1Map = level1.stream()
                .collect(Collectors.groupingBy(MenuItemDto::getMenuGroup));

        Map<String, List<MenuItemDto>> level2Map = level2.stream()
                .collect(Collectors.groupingBy(MenuItemDto::getMenuGroup));

        // Step 3: Attach level 2 to level 1
        for (MenuItemDto l1 : level1) {
            List<MenuItemDto> children = level2Map.getOrDefault(l1.getMenuId(), List.of());
            l1.getChildren().addAll(children);
        }

        // Step 4: Attach level 1 to level 0
        for (MenuItemDto l0 : level0) {
            List<MenuItemDto> children = level1Map.getOrDefault(l0.getMenuId(), List.of());
            l0.getChildren().addAll(children);
        }

        return level0;
    }
}

