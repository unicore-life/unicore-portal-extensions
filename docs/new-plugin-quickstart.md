# New Plugin Quickstart Setup

This document introduce how to add new UNICORE Portal plugin to the project. 

For the purpose of this instruction, assume that it has name: **pl.unicore.portal.awesome-plugin**.


### Prepare sources

First, enter the project directory and copy recursively sources from *template-project* submodule:

```bash 
cp -r template-plugin pl.unicore.portal.awesome-plugin
```


### Compile and build distribution

After copy the template sources, it should be enough to run: 

```bash
./gradlew :pl.unicore.portal.awesome-plugin:build
```

in order to compile and build plugin distribution. 

The distribution archive will be located at:
`pl.unicore.portal.awesome-plugin/build/distributions/pl.unicore.portal.awesome-plugin-0.1.0-SNAPSHOT.zip`.


### Plugin installation

In order to add plugin to *UNICORE Portal*, one should extract archive and add all JAR files
(there should be at least file named `pl.unicore.portal.awesome-plugin-0.1.0-SNAPSHOT.jar`) 
to Portal *CLASSPATH* (for example by coping it into `lib/` subdirectory).

Next, one have to enable new plugin by adding in configuration file `portal.properties` line:
```properties
portal.ui.menu.navigation.contents.10=TemplatePlugin.MenuEntry
``` 

Do not forget to restart *UNICORE Portal* and read logs if everything started correctly. 
If everything went correctly, there should be a new menu entry called `template`. Use it!


## Develop and enjoy

Final step is to add your own implementation. In case of any bugs or questions do not hesitate 
to create [Github issue](https://github.com/unicore-life/unicore-portal-extensions/issues).
