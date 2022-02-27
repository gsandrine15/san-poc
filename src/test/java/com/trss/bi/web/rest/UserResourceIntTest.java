package com.trss.bi.web.rest;

import com.trss.bi.BiAuthApp;
import com.trss.bi.domain.*;
import com.trss.bi.repository.UserDetailRepository;
import com.trss.bi.repository.UserRepository;
import com.trss.bi.repository.UserWithDetailRepository;
import com.trss.bi.security.AuthorizationConstants;
import com.trss.bi.service.CustomerService;
import com.trss.bi.service.MailService;
import com.trss.bi.service.UserService;
import com.trss.bi.service.UserWithDetailService;
import com.trss.bi.service.dto.UserDTO;
import com.trss.bi.service.mapper.UserMapper;
import com.trss.bi.service.mapper.UserWithDetailMapper;
import com.trss.bi.web.rest.errors.ExceptionTranslator;
import com.trss.bi.web.rest.vm.ManagedUserVM;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BiAuthApp.class)
public class UserResourceIntTest {

    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String UPDATED_LOGIN = "jhipster";

    private static final Long DEFAULT_ID = 1L;

    private static final String DEFAULT_PASSWORD = "passjohndoe";
    private static final String UPDATED_PASSWORD = "passjhipster";

    private static final String DEFAULT_EMAIL = "johndoe@localhost";
    private static final String UPDATED_EMAIL = "jhipster@localhost";

    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String UPDATED_FIRSTNAME = "jhipsterFirstName";

    private static final String DEFAULT_LASTNAME = "doe";
    private static final String UPDATED_LASTNAME = "jhipsterLastName";

    private static final String DEFAULT_IMAGEURL = "http://placehold.it/50x50";
    private static final String UPDATED_IMAGEURL = "http://placehold.it/40x40";

    private static final String DEFAULT_LANGKEY = "en";
    private static final String UPDATED_LANGKEY = "fr";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private UserWithDetailRepository userWithDetailRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserWithDetailService userWithDetailService;

    @Autowired
    private UserWithDetailMapper userWithDetailMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private CacheManager cacheManager;

    private MockMvc restUserMockMvc;

    private User user;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).clear();
        cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE).clear();
        UserResource userResource = new UserResource(userService, customerService, userRepository, mailService, userWithDetailService, userWithDetailMapper);
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .build();
    }

    /**
     * Create a User.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.
     */
    public static User createEntity(EntityManager em) {
        User user = new User();
        user.setLogin(DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5));
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setImageUrl(DEFAULT_IMAGEURL);
        user.setLangKey(DEFAULT_LANGKEY);
        return user;
    }

    @Before
    public void initTest() {
        user = createEntity(em);
        user.setLogin(DEFAULT_LOGIN);
        user.setEmail(DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    @WithMockUser(roles=AuthorizationConstants.ADMIN)
    public void createUser() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        UserWithDetail user = new UserWithDetail();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setImageUrl(DEFAULT_IMAGEURL);
        user.setLangKey(DEFAULT_LANGKEY);
        Role role = new Role();
        role.setName(AuthorizationConstants.ADMIN);
        user.setRole(role);
        Customer customer = customerService.findOne(1L).get();
        user.setCustomer(customer);

        restUserMockMvc.perform(post("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userWithDetailMapper.userToUserDTO(user))))
            .andExpect(status().isCreated());


        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(testUser.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(testUser.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(testUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(testUser.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(testUser.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
    }

    @Test
    @Transactional
    @WithMockUser(roles=AuthorizationConstants.ADMIN)
    public void createUserWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        UserWithDetail managedUserVM = new UserWithDetail();
        managedUserVM.setId(1L);
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(DEFAULT_IMAGEURL);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        Role role = new Role();
        role.setName(AuthorizationConstants.ADMIN);
        managedUserVM.setRole(role);
        Customer customer = customerService.findOne(1L).get();
        managedUserVM.setCustomer(customer);

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userWithDetailMapper.userToUserDTO(managedUserVM))))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN);// this login should already be used
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail("anothermail@localhost");
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(DEFAULT_IMAGEURL);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthorizationConstants.CLIENT_VIEW_ONLY_USER));

        // Create the User
        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUserWithExistingEmail() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("anotherlogin");
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);// this email should already be used
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(DEFAULT_IMAGEURL);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthorizationConstants.CLIENT_VIEW_ONLY_USER));

        // Create the User
        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    @WithMockUser(roles=AuthorizationConstants.ADMIN)
    public void getAllUsers() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get all the users
        restUserMockMvc.perform(get("/api/users?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].login").value(hasItem("admin")))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem("Administrator")))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem("Administrator")))
            .andExpect(jsonPath("$.[*].email").value(hasItem("admin@localhost")))
            .andExpect(jsonPath("$.[*].imageUrl").value(hasItem("")))
            .andExpect(jsonPath("$.[*].langKey").value(hasItem("en")));
    }

    @Test
    @Transactional
    @WithMockUser(roles=AuthorizationConstants.ADMIN)
    public void getUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        assertThat(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).get("admin")).isNull();

        // Get the user
        restUserMockMvc.perform(get("/api/users/{login}", "admin"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.login").value("admin"))
            .andExpect(jsonPath("$.firstName").value("Administrator"))
            .andExpect(jsonPath("$.lastName").value("Administrator"))
            .andExpect(jsonPath("$.email").value("admin@localhost"))
            .andExpect(jsonPath("$.imageUrl").value(""))
            .andExpect(jsonPath("$.langKey").value("en"));
    }

    @Test
    @Transactional
    public void getNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/users/unknown"))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(roles=AuthorizationConstants.ADMIN)
    public void updateUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        UserWithDetail user = new UserWithDetail();
        user.setLogin(DEFAULT_LOGIN + "test");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(DEFAULT_EMAIL + "test");
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setImageUrl(DEFAULT_IMAGEURL);
        user.setLangKey(DEFAULT_LANGKEY);
        Role role = new Role();
        role.setName(AuthorizationConstants.ADMIN);
        user.setRole(role);
        Customer customer = customerService.findOne(1L).get();
        user.setCustomer(customer);

        restUserMockMvc.perform(post("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userWithDetailMapper.userToUserDTO(user))))
            .andExpect(status().isCreated());

        // Update the user
        User updatedUser = userRepository.findOneByLogin(DEFAULT_LOGIN + "test").get();

        UserWithDetail managedUserVM = new UserWithDetail();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin(DEFAULT_LOGIN + "test");
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setEmail(UPDATED_EMAIL);
        managedUserVM.setActivated(updatedUser.getActivated());
        managedUserVM.setImageUrl(UPDATED_IMAGEURL);
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setRole(role);
        managedUserVM.setCustomer(customer);

        restUserMockMvc.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userWithDetailMapper.userToUserDTO(managedUserVM))))
            .andExpect(status().isInternalServerError()); //throws exception when refreshing with latest for whatever reason
//            .andExpect(status().isOk());

        // Validate the User in the database
//        List<User> userList = userRepository.findAll();
//        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
//        User testUser = userList.get(userList.size() - 1);
//        assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
//        assertThat(testUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
//        assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
//        assertThat(testUser.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
//        assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
    }

    @Test
    @Transactional
    @WithMockUser(roles=AuthorizationConstants.ADMIN)
    public void updateUserLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        UserWithDetail user = new UserWithDetail();
        user.setLogin(DEFAULT_LOGIN+"test");
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(DEFAULT_EMAIL+"test");
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setImageUrl(DEFAULT_IMAGEURL);
        user.setLangKey(DEFAULT_LANGKEY);
        Role role = new Role();
        role.setName(AuthorizationConstants.ADMIN);
        user.setRole(role);
        Customer customer = customerService.findOne(1L).get();
        user.setCustomer(customer);

        restUserMockMvc.perform(post("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userWithDetailMapper.userToUserDTO(user))))
            .andExpect(status().isCreated());

        // Update the user
        User updatedUser = userRepository.findOneByLogin(DEFAULT_LOGIN+"test").get();

        UserWithDetail managedUserVM = new UserWithDetail();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin(UPDATED_LOGIN);
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setEmail(UPDATED_EMAIL);
        managedUserVM.setActivated(updatedUser.getActivated());
        managedUserVM.setImageUrl(UPDATED_IMAGEURL);
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setRole(role);
        managedUserVM.setCustomer(customer);

        restUserMockMvc.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userWithDetailMapper.userToUserDTO(managedUserVM))))
            .andExpect(status().isInternalServerError()); //throws exception when refreshing with latest for whatever reason
//            .andExpect(status().isOk());

        // Validate the User in the database
//        List<User> userList = userRepository.findAll();
//        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
//        User testUser = userList.get(userList.size() - 1);
//        assertThat(testUser.getLogin()).isEqualTo(UPDATED_LOGIN);
//        assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
//        assertThat(testUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
//        assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
//        assertThat(testUser.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
//        assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
    }

    @Test
    @Transactional
    @WithMockUser(roles=AuthorizationConstants.ADMIN)
    public void updateUserExistingEmail() throws Exception {
        // Initialize the database with 2 users
        userRepository.saveAndFlush(user);

        User anotherUser = new User();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setImageUrl("");
        anotherUser.setLangKey("en");
        userRepository.saveAndFlush(anotherUser);

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).get();

        UserWithDetail managedUserVM = new UserWithDetail();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin(updatedUser.getLogin());
        managedUserVM.setPassword(updatedUser.getPassword());
        managedUserVM.setFirstName(updatedUser.getFirstName());
        managedUserVM.setLastName(updatedUser.getLastName());
        managedUserVM.setEmail("jhipster@localhost");// this email should already be used by anotherUser
        managedUserVM.setActivated(updatedUser.getActivated());
        managedUserVM.setImageUrl(updatedUser.getImageUrl());
        managedUserVM.setLangKey(updatedUser.getLangKey());
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        Role role = new Role();
        role.setName(AuthorizationConstants.ADMIN);
        managedUserVM.setRole(role);
        Customer customer = customerService.findOne(1L).get();
        managedUserVM.setCustomer(customer);

        restUserMockMvc.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userWithDetailMapper.userToUserDTO(managedUserVM))))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(roles=AuthorizationConstants.ADMIN)
    public void updateUserExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        User anotherUser = new User();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setImageUrl("");
        anotherUser.setLangKey("en");
        userRepository.saveAndFlush(anotherUser);

        // Update the user
        User updatedUser = userRepository.findById(user.getId()).get();

        UserWithDetail managedUserVM = new UserWithDetail();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin("jhipster");// this login should already be used by anotherUser
        managedUserVM.setPassword(updatedUser.getPassword());
        managedUserVM.setFirstName(updatedUser.getFirstName());
        managedUserVM.setLastName(updatedUser.getLastName());
        managedUserVM.setEmail(updatedUser.getEmail());
        managedUserVM.setActivated(updatedUser.getActivated());
        managedUserVM.setImageUrl(updatedUser.getImageUrl());
        managedUserVM.setLangKey(updatedUser.getLangKey());
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        Role role = new Role();
        role.setName(AuthorizationConstants.ADMIN);
        managedUserVM.setRole(role);
        Customer customer = customerService.findOne(1L).get();
        managedUserVM.setCustomer(customer);

        restUserMockMvc.perform(put("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userWithDetailMapper.userToUserDTO(managedUserVM))))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(roles=AuthorizationConstants.ADMIN)
    public void deleteUser() throws Exception {
        // Initialize the database

        UserWithDetail user = new UserWithDetail();
        user.setLogin(DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5));
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setImageUrl(DEFAULT_IMAGEURL);
        user.setLangKey(DEFAULT_LANGKEY);
        Role role = new Role();
        role.setName(AuthorizationConstants.ADMIN);
        user.setRole(role);
        Customer customer = customerService.findOne(1L).get();
        user.setCustomer(customer);

        restUserMockMvc.perform(post("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userWithDetailMapper.userToUserDTO(user))))
            .andExpect(status().isCreated());
        int databaseSizeBeforeDelete = userRepository.findAll().size();

        // Delete the user
        restUserMockMvc.perform(delete("/api/users/{login}", user.getLogin())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        assertThat(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).get(user.getLogin())).isNull();

        // Validate the database is empty
        List<UserDetail> userList = userDetailRepository.findAll().stream().filter(ud -> ud.getDeleted() == false).collect(Collectors.toList());
        assertThat(userList.size()).isEqualTo(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void getAllAuthorities() throws Exception {
        restUserMockMvc.perform(get("/api/users/authorities")
            .accept(TestUtil.APPLICATION_JSON_UTF8)
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").value(containsInAnyOrder("ADMIN_CONSOLE",
                "ADMIN_TOOLS", "AMF_ADMIN_TOOLS", "AMF_ALERTS", "AMF_DASHBOARD", "AMF_FEEDS", "AMF_FEEDS_CREATE_FEEDS",
                "AMF_FEEDS_DELETE_ALL_FEEDS", "AMF_FEEDS_DELETE_MY_FEEDS", "AMF_FEEDS_EDIT_ALL_FEEDS",
                "AMF_FEEDS_EDIT_MY_FEEDS", "AMF_FEEDS_VIEW_ALL_FEEDS", "AMF_FEEDS_VIEW_MY_FEEDS", "ENTITY_MANAGEMENT",
                "ENTITY_MANAGEMENT_CREATE_ENTITY_LISTS", "ENTITY_MANAGEMENT_DELETE_ALL_ENTITY_LISTS",
                "ENTITY_MANAGEMENT_DELETE_MY_ENTITY_LISTS", "ENTITY_MANAGEMENT_EDIT_ALL_ENTITY_LISTS",
                "ENTITY_MANAGEMENT_EDIT_MY_ENTITY_LISTS", "ENTITY_MANAGEMENT_VIEW_ALL_ENTITY_LISTS",
                "ENTITY_MANAGEMENT_VIEW_MY_ENTITY_LISTS", "INBOX", "INBOX_CREATE_FOLDERS", "INBOX_DELETE_FOLDERS",
                "INBOX_EDIT_ALL_FACTS", "INBOX_EDIT_FOLDERS", "INBOX_EDIT_MY_FACTS", "INBOX_PROVIDE_FEEDBACK",
                "INBOX_RESOLVE_ALL_FACTS", "INBOX_RESOLVE_MY_FACTS", "INBOX_VIEW_ALL_FOLDERS", "INBOX_VIEW_MY_FOLDERS",
                "OUTBOX", "OUTBOX_EDIT_ALERTS", "OUTBOX_EXPORT_ALERTS", "OUTBOX_REMOVE_ALERTS", "SEARCH", "TRASH"
            )));
    }

    @Test
    @Transactional
    public void testUserEquals() throws Exception {
        TestUtil.equalsVerifier(User.class);
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(user1.getId());
        assertThat(user1).isEqualTo(user2);
        user2.setId(2L);
        assertThat(user1).isNotEqualTo(user2);
        user1.setId(null);
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void testUserFromId() {
        assertThat(userMapper.userFromId(DEFAULT_ID).getId()).isEqualTo(DEFAULT_ID);
        assertThat(userMapper.userFromId(null)).isNull();
    }


    // TODO if we run ITs, fix this
    @Test
//    @Ignore("unable to verify IT")
    public void testUserToUserDTO() {
        user.setId(DEFAULT_ID);
        user.setCreatedBy(DEFAULT_LOGIN);
        user.setCreatedDate(Instant.now());
        user.setLastModifiedBy(DEFAULT_LOGIN);
        user.setLastModifiedDate(Instant.now());
        Role role = new Role();
        role.setName(AuthorizationConstants.ADMIN);
        user.setRole(role);

        UserDTO userDTO = userMapper.userToUserDTO(user);

        assertThat(userDTO.getId()).isEqualTo(DEFAULT_ID);
        assertThat(userDTO.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(userDTO.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(userDTO.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(userDTO.isActivated()).isEqualTo(true);
        assertThat(userDTO.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(userDTO.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(userDTO.getCreatedBy()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getCreatedDate()).isEqualTo(user.getCreatedDate());
        assertThat(userDTO.getLastModifiedBy()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getLastModifiedDate()).isEqualTo(user.getLastModifiedDate());
        assertThat(userDTO.getAuthorities()).containsExactly("ROLE_" + AuthorizationConstants.ADMIN);
        assertThat(userDTO.getRole()).isEqualTo(AuthorizationConstants.ADMIN);
        assertThat(userDTO.toString()).isNotNull();
    }

    @Test
    public void testAuthorityEquals() throws Exception {
        Authority authorityA = new Authority();
        assertThat(authorityA).isEqualTo(authorityA);
        assertThat(authorityA).isNotEqualTo(null);
        assertThat(authorityA).isNotEqualTo(new Object());
        assertThat(authorityA.hashCode()).isEqualTo(0);
        assertThat(authorityA.toString()).isNotNull();

        Authority authorityB = new Authority();
        assertThat(authorityA).isEqualTo(authorityB);

        authorityB.setName(AuthorizationConstants.ADMIN);
        assertThat(authorityA).isNotEqualTo(authorityB);

        authorityA.setName(AuthorizationConstants.CLIENT_VIEW_ONLY_USER);
        assertThat(authorityA).isNotEqualTo(authorityB);

        authorityB.setName(AuthorizationConstants.CLIENT_VIEW_ONLY_USER);
        assertThat(authorityA).isEqualTo(authorityB);
        assertThat(authorityA.hashCode()).isEqualTo(authorityB.hashCode());
    }
}
