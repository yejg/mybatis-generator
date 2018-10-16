package com.yejg.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.plugins.SerializablePlugin;

import java.util.List;
import java.util.Properties;

/**
 * 功能说明: <br>
 * 系统版本: v1.0<br>
 * 开发人员: @author yejg<br>
 * 开发时间: 2018年10月15日<br>
 */
public class SerializableModelAndExamplePlugin extends PluginAdapter {

	private FullyQualifiedJavaType serializable;
	private FullyQualifiedJavaType gwtSerializable;
	private boolean addGWTInterface;
	private boolean suppressJavaInterface;

	public SerializableModelAndExamplePlugin() {
		super();
		serializable = new FullyQualifiedJavaType("java.io.Serializable"); //$NON-NLS-1$
		gwtSerializable = new FullyQualifiedJavaType("com.google.gwt.user.client.rpc.IsSerializable"); //$NON-NLS-1$
	}

	@Override
	public boolean validate(List<String> warnings) {
		// this plugin is always valid
		return true;
	}

	@Override
	public void setProperties(Properties properties) {
		super.setProperties(properties);
		addGWTInterface = Boolean.valueOf(properties.getProperty("addGWTInterface")); //$NON-NLS-1$
		suppressJavaInterface = Boolean.valueOf(properties.getProperty("suppressJavaInterface")); //$NON-NLS-1$
	}

	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
												 IntrospectedTable introspectedTable) {
		makeSerializable(topLevelClass, introspectedTable);
		return true;
	}

	@Override
	public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass,
												 IntrospectedTable introspectedTable) {
		makeSerializable(topLevelClass, introspectedTable);
		return true;
	}

	@Override
	public boolean modelRecordWithBLOBsClassGenerated(
			TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		makeSerializable(topLevelClass, introspectedTable);
		return true;
	}

	protected void makeSerializable(TopLevelClass topLevelClass,
									IntrospectedTable introspectedTable) {
		if (addGWTInterface) {
			topLevelClass.addImportedType(gwtSerializable);
			topLevelClass.addSuperInterface(gwtSerializable);
		}

		if (!suppressJavaInterface) {
			topLevelClass.addImportedType(serializable);
			topLevelClass.addSuperInterface(serializable);

			Field field = new Field();
			field.setFinal(true);
			field.setInitializationString("1L"); //$NON-NLS-1$
			field.setName("serialVersionUID"); //$NON-NLS-1$
			field.setStatic(true);
			field.setType(new FullyQualifiedJavaType("long")); //$NON-NLS-1$
			field.setVisibility(JavaVisibility.PRIVATE);

			if (introspectedTable.getTargetRuntime() == IntrospectedTable.TargetRuntime.MYBATIS3_DSQL) {
				context.getCommentGenerator().addFieldAnnotation(field, introspectedTable,
						topLevelClass.getImportedTypes());
			} else {
				context.getCommentGenerator().addFieldComment(field, introspectedTable);
			}

			topLevelClass.addField(field);
		}
	}


	/**
	 * 添加给Example类序列化的方法
	 *
	 * @param topLevelClass
	 * @param introspectedTable
	 * @return
	 */
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		makeSerializable(topLevelClass, introspectedTable);

		for (InnerClass innerClass : topLevelClass.getInnerClasses()) {
			if ("GeneratedCriteria".equals(innerClass.getType().getShortName())) { //$NON-NLS-1$
				innerClass.addSuperInterface(serializable);
			}
			if ("Criteria".equals(innerClass.getType().getShortName())) { //$NON-NLS-1$
				innerClass.addSuperInterface(serializable);
			}
			if ("Criterion".equals(innerClass.getType().getShortName())) { //$NON-NLS-1$
				innerClass.addSuperInterface(serializable);
			}
		}

		return true;
	}
}
