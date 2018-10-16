### 源码版本
版本：1.3.7 <br />
<https://github.com/mybatis/generator>

### 源码修改点
- DatabaseIntrospector#getColumns Line530 获取数据库表的列名都转成小写

### 扩展修改
扩展修改都在[com.yejg.mybatis.generator.plugins]包里
- 增加Oracle和Mysql数据库分页代码生成插件
- 增加 字段、Model 代码注释插件
- 增加OverIsMergeablePlugin插件
- 增加model序列化插件，自带的序列化插件只会序列化model，Example不会序列化

<hr />

## 背景
项目中使用Mybatis做持久层框架，但由于开发成员水平不一，写dao的时候，各有各的偏好，有时候还会写出带sql注入漏洞的代码。

```
出现sql注入漏洞，一般是#和$的区别没弄明白：
$ 直接把字符串原封不动的搬进sql，有sql注入的风险
# 是预留一个问号,作为参数插入的，即可通过预编译sql的方式避免sql注入
```
于是想使用Mybatis generator这个工具来统一生成代码（java bean，mapper，xml）


## 使用
Mybatis generator可以通过如下方式运行
- 命令行
```
下载mybatis-generator-core.jar，然后配置generatorConfig.xml文件，执行如下命令
java -jar mybatis-generator-core-1.3.7.jar -configfile generatorConfig.xml -overwrite
```
- IDE插件，run as
```
安装eclipse/idea插件
```
- 通过main方法执行
```
public static void main(String[] args) throws Exception {
	List<String> warnings = new ArrayList<String>();
	boolean overwrite = true;
	ConfigurationParser cp = new ConfigurationParser(warnings);

	Configuration config = cp.parseConfiguration(MainGenerate.class.getClassLoader().getResourceAsStream("generatorConfig.xml"));

	DefaultShellCallback callback = new DefaultShellCallback(overwrite);
	MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
	myBatisGenerator.generate(null);
	System.out.println("----done----");
}
```

## 问题及解决方法
#### 分页问题
默认生成的xml是没有分页查询的，可通过
```
<plugin type="org.mybatis.generator.plugins.RowBoundsPlugin">
```
来实现分页，不过...
- 在低版本的generator插件里是不包含这个的。
- 使用这个插件生成分页代码后，会多一个selectByExampleWithRowbounds(XxxExample example, RowBounds rowBounds) 的方法，但是XxxMapper.xml文件中的selectByExampleWithRowbounds元素，可以发现select语句并没有使用limit 或者 rownum。
实际上RowBounds原理是通过ResultSet的游标来实现分页，容易出现性能问题

##### 解决办法
可使用pagehelper来解决，See [Github](https://github.com/pagehelper/Mybatis-PageHelper/blob/master/wikis/zh/HowToUse.md) <br />

除此之外，我们也可以通过自定义分页插件来解决
- Oracle插件

```
package com.yejg.mybatis.generator.plugins;

// 省略import

public class OraclePaginationPlugin extends PluginAdapter {

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
```

- MySQl插件

```
package com.yejg.mybatis.generator.plugins;

// 省略import

public class MySQLPaginationPlugin extends PluginAdapter {

	@Override
	public boolean validate(List<String> list) {
		return true;
	}

	/**
	 * 为每个Example类添加limit和offset属性已经set、get方法
	 */
	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

		PrimitiveTypeWrapper integerWrapper = FullyQualifiedJavaType.getIntInstance().getPrimitiveTypeWrapper();

		Field limit = new Field();
		limit.setName("limit");
		limit.setVisibility(JavaVisibility.PRIVATE);
		limit.setType(integerWrapper);
		topLevelClass.addField(limit);

		Method setLimit = new Method();
		setLimit.setVisibility(JavaVisibility.PUBLIC);
		setLimit.setName("setLimit");
		setLimit.addParameter(new Parameter(integerWrapper, "limit"));
		setLimit.addBodyLine("this.limit = limit;");
		topLevelClass.addMethod(setLimit);

		Method getLimit = new Method();
		getLimit.setVisibility(JavaVisibility.PUBLIC);
		getLimit.setReturnType(integerWrapper);
		getLimit.setName("getLimit");
		getLimit.addBodyLine("return limit;");
		topLevelClass.addMethod(getLimit);

		Field offset = new Field();
		offset.setName("offset");
		offset.setVisibility(JavaVisibility.PRIVATE);
		offset.setType(integerWrapper);
		topLevelClass.addField(offset);

		Method setOffset = new Method();
		setOffset.setVisibility(JavaVisibility.PUBLIC);
		setOffset.setName("setOffset");
		setOffset.addParameter(new Parameter(integerWrapper, "offset"));
		setOffset.addBodyLine("this.offset = offset;");
		topLevelClass.addMethod(setOffset);

		Method getOffset = new Method();
		getOffset.setVisibility(JavaVisibility.PUBLIC);
		getOffset.setReturnType(integerWrapper);
		getOffset.setName("getOffset");
		getOffset.addBodyLine("return offset;");
		topLevelClass.addMethod(getOffset);

		return true;
	}

	/**
	 * 为Mapper.xml的selectByExample添加limit
	 */
	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {

		XmlElement ifLimitNotNullElement = new XmlElement("if");
		ifLimitNotNullElement.addAttribute(new Attribute("test", "limit != null"));

		XmlElement ifOffsetNotNullElement = new XmlElement("if");
		ifOffsetNotNullElement.addAttribute(new Attribute("test", "offset != null"));
		ifOffsetNotNullElement.addElement(new TextElement("limit ${offset}, ${limit}"));
		ifLimitNotNullElement.addElement(ifOffsetNotNullElement);

		XmlElement ifOffsetNullElement = new XmlElement("if");
		ifOffsetNullElement.addAttribute(new Attribute("test", "offset == null"));
		ifOffsetNullElement.addElement(new TextElement("limit ${limit}"));
		ifLimitNotNullElement.addElement(ifOffsetNullElement);

		element.addElement(ifLimitNotNullElement);

		return true;
	}
}
```

#### 生成的xml不是覆盖旧文件，有时还有重复的段
问题原因在于：
在IntrospectedTableMyBatis3Impl.getGeneratedXmlFiles方法中，isMergeable值被写死为true了。
```	
GeneratedXmlFile gxf = new GeneratedXmlFile(document,
        getMyBatis3XmlMapperFileName(), getMyBatis3XmlMapperPackage(),
        context.getSqlMapGeneratorConfiguration().getTargetProject(),
        true, context.getXmlFormatter());
```
而MyBatisGenerator.writeGeneratedXmlFile方法中使用到该属性了。代码如下：
```
if (targetFile.exists()) {
    if (gxf.isMergeable()) {
        source = XmlFileMergerJaxp.getMergedSource(gxf, targetFile);
    } else if (shellCallback.isOverwriteEnabled()) {
        source = gxf.getFormattedContent();
        warnings.add(getString("Warning.11", targetFile.getAbsolutePath()));
    } else {
        source = gxf.getFormattedContent();
        targetFile = getUniqueFileName(directory, gxf.getFileName());
        warnings.add(getString("Warning.2", targetFile.getAbsolutePath()));
    }
} else {
    source = gxf.getFormattedContent();
}
```
##### 解决办法
方法一：可直接修改源码，把isMergeable写成false <br />
方法二：拿到GeneratedXmlFile对象，通过反射把isMergeable改成false
```
// 可以在前面自定义的Plugin中，sqlMapGenerated方法中拿到GeneratedXmlFile对象
public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
	try {
		java.lang.reflect.Field field = sqlMap.getClass().getDeclaredField("isMergeable");
		field.setAccessible(true);
		field.setBoolean(sqlMap, false);
	} catch (Exception e) {
		
	}
	return true;
}
```


#### 注释问题
默认的注释完全没什么用，不如自定义注释，把数据库表字段的注释作为bean字段的注释

##### 解决办法
```
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
		StringBuilder sb = new StringBuilder();
		sb.append("/** ").append(introspectedColumn.getRemarks().replace("\n", " ")).append(" */");
		field.addJavaDocLine(sb.toString());
	}

    // 省略了其他方法
}
```
不过在使用的时候发现通过
introspectedColumn.getRemarks()
获取到的注释为null，此问题可通过修改xml配置文件来处理
```
<jdbcConnection driverClass="oracle.jdbc.driver.OracleDriver" connectionURL="jdbc:oracle:thin:@127.0.0.1:1521:YEJG" userId="XXX" password="XXX">
      <!-- 针对oracle数据库 -->
	  <property name="remarksReporting" value="true" />
	  
	  <!-- 针对mysql数据库 -->
	  <!-- <property name="useInformationSchema" value="true" /> -->
</jdbcConnection>
```

#### 序列问题
其实这算不算什么问题，xml配置一下就可以了
```
<table tableName="users">
	<property name="useActualColumnNames" value="true" />
    <generatedKey type="pre" column="SERIAL_NO" sqlStatement="select users_seq.nextval from dual"></generatedKey>
</table>
```
这里需要注意下，generatedKey不要写在property前面了，mybatis generator对顺序有要求的。

#### 字段命名方式问题
数据库表字段是USER_ID形式，生成的bean的字段变成userId形式
##### 解决办法
可在generatorConfig.xml中添加如下配置
```
<property name="useActualColumnNames" value="true" />
```
不过这么一来，bean中的字段就都变成大写的了，期望生成user_id的形式，可通过修改源码来解决
```
// DatabaseIntrospector#getColumns，把column_name先toLowerCase处理一下
introspectedColumn.setActualColumnName(rs.getString("COLUMN_NAME").toLowerCase());
```
