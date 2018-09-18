package gov.usgs.wma.mlrauthserver.config.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.usgs.wma.mlrauthserver.dao.WaterAuthResourceIdAuthsDAO;

import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@SpringBootConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WaterAuthResourceIdAuthsDAO.class })

public class WaterAuthResourceIdAuthsDAOTest {
    @MockBean
    private DataSource dataSource;
    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WaterAuthResourceIdAuthsDAO authDao;

    private String list1 = "group1,group2,group3";
    private String list2 = "group1";
    private String list3 = "";
    private String list4 = null;

    @Before
    public void setup() {

    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAuthListForResourceIdTest() {
        given(jdbcTemplate.queryForObject(any(String.class), any(Object[].class), any(Class.class))).willReturn(list1);
        Set<String> result = authDao.getAuthListForResourceId("test");
        assertEquals(result.size(), 3);
        assertThat(result, containsInAnyOrder("group1", "group2", "group3"));
        given(jdbcTemplate.queryForObject(any(String.class), any(Object[].class), any(Class.class))).willReturn(list2);
        result = authDao.getAuthListForResourceId("test");
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder("group1"));
        given(jdbcTemplate.queryForObject(any(String.class), any(Object[].class), any(Class.class))).willReturn(list3);
        result = authDao.getAuthListForResourceId("test");
        assertEquals(result.size(), 0);
        given(jdbcTemplate.queryForObject(any(String.class), any(Object[].class), any(Class.class))).willReturn(list4);
        result = authDao.getAuthListForResourceId("test");
        assertEquals(result.size(), 0);
        given(jdbcTemplate.queryForObject(any(String.class), any(Object[].class), any(Class.class))).willThrow(new EmptyResultDataAccessException("error", 1));
        result = authDao.getAuthListForResourceId("test");
        assertTrue(result == null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAuthListForResourceIdListTest1() {        
        given(jdbcTemplate.queryForObject(any(String.class), eq(new Object[]{"test1"}), any(Class.class))).willReturn(list1);
        given(jdbcTemplate.queryForObject(any(String.class), eq(new Object[]{"test2"}), any(Class.class))).willReturn(list2);
        given(jdbcTemplate.queryForObject(any(String.class), eq(new Object[]{"test3"}), any(Class.class))).willReturn(list3);
        given(jdbcTemplate.queryForObject(any(String.class), eq(new Object[]{"test4"}), any(Class.class))).willReturn(list4);
        given(jdbcTemplate.queryForObject(any(String.class), eq(new Object[]{"test5"}), any(Class.class))).willThrow(new EmptyResultDataAccessException("error", 1));
    
        Set<String> result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test1")));
        assertEquals(result.size(), 3);
        assertThat(result, containsInAnyOrder("group1", "group2", "group3"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test1", "test2")));
        assertEquals(result.size(), 3);
        assertThat(result, containsInAnyOrder("group1", "group2", "group3"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test1", "test2", "test3")));
        assertEquals(result.size(), 3);
        assertThat(result, containsInAnyOrder("group1", "group2", "group3"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test1", "test2", "test3", "test4")));
        assertEquals(result.size(), 3);
        assertThat(result, containsInAnyOrder("group1", "group2", "group3"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test1", "test2", "test3", "test4", "test5")));
        assertEquals(result.size(), 3);
        assertThat(result, containsInAnyOrder("group1", "group2", "group3"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test2")));
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder("group1"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test2", "test3")));
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder("group1"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test2", "test3", "test4")));
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder("group1"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test2", "test3", "test4", "test5")));
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder("group1"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test3")));
        assertEquals(result.size(), 0);

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test3", "test4")));
        assertEquals(result.size(), 0);

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test3", "test4", "test5")));
        assertEquals(result.size(), 0);

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test4")));
        assertEquals(result.size(), 0);

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test4", "test5")));
        assertEquals(result.size(), 0);

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test5")));
        assertEquals(result.size(), 0);

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test5", "test1")));
        assertEquals(result.size(), 3);
        assertThat(result, containsInAnyOrder("group1", "group2", "group3"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test2", "test5", "test1")));
        assertEquals(result.size(), 3);
        assertThat(result, containsInAnyOrder("group1", "group2", "group3"));
        
        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test4", "test2")));
        assertEquals(result.size(), 1);
        assertThat(result, containsInAnyOrder("group1"));

        result = authDao.getAuthListForResourceIdList(new HashSet<>(Arrays.asList("test4", "test2", "test3", "test1")));
        assertEquals(result.size(), 3);
        assertThat(result, containsInAnyOrder("group1", "group2", "group3"));
    }
}