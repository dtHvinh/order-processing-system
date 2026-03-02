### `maven-resources-plugin` or `maven-dependency-plugin`

#### `maven-resources-plugin` code example:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.8.1</version>
    <executions>
        <execution>
            <id>copy-common-bundle</id>
            <phase>package</phase>
            <goals>
                <goal>copy</goal>
            </goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>${project.groupId}</groupId>
                        <artifactId>common</artifactId>
                        <version>${project.version}</version>
                        <type>jar</type>
                        <outputDirectory>${project.build.directory}</outputDirectory>
                        <destFileName>common-${project.version}.jar</destFileName>
                    </artifactItem>
                </artifactItems>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### `maven-dependency-plugin` code example:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.8.1</version>
    <executions>
        <execution>
            <id>copy-common-bundle</id>
            <phase>package</phase>
            <goals>
                <goal>copy</goal>
            </goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>${project.groupId}</groupId>
                        <artifactId>common</artifactId>
                        <version>${project.version}</version>
                        <type>jar</type>
                        <outputDirectory>${project.build.directory}</outputDirectory>
                        <destFileName>common-${project.version}.jar</destFileName>
                    </artifactItem>
                </artifactItems>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Top solution has better dynamic dependency resolution but it's cause some error in visual studio code