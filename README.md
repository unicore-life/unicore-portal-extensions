# unicore-portal-extensions

[![Build Status](https://travis-ci.org/unicore-life/unicore-portal-extensions.svg?branch=master)](https://travis-ci.org/unicore-life/unicore-portal-extensions)

## Development

### Building

Just clone the project and run Gradle command presented below.

```bash
./gradlew build
```

### Releasing

To see current version of UNICORE portal extensions (Gradle modules) use Gradle task
[currentVersion](http://axion-release-plugin.readthedocs.io/en/latest/configuration/tasks.html#currentversion)
(it is stored as a git tag).

```bash
./gradlew :currentVersion
```

To release a new version of UNICORE portal extension (module) use
[release](http://axion-release-plugin.readthedocs.io/en/latest/configuration/tasks.html#release) task.
Sample command are presented below.

```
./gradlew :pl.unicore.portal.sinusmed:release
```

After release, travis automatically builds distribution ZIP archive and attaches it to created release 
on [repository releases page](https://github.com/unicore-life/unicore-portal-extensions/releases).
