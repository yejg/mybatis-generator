package com.yejg.mybatis.generator;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.internal.DefaultShellCallback;

public class TestCase {

	@Test
	public void test() throws Exception {
		List<String> warnings = new ArrayList<String>();
		boolean overwrite = true;
		ConfigurationParser cp = new ConfigurationParser(warnings);

		// String configFilePath = "E:/eclipse_workspace/workspace_luna_experiment_branch/mybatis-generator/src/main/resources/generatorConfig.xml";
		// FileInputStream fis = new FileInputStream(configFilePath);
		// Configuration config = cp.parseConfiguration(fis);

		Configuration config = cp.parseConfiguration(MainGenerate.class.getClassLoader().getResourceAsStream("generatorConfig.xml"));

		DefaultShellCallback callback = new DefaultShellCallback(overwrite);
		MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
		myBatisGenerator.generate(null);
		System.out.println("----done----");
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {}

	@Before
	public void setUp() throws Exception {}

	@After
	public void tearDown() throws Exception {}

}
