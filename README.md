# files

Java File Utilities

[Guide to Using Toolchains]: https://maven.apache.org/guides/mini/guide-using-toolchains.html

# Lessons Learned

## Java 21

Getting things to work with Java 21 was a bit of a challenge, but [Guide to Using Toolchains]
saved the day. I started using Java 21 the day after it was released, so many other tools,
plugins, etc. had not yet been updated to support it.

I had to add the following to my pom.xml file:

```xml
    <build>
        <pluginManagement>
            <plugins>

                <plugin>
                    <artifactId>maven-toolchains-plugin</artifactId>
                    <version>3.1.0</version>
                    <executions>
                        <execution>
                            <goals>
                                <goal>toolchain</goal>
                            </goals>
                        </execution>
                    </executions>
                    <configuration>
                        <toolchains>
                            <jdk>
                                <version>${java.version}</version>
                                <vendor>oracle</vendor>
                            </jdk>
                        </toolchains>
                    </configuration>
                </plugin>

            </plugins>
        </pluginManagement>
    </build>
```
and create ~/.m2/toolchains.xml with the following content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <!-- JDK toolchains -->
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>21</version>
      <vendor>oracle</vendor>
    </provides>
    <configuration>
      <jdkHome>C:\Program Files\Java\jdk-21</jdkHome>
    </configuration>
  </toolchain>
 
  <!-- other toolchains -->

</toolchains>
```
