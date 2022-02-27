package com.trss.bi.web.rest;

import com.trss.bi.BiAuthApp;

import com.trss.bi.config.SecurityBeanOverrideConfiguration;

import com.trss.bi.domain.*;
import com.trss.bi.repository.CustomerApplicationRepository;
import com.trss.bi.security.AuthorizationConstants;
import com.trss.bi.service.CustomerApplicationService;
import com.trss.bi.service.CustomerService;
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
import java.util.Date;
import java.util.List;


import static com.trss.bi.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the CustomerApplicationResource REST controller.
 *
 * @see CustomerApplicationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BiAuthApp.class)
public class CustomerApplicationResourceIntTest {

    private static final ApplicationAssignmentStatus DEFAULT_STATUS = ApplicationAssignmentStatus.UNAVAILABLE;
    private static final ApplicationAssignmentStatus UPDATED_STATUS = ApplicationAssignmentStatus.AVAILABLE;

    @Autowired
    private CustomerApplicationRepository customerApplicationRepository;

    @Autowired
    private CustomerApplicationService customerApplicationService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restCustomerApplicationMockMvc;

    private CustomerApplication customerApplication;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final CustomerApplicationResource customerApplicationResource = new CustomerApplicationResource(customerApplicationService);
        this.restCustomerApplicationMockMvc = MockMvcBuilders.standaloneSetup(customerApplicationResource)
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
    public static CustomerApplication createEntity(EntityManager em) {
        CustomerApplication customerApplication = new CustomerApplication();
            customerApplication.setStatus(DEFAULT_STATUS);
        // Add required entity
        Customer customer = CustomerResourceIntTest.createEntity(em);
        customer.setMarket(CustomerMarket.CORPORATE);
        customer.setAnalysts("analyst");
        customer.setContractEndDate(new Date());
        customer.setContractStartDate(new Date());
        customer.setBdOwner("bdOwner");
        customer.setSessionTimeoutS(Integer.MAX_VALUE);
        em.persist(customer);
        em.flush();
        customerApplication.setCustomer(customer);
        // Add required entity
        Application application = ApplicationResourceIntTest.createEntity(em);
        application.setCode("code");
        em.persist(application);
        em.flush();
        customerApplication.setApplication(application);
        return customerApplication;
    }

    @Before
    public void initTest() {
        customerApplication = createEntity(em);
    }

    @Test
    @Transactional
    @WithMockUser(roles = AuthorizationConstants.ADMIN)
    public void getCustomerApplication() throws Exception {
        // Initialize the database
        customerApplicationRepository.saveAndFlush(customerApplication);

        // Get the customerApplication
        restCustomerApplicationMockMvc.perform(get("/api/customer-applications/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$[0].status", is("AVAILABLE")));
    }
    @Test
    @Transactional
    @WithMockUser(roles = AuthorizationConstants.ADMIN)
    public void getNonExistingCustomerApplication() throws Exception {
        // Get the customerApplication
        restCustomerApplicationMockMvc.perform(get("/api/customer-applications/{id}", Long.MAX_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CustomerApplication.class);
        CustomerApplication customerApplication1 = new CustomerApplication();
        customerApplication1.setId(1L);
        CustomerApplication customerApplication2 = new CustomerApplication();
        customerApplication2.setId(customerApplication1.getId());
        assertThat(customerApplication1).isEqualTo(customerApplication2);
        customerApplication2.setId(2L);
        assertThat(customerApplication1).isNotEqualTo(customerApplication2);
        customerApplication1.setId(null);
        assertThat(customerApplication1).isNotEqualTo(customerApplication2);
    }
}
