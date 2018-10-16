package com.yejg.mybatis.generator.plugins;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.InnerEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class MyCommentGenerator implements CommentGenerator {

	private Properties systemPro;
	private boolean suppressAllComments;
	private SimpleDateFormat dateFormat;

	public MyCommentGenerator() {
		super();
		systemPro = System.getProperties();
		suppressAllComments = false;
		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	}

	/**
	 * 生成java model的类头上的注释
	 */
	@Override
	public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		if (suppressAllComments) {
			return;
		}

		topLevelClass.addJavaDocLine("/**");
		StringBuffer sb = new StringBuffer();
		sb.append(" * ");
		sb.append(introspectedTable.getRemarks());
		sb.append(" [");
		sb.append(introspectedTable.getFullyQualifiedTable().toString().toLowerCase());
		sb.append("]");
		topLevelClass.addJavaDocLine(sb.toString());
		topLevelClass.addJavaDocLine(" * @author " + systemPro.getProperty("user.name"));
		topLevelClass.addJavaDocLine(" */");
	}

	/**
	 * 添加字段注释
	 */
	@Override
	public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
		if (suppressAllComments) {
			return;
		}
		String remarks = introspectedColumn.getRemarks();
		if (remarks == null || remarks.length() == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("/** ").append(introspectedColumn.getRemarks().replace("\n", " ")).append(" */");
		field.addJavaDocLine(sb.toString());
	}

	@Override
	public void addFieldComment(Field field, IntrospectedTable introspectedTable) {

	}

	@Override
	public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {

	}

	@Override
	public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {

	}

	@Override
	public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {

	}

	@Override
	public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {

	}

	@Override
	public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {}

	@Override
	public void addConfigurationProperties(Properties properties) {

	}

	@Override
	public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {

	}

	@Override
	public void addJavaFileComment(CompilationUnit compilationUnit) {

	}

	@Override
	public void addComment(XmlElement xmlElement) {

	}

	@Override
	public void addRootComment(XmlElement rootElement) {

	}

	@Override
	public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {

	}

	@Override
	public void addGeneralMethodAnnotation(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {

	}

	@Override
	public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {

	}

	@Override
	public void addFieldAnnotation(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Set<FullyQualifiedJavaType> imports) {

	}

	@Override
	public void addClassAnnotation(InnerClass innerClass, IntrospectedTable introspectedTable, Set<FullyQualifiedJavaType> imports) {

	}

	protected String getDateString() {
		return dateFormat.format(new Date());
	}
}