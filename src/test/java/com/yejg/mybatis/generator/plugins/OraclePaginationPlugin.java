package com.yejg.mybatis.generator.plugins;

import java.util.List;

//import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.PrimitiveTypeWrapper;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;


public class OraclePaginationPlugin extends PluginAdapter {

//	// 写在这里也可以
//	public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
//		try {
//			java.lang.reflect.Field field = sqlMap.getClass().getDeclaredField("isMergeable");
//			field.setAccessible(true);
//			field.setBoolean(sqlMap, false);
//		} catch (Exception e) {
//			
//		}
//		return true;
//	}

	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		PrimitiveTypeWrapper integerWrapper = FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper();

		Field begin = new Field();
		begin.setName("begin");
		begin.setVisibility(JavaVisibility.PRIVATE);
		begin.setType(integerWrapper);
		topLevelClass.addField(begin);
		context.getCommentGenerator().addFieldComment(begin, introspectedTable);

		Method setBegin = new Method();
		setBegin.setVisibility(JavaVisibility.PUBLIC);
		setBegin.setName("setBegin");
		setBegin.addParameter(new Parameter(integerWrapper, "begin"));
		setBegin.addBodyLine("this.begin = begin;");
		topLevelClass.addMethod(setBegin);
		context.getCommentGenerator().addGeneralMethodComment(setBegin, introspectedTable);
		
		Method getBegin = new Method();
		getBegin.setVisibility(JavaVisibility.PUBLIC);
		getBegin.setReturnType(integerWrapper);
		getBegin.setName("getBegin");
		getBegin.addBodyLine("return begin;");
		topLevelClass.addMethod(getBegin);
		context.getCommentGenerator().addGeneralMethodComment(getBegin, introspectedTable);

		Field end = new Field();
		end.setName("end");
		end.setVisibility(JavaVisibility.PRIVATE);
		end.setType(integerWrapper);
		topLevelClass.addField(end);
		context.getCommentGenerator().addFieldComment(end, introspectedTable);

		Method setEnd = new Method();
		setEnd.setVisibility(JavaVisibility.PUBLIC);
		setEnd.setName("setEnd");
		setEnd.addParameter(new Parameter(integerWrapper, "end"));
		setEnd.addBodyLine("this.end = end;");
		topLevelClass.addMethod(setEnd);
		context.getCommentGenerator().addGeneralMethodComment(setEnd, introspectedTable);

		Method getEnd = new Method();
		getEnd.setVisibility(JavaVisibility.PUBLIC);
		getEnd.setReturnType(integerWrapper);
		getEnd.setName("getEnd");
		getEnd.addBodyLine("return end;");
		topLevelClass.addMethod(getEnd);
		context.getCommentGenerator().addGeneralMethodComment(getEnd, introspectedTable);

		return true;
	}

	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		XmlElement parentElement = document.getRootElement();

		XmlElement paginationPrefixElement = new XmlElement("sql");
		paginationPrefixElement.addAttribute(new Attribute("id", "Oracle_Paging_Prefix"));
		XmlElement pageStart = new XmlElement("if");
		pageStart.addAttribute(new Attribute("test", "begin != null and end != null"));
		pageStart.addElement(new TextElement("select * from ( select row_.*, rownum rownum_ from ( "));
		context.getCommentGenerator().addComment(paginationPrefixElement);
		paginationPrefixElement.addElement(pageStart);
		parentElement.addElement(paginationPrefixElement);

		XmlElement paginationSuffixElement = new XmlElement("sql");
		paginationSuffixElement.addAttribute(new Attribute("id", "Oracle_Paging_Suffix"));
		XmlElement pageEnd = new XmlElement("if");
		pageEnd.addAttribute(new Attribute("test", "begin != null and end != null"));
		pageEnd.addElement(new TextElement("<![CDATA[ ) row_ ) where rownum_ > #{begin} and rownum_ <= #{end} ]]>"));
		context.getCommentGenerator().addComment(paginationSuffixElement);
		paginationSuffixElement.addElement(pageEnd);
		parentElement.addElement(paginationSuffixElement);

		return super.sqlMapDocumentGenerated(document, introspectedTable);
	}

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {

		XmlElement pageStart = new XmlElement("include"); //$NON-NLS-1$
		pageStart.addAttribute(new Attribute("refid", "Oracle_Paging_Prefix"));
		// context.getCommentGenerator().addComment(pageStart);
		element.getElements().add(0, pageStart);

		XmlElement isNotNullElement = new XmlElement("include"); //$NON-NLS-1$
		isNotNullElement.addAttribute(new Attribute("refid", "Oracle_Paging_Suffix"));
		// context.getCommentGenerator().addComment(isNotNullElement);
		element.getElements().add(isNotNullElement);

		return super.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
	}

	/**
	 * This plugin is always valid - no properties are required
	 */
	public boolean validate(List<String> warnings) {
		return true;
	}
}