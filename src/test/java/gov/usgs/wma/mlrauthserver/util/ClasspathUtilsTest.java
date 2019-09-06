package gov.usgs.wma.mlrauthserver.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.core.io.Resource;

public class ClasspathUtilsTest {

	@Test
	public void loadFromFileOrClasspathTest1() {
		String path = "classpath:water-auth-load-from-file-or-classpath-test.txt";
		Resource result = ClasspathUtils.loadFromFileOrClasspath(path);
		assertTrue(result.exists());
		assertTrue(result.isReadable());
		assertEquals(result.getFilename(), "water-auth-load-from-file-or-classpath-test.txt");
	}

	@Test
	public void loadFromFileOrClasspathTest2() {
		String path = "classpath:water-auth-load-from-file-or-classpath-test-1.txt";
		Resource result = ClasspathUtils.loadFromFileOrClasspath(path);
		assertFalse(result.exists());
		assertFalse(result.isReadable());
	}

	@Test
	public void loadFromFileOrClasspathTest3() {
		String path = "water-auth-load-from-file-or-classpath-test.txt";
		Resource result = ClasspathUtils.loadFromFileOrClasspath(path);
		assertFalse(result.exists());
		assertFalse(result.isReadable());
	}
}