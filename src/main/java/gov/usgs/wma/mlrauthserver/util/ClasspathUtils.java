package gov.usgs.wma.mlrauthserver.util;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class ClasspathUtils {
    
    public static Resource loadFromFileOrClasspath(String filePath) {
		ResourceLoader loader;
		String adjustedPath;

		if(filePath.toLowerCase().startsWith("classpath:")){
			loader = new DefaultResourceLoader();
			adjustedPath = filePath.replaceFirst("classpath:", "");
		} else {
			loader = new FileSystemResourceLoader();
			adjustedPath = filePath;
		}

		return loader.getResource(adjustedPath);
	}
}