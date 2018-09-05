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



#### 覆盖文件问题
在IntrospectedTableMyBatis3Impl.getGeneratedXmlFiles方法中，isMergeable值被写死为true了。
<pre>	
GeneratedXmlFile gxf = new GeneratedXmlFile(document,
        getMyBatis3XmlMapperFileName(), getMyBatis3XmlMapperPackage(),
        context.getSqlMapGeneratorConfiguration().getTargetProject(),
        true, context.getXmlFormatter());
</pre>
而MyBatisGenerator.writeGeneratedXmlFile方法中使用到该属性了。代码如下：
<pre>
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
</pre>
关键点就在第2行，结果导致每次重新生成后都是追加。
鉴于此，可以使用反射在运行时把GeneratedXmlFile对象的isMergeable强制改成false