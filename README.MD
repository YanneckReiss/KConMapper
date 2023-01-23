[![Version](https://img.shields.io/github/v/release/yanneckreiss/kconmapper?display_name=tag&include_prereleases&style=for-the-badge&color=green)](https://github.com/YanneckReiss/KConMapper/packages/1759983)
[![License Apache 2.0](https://img.shields.io/github/license/yanneckreiss/kconmapper.svg?style=for-the-badge&color=orange)](https://opensource.org/licenses/Apache-2.0)
[![kotlin](https://img.shields.io/github/languages/top/yanneckreiss/kconmapper?style=for-the-badge&color=blueviolet)](https://kotlinlang.org/)

<p align="center"> 
   <img height="200" src="https://user-images.githubusercontent.com/100080106/208970921-5bc67978-a04e-40a0-8fb9-cafaa2b1334e.png"/> 
</p>


# KConMapper (KCM)

The **Kotlin Constructor Mapper (KConMapper / KCM)** is a **Kotlin Symbol Processing (KSP)** plugin that can automatically 
generate extension functions to map variables of one class to the primary constructor of another class.

---

## How To Use The Plugin

Let's say you have a class `ExampleEntity`. A common use case is to have various **Data Transfer Object (DTO)** classes
that we want to convert into such an `ExampleEntity` class.

it is common for many of the DTO parameters to be the same as those in the corresponding entity class. 
As a result, we often find ourselves having to manually map the DTO to the target class, which can be a tedious and redundant task.

### Here comes the **KConMapper** into play.</h2>

Our entity might look like the following:

```kt
import java.time.ZonedDateTime
import java.util.*

data class ExampleEntity(
    val uid: UUID = UUID.randomUUID(),
    val name: String,
    val timestamp: ZonedDateTime
)
```

Now we want to have a DTO for the CRUD operations of update and create.

The **create** case might look like the following:

```kt
import java.time.ZonedDateTime

data class CreateExampleDTO(
    val name: String,
    val timestamp: ZonedDateTime
)
```

While the **create** case lacks the `UUID` because it's not created yet, the
**update** case will contain it:

```kt
import java.time.ZonedDateTime

data class UpdateExampleDTO(
    val uid: UUID,
    val name: String,
    val timestamp: ZonedDateTime
)
```

To create mapping functions, you simply need to add the `@KConMapper` annotation above the target class.

- The **target class**, `ExampleEntity`, is the class whose constructor serves as the schema for the mapping function.
- The **source classes**, `CreateExampleDTO` and `UpdateExampleDTO`, are the classes whose parameters we want to map to the constructor schema of the target class.

```kt
import java.time.ZonedDateTime
import java.util.*

@KConMapper(fromClasses = [CreateExampleDTO::class, UpdateExampleDTO::class])
data class ExampleEntity(
    val uid: UUID = UUID.randomUUID(),
    val name: String,
    val timestamp: ZonedDateTime
)
```

By executing the `kspKotlin` task, the respective mapping functions get automatically generated as extension functions
of the
source classes.

With these mapping functions, we can implement the following:

```kt
    // For the create case:
    val createExampleDTO = CreateExampleDTO()
    val exampleEntity: ExampleEntity = createExampleDTO.toExampleEntity()
    
    // And for the update case:
    val updateExampleDTO = UpdateExampleDTO()
    val exampleEntity: ExampleEntity = updateExampleDTO.toExampleEntity()
```

If we want to generate mapping functions from the annotated class to another target class, we can supply the `toClasses` argument:

```kt
import java.time.ZonedDateTime
import java.util.*

@KConMapper(toClasses = [CreateExampleDTO::class, UpdateExampleDTO::class])
data class ExampleEntity(
    val uid: UUID = UUID.randomUUID(),
    val name: String,
    val timestamp: ZonedDateTime
)
```

The code snippet from above would generate the following:
```kt
    val exampleEntity: ExampleEntity = ExampleEntity()
    
    // For the create case:
    val createExampleDTO: CreateExampleDTO = exampleEntity.toCreateExampleDTO()
    
    // And for the update case:
    val updateExampleDTO: UpdateExampleDTO = exampleEntity.toUpdateExampleDTO()
```

## Property name overriding

The **KConMapper** mapping function works by matching the names of the variables in the source class 
to the ones in the target class. If the names of the variables in the source class do not match 
the names in the target class, you can use the `@KConMapperProperty` annotation to specify which 
variables in the source class should be mapped to which variables in the target class.

For example:

```kotlin
@KConMapper(fromClasses = [CreateExampleDTO::class])
data class ExampleEntity(val name: String, val age: Int)

data class CreateExampleDTO(val fullName: String, val yearsOld: Int)
```
In this example, the fullName variable in the `CreateExampleDTO` class does 
not have a matching variable in the `ExampleEntity` class. 
To map the `fullName` variable to the `name` variable in the `ExampleEntity` class, 
we would use the `@KConMapperProperty` annotation like this:

```kotlin
@KConMapper(fromClasses = [CreateExampleDTO::class])
data class ExampleEntity(val name: String, val age: Int)

data class CreateExampleDTO(
@KConMapperProperty("name") val fullName: String,
@KConMapperProperty("age") val yearsOld: Int
)
```

With the `@KConMapperProperty` annotation in place, the `exampleMapper` function will map the fullName variable in the CreateExampleDTO class to the name variable in the ExampleEntity class and the yearsOld variable to the age variable when it is called.

---

## Setup
The following section describes the required setup to use the **KConMapper** KSP plugin in your project.

#### 1. Add the KSP (Kotlin Symbol Processing) plugin:

> Lookup the matching KSP version from the [official GitHub release page]("https://github.com/google/ksp/releases") 
of the KSP repository.
As an example: If you're using Kotlin version `1.8.0`, the latest KSP would be `1.8.0-1.0.8`.

<details open>
  <summary>groovy - build.gradle(:module-name)</summary>

```groovy
plugins {
    // Depends on your project's Kotlin version
    id 'com.google.devtools.ksp' version '1.8.0-1.0.8'
}
```
</details>

<details>
  <summary>kotlin - build.gradle.kts(:module-name)</summary>  

```kt
plugins {
    // Depends on your project's Kotlin version
    id("com.google.devtools.ksp") version "1.8.0-1.0.8"
}
```
</details>

#### 2. Dependency
Add the **KConMapper** plugin dependency:

<details open>
  <summary>groovy - build.gradle(:module-name)</summary>

```groovy
repositories {
    maven { url = 'https://jitpack.io' }    
}

dependencies {
    // ..
    implementation 'com.github.yanneckreiss:kconmapper:kconmapper-annotations:1.0.0-alpha03'
    ksp 'com.github.yanneckreiss:kconmapper:kconmapper-ksp:1.0.0-alpha03'
}
```
</details>

<details>
  <summary>kotlin - build.gradle.kts(:module-name)</summary>  

```kt
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // ..
    implementation("com.github.yanneckreiss:kconmapper:kconmapper-annotations:1.0.0-alpha03")
    ksp("com.github.yanneckreiss:kconmapper:kconmapper-ksp:1.0.0-alpha03")
}
```
</details>

#### 3. Add source sets
To be able to access the generated classes in your project, you need to add them to your 
`sourceSets`:
<details>
    <summary>Android project</summary>

```kt
android {
  kotlin.sourceSets.all {
      android.applicationVariants.all { variant ->
            kotlin.srcDir("build/generated/ksp/${variant.name}/kotlin")
      }
  }
}
 ```

</details>

<details>
<summary>Kotlin JVM or equal project</summary>

```kt
kotlin.sourceSets {
    getByName(name) {
         kotlin.srcDir("build/generated/ksp/$name/kotlin")
    }
}
 ```
</details>

---

License
=======
    Copyright 2022 Yanneck Reiß

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.