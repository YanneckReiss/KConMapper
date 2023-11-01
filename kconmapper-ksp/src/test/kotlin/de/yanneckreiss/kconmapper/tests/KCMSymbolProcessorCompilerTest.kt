package de.yanneckreiss.kconmapper.tests

import com.github.yanneckreiss.kconmapper.annotations.KConMapper
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import de.yanneckreiss.kconmapper.base.KSPCompilerTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.reflect.Method

class KCMSymbolProcessorCompilerTest : KSPCompilerTest() {

    @Test
    fun `Test matching parameters produce correct mapping extension functions`() {

        val userEntity = SourceFile.kotlin(
            "UserEntity.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.dto.CreateUserDTO
                    import foo.bar.dto.UpdateUserDTO
                    
                    @${KConMapper::class.simpleName}(
                        fromClasses = [CreateUserDTO::class, UpdateUserDTO::class],
                        toClasses = [CreateUserDTO::class, UpdateUserDTO::class]
                    )
                    data class UserEntity(
                        val name: String,
                        val address: String,
                    )
        """.trimIndent()
        )

        val createUserDTO = SourceFile.kotlin(
            "CreateUserDTO.kt", """
                package foo.bar.dto
                
                data class CreateUserDTO(
                    val name: String,
                    val address: String
)
        """.trimIndent()
        )

        val updateUserDTOSource = SourceFile.kotlin(
            "UpdateUserDTO.kt", """
        package foo.bar.dto
        
        data class UpdateUserDTO(
            val name: String,
            val address: String,
        )
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntity, createUserDTO, updateUserDTOSource)

        val generatedFile: Class<*> = result.loadGeneratedClass("UserEntityKConMapperExtensionsKt")

        val declaredFunctions: Array<Method> = generatedFile.declaredMethods
        assertTrue(declaredFunctions.any { it.name == "toCreateUserDTO" })
        assertTrue(declaredFunctions.any { it.name == "toUpdateUserDTO" })
        assertTrue(declaredFunctions.any { it.name == "toUserEntity" })
        assertTrue(declaredFunctions.any { it.name == "toUserEntity" })
    }

    @Test
    fun `Test parameters that don't match will be added as required function parameters`() {

        val userEntity = SourceFile.kotlin(
            "UserEntity.kt", """
                    package foo.bar.db
                    
                    import ${KConMapper::class.qualifiedName}
                    import foo.bar.dto.CreateUserDTO
                    
                    @${KConMapper::class.simpleName}(
                        fromClasses = [CreateUserDTO::class],
                        toClasses = [CreateUserDTO::class]
                    )
                    data class UserEntity(
                        val name: String,
                        val address: String,
                    )
        """.trimIndent()
        )

        val createUserDTO = SourceFile.kotlin(
            "CreateUserDTO.kt", """
                package foo.bar.dto
                
                data class CreateUserDTO(
                    val name: String,
                    val lastName: String,
                    val address: String,
        )
        """.trimIndent()
        )

        val result: KotlinCompilation.Result = compileSource(userEntity, createUserDTO)

        val generatedFile: Class<*> = result.loadGeneratedClass("UserEntityKConMapperExtensionsKt")

        val declaredFunctions: Array<Method> = generatedFile.declaredMethods

        assertTrue(declaredFunctions.any { it.name == "toCreateUserDTO" })
        assertTrue(declaredFunctions.any { it.name == "toUserEntity" })

        val toCreateUserDTOFunction = declaredFunctions.first { it.name == "toCreateUserDTO" }
        assertTrue(toCreateUserDTOFunction.parameterTypes.contains(String::class.java))

        val toUserEntityFunction = declaredFunctions.first { it.name == "toUserEntity" }
        assertFalse(toUserEntityFunction.parameterTypes.contains(String::class.java))
    }
}
