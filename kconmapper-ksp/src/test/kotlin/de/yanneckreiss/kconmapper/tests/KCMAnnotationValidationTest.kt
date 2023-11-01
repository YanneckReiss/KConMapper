package de.yanneckreiss.kconmapper.tests

import com.github.yanneckreiss.kconmapper.annotations.KConMapper
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.yanneckreiss.kconmapper.base.KSPCompilerTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KCMAnnotationValidationTest : KSPCompilerTest() {

    private val sampleUserModel = SourceFile.kotlin(
        "UserModel.kt", """
                    package foo.bar.domain.model
                    
                    data class UserModel(
                        val name: String,
                        val address: String,
                        val age: Int,
                    )
        """.trimIndent()
    )

    @Test
    fun `Test annotation on data class is successful`() {

        val userEntityAsDataClass = SourceFile.kotlin(
            "UserEntity.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    @${KConMapper::class.simpleName}(
                        fromClasses = [UserModel::class],
                        toClasses = [UserModel::class]
                    )
                    data class UserEntity(
                        val id: Int,
                        val name: String,
                        val address: String,
                        val age: Int
                    )
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsDataClass, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `Test annotation on class is successful`() {
        val userEntityAsClass = SourceFile.kotlin(
            "UserEntity.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    @${KConMapper::class.simpleName}(
                        fromClasses = [UserModel::class],
                        toClasses = [UserModel::class]
                    )
                    class UserEntity(
                        val id: Int,
                        val name: String,
                        val address: String,
                        val age: Int
                    )
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsClass, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `Test annotation on interface fails`() {
        val userEntityAsInterface = SourceFile.kotlin(
            "UserEntity.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    @${KConMapper::class.simpleName}(
                        fromClasses = [UserModel::class],
                        toClasses = [UserModel::class]
                    )
                    interface UserEntity {
                        val id: Int
                        val name: String
                        val address: String
                        val age: Int
                    }
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsInterface, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }

    @Test
    fun `Test annotation on enum fails`() {
        val userEntityAsEnum = SourceFile.kotlin(
            "UserType.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    @${KConMapper::class.simpleName}(
                        fromClasses = [UserModel::class],
                        toClasses = [UserModel::class]
                    )
                    enum class UserType
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsEnum, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }

    @Test
    fun `Test annotation on object fails`() {
        val userEntityAsObject = SourceFile.kotlin(
            "UserEntity.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    @${KConMapper::class.simpleName}(
                        fromClasses = [UserModel::class],
                        toClasses = [UserModel::class]
                    )
                    object UserEntity {
                        val id: Int
                        val name: String
                        val address: String
                        val age: Int
                    }
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsObject, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }

    @Test
    fun `Test annotation on annotation fails`() {
        val userEntityAsAnnotation = SourceFile.kotlin(
            "UserAnnotation.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    @${KConMapper::class.simpleName}(
                        fromClasses = [UserModel::class],
                        toClasses = [UserModel::class]
                    )
                    annotation class UserAnnotation
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsAnnotation, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }

    @Test
    fun `Test annotation on sealed class fails`() {
        val userEntityAsSealedClass = SourceFile.kotlin(
            "UserEntity.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    @${KConMapper::class.simpleName}(
                        fromClasses = [UserModel::class],
                        toClasses = [UserModel::class]
                    )
                    sealed class UserEntity {
                        
                        data object LocalUser : UserEntity()
                    }
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsSealedClass, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }

    @Test
    fun `Test annotation on inner class fails`() {
        val userEntityAsInnerClass = SourceFile.kotlin(
            "UserEntity.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    class UserEntity {
                        @${KConMapper::class.simpleName}(
                            fromClasses = [UserModel::class],
                            toClasses = [UserModel::class]
                        )
                        inner class InnerUserEntity {
                            val id: Int
                            val name: String
                            val address: String
                            val age: Int
                        }
                    }
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsInnerClass, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }

    @Test
    fun `Test annotation on inner object fails`() {
        val userEntityAsInnerObject = SourceFile.kotlin(
            "UserEntity.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    class UserEntity {
                        @${KConMapper::class.simpleName}(
                            fromClasses = [UserModel::class],
                            toClasses = [UserModel::class]
                        )
                        inner object InnerUserEntity {
                            val id: Int
                            val name: String
                            val address: String
                            val age: Int
                        }
                    }
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsInnerObject, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }

    @Test
    fun `Test annotation on inner enum fails`() {
        val userEntityAsInnerEnum = SourceFile.kotlin(
            "UserEntity.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    class UserEntity {
                        @${KConMapper::class.simpleName}(
                            fromClasses = [UserModel::class],
                            toClasses = [UserModel::class]
                        )
                        inner enum class InnerUserEntity
                    }
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsInnerEnum, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }

    @Test
    fun `Test annotation on inner annotation fails`() {
        val userEntityAsInnerAnnotation = SourceFile.kotlin(
            "InnerUserAnnotation.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.domain.model.UserModel
                    
                    class UserEntity {
                        @${KConMapper::class.simpleName}(
                            fromClasses = [UserModel::class],
                            toClasses = [UserModel::class]
                        )
                        inner annotation class InnerUserAnnotation
                    }
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntityAsInnerAnnotation, sampleUserModel)

        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }
}
