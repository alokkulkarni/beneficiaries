package com.alok.payment.beneficiaries.bdd.hooks;

import com.alok.payment.beneficiaries.bdd.context.TestContext;
import io.cucumber.java.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.sql.SQLException;

public class DatabaseHooks {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private TestContext testContext;
    
    @After
    public void cleanDatabase() throws SQLException {
        // Reset test context
        testContext.reset();
        
        // Clean all tables
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            
            // Delete all data from beneficiaries table
            statement.execute("DELETE FROM beneficiaries");
            
            // Reset the sequence
            statement.execute("SELECT setval('beneficiaries_id_seq', 1, false)");
        }
        
        // Re-execute init.db to restore test data
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("init.db"));
        populator.execute(dataSource);
    }
}
