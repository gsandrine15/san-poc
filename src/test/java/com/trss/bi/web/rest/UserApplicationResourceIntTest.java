package com.trss.bi.web.rest;

import com.trss.bi.BiAuthApp;

import com.trss.bi.config.SecurityBeanOverrideConfiguration;

import com.trss.bi.domain.UserApplication;
import com.trss.bi.domain.User;
import com.trss.bi.domain.Application;
import com.trss.bi.repository.UserApplicationRepository;
import com.trss.bi.security.AuthorizationConstants;
import com.trss.bi.service.UserApplicationService;
import com.trss.bi.service.UserService;
import com.trss.bi.service.UserWithDetailService;
import com.trss.bi.service.dto.UserApplicationDTO;
import com.trss.bi.service.mapper.UserApplicationMapper;
import com.trss.bi.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;


import static com.trss.bi.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the UserApplicationResource REST controller.
 *
 * @see UserApplicationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BiAuthApp.class)
public class UserApplicationResourceIntTest {

    private static final Boolean DEFAULT_HIDE = false;
    private static final Boolean UPDATED_HIDE = true;

    @Autowired
    private UserApplicationRepository userApplicationRepository;

    @Autowired
    private UserApplicationMapper userApplicationMapper;

    @Autowired
    private UserApplicationService userApplicationService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restUserApplicationMockMvc;

    private UserApplication userApplication;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final UserApplicationResource userApplicationResource = new UserApplicationResource(userApplicationService);
        this.restUserApplicationMockMvc = MockMvcBuilders.standaloneSetup(userApplicationResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UserApplication createEntity(EntityManager em) {
        UserApplication userApplication = new UserApplication()
            .hide(DEFAULT_HIDE);
        // Add required entity
        User user = UserResourceIntTest.createEntity(em);
        em.persist(user);
        em.flush();
        userApplication.setUser(user);
        // Add required entity
        Application application = ApplicationResourceIntTest.createEntity(em);
        em.persist(application);
        em.flush();
        userApplication.setApplication(application);
        return userApplication;
    }

    @Before
    public void initTest() {
        userApplication = createEntity(em);
    }

    @Test
    @Transactional
    @WithMockUser(roles = AuthorizationConstants.ADMIN)
    public void getUserApplication() throws Exception {
        // Initialize the database
        userApplicationRepository.saveAndFlush(userApplication);

        // Get the userApplication
        restUserApplicationMockMvc.perform(get("/api/user-applications/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$[*].id", containsInAnyOrder(1)))
            .andExpect(jsonPath("$[*].hide", containsInAnyOrder(true)));
    }
    @Test
    @Transactional
    @WithMockUser(roles = AuthorizationConstants.ADMIN)
    public void getNonExistingUserApplication() throws Exception {
        // Get the userApplication
        restUserApplicationMockMvc.perform(get("/api/user-applications/{id}", Long.MAX_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateNonExistingUserApplication() throws Exception {
        int databaseSizeBeforeUpdate = userApplicationRepository.findAll().size();

        // Create the UserApplication
        UserApplicationDTO userApplicationDTO = userApplicationMapper.toDto(userApplication);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUserApplicationMockMvc.perform(put("/api/user-applications/" + userApplicationDTO.getId())
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userApplication)))
            .andExpect(status().isBadRequest());

        // Validate the UserApplication in the database
        List<UserApplication> userApplicationList = userApplicationRepository.findAll();
        assertThat(userApplicationList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UserApplication.class);
        UserApplication userApplication1 = new UserApplication();
        userApplication1.setId(1L);
        UserApplication userApplication2 = new UserApplication();
        userApplication2.setId(userApplication1.getId());
        assertThat(userApplication1).isEqualTo(userApplication2);
        userApplication2.setId(2L);
        assertThat(userApplication1).isNotEqualTo(userApplication2);
        userApplication1.setId(null);
        assertThat(userApplication1).isNotEqualTo(userApplication2);
    }
}
