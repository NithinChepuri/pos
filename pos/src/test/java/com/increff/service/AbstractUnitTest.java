package com.increff.service;

import com.increff.spring.QaConfig;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = QaConfig.class)
@WebAppConfiguration
@Transactional
public abstract class AbstractUnitTest {
    // Common test setup and utilities can go here
} 