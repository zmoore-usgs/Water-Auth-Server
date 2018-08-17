package gov.usgs.wma.mlrauthserver.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SpringBootTest
@SpringBootConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=testuser",
    "spring.datasource.password=testpassword",
    "spring.session.jdbc.initializer.enabled=true",
    "spring.datasource.driverClassName=org.h2.Driver",
})
@ContextConfiguration(classes = { PersistenceConfig.class } )
public class PersistenceConfigTest {
	
	
	@Autowired
	private DataSource dataSource;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testConnectionIsNotClosed() throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			assertThat(dataSource.getConnection().isClosed(), is(false));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testDatabaseTablesExisting() throws SQLException {
		String sql = "show tables;";
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement stmt = connection.prepareStatement(sql);
			List<String> tableNames = new ArrayList<>();
			try (ResultSet resultSet = stmt.executeQuery()) {
				while (resultSet.next()) {
					tableNames.add(resultSet.getString(1));
				}
			}
			assertTrue(String.format("%s not in database table names", "OAUTH_CLIENT_DETAILS"), tableNames.contains("OAUTH_CLIENT_DETAILS"));
			assertTrue(String.format("%s not in database table names", "OAUTH_CODE"), tableNames.contains("OAUTH_CODE"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
