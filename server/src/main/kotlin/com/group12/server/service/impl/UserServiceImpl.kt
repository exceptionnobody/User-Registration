package com.group12.server.service.impl

import com.group12.server.dto.ActivationDTO
import com.group12.server.dto.RegistrationDTO
import com.group12.server.dto.TokenDTO
import com.group12.server.dto.UserDTO
import com.group12.server.entity.Activation
import com.group12.server.entity.User
import com.group12.server.entity.toDTO
import com.group12.server.repository.ActivationRepository
import com.group12.server.repository.UserRepository
import com.group12.server.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserServiceImpl: UserService {

    private val activationCodeSize = 6
    private val charRange : CharRange = '0'..'9'

    @Autowired
    lateinit var emailService: EmailServiceImpl
    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var activationRepository: ActivationRepository

    // Returns true if the password is valid, false otherwise
    override fun isValidPwd(pwd: String) : Boolean{
        var hasUpper = false
        var hasLower = false
        var hasNumber = false
        var hasSpecial =false
        for(c in pwd) {
            if(c.isWhitespace())
                return false
            if(c.isDigit())
                hasNumber=true
            if(c.isUpperCase())
                hasUpper=true
            if(c.isLowerCase())
                hasLower=true
            if(!c.isLetterOrDigit())
                hasSpecial=true
        }
       return hasUpper &&  hasLower &&  hasNumber &&  hasSpecial
    }

    override fun isValidEmail(email: String) : Boolean {

        return !userRepository.existsByEmail(email)
    }

    override fun isValidNickname(nickname: String) : Boolean {

        return !userRepository.existsByNickname(nickname)
    }

    override fun isValidProvisionalId(provisionalId: String): Boolean {
        val reg = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
        return reg.matches(provisionalId)
    }

    override fun isValidActivationCode(activationCode: String) : Boolean {
        if (activationCode.length != 6)
            return false
        try {
            activationCode.toLong()
        } catch (e: NumberFormatException) {
            return false
        }
        return true
    }

    override fun newActivationCode() : String {
        return (1..activationCodeSize)
            .map { _ -> kotlin.random.Random.nextInt(0, 10) }
            .map(charRange::elementAt)
            .joinToString("")
    }

    override fun userReg(newUser: RegistrationDTO): ActivationDTO {
        val tempUser = User(newUser.email, newUser.nickname, newUser.password, false)
        val savedUser = userRepository.save(tempUser)
        val activationCode = newActivationCode()
        val tempActivation = Activation(savedUser, newUser.email, activationCode)
        val savedActivation = activationRepository.save(tempActivation)
        emailService.sendEmail(newUser.email, newUser.nickname, activationCode)
        return savedActivation.toDTO()
    }

    override fun completedReg(token: TokenDTO) : UserDTO? {
        if( !isValidProvisionalId(token.provisional_id) ) {
            return null
        }
        val activation = activationRepository.findById(UUID.fromString(token.provisional_id)).orElse(null)
        activation ?: return null
        if (activation.deadline.before(Date())) {
            activationRepository.deleteById(UUID.fromString(token.provisional_id))
            userRepository.deleteById(activation.user.userId!!)
            return null
        }
        if (!isValidActivationCode(token.activation_code) ||
            activation.activationCode != token.activation_code) {
            if (activation.attemptCounter == 1) {
                val userId = activation.user.userId
                activationRepository.deleteById(UUID.fromString(token.provisional_id))
                userRepository.deleteById(userId!!)
            } else {
                activation.attemptCounter--
                activationRepository.save(activation)
            }
            return null
        }
        val user = activation.user
        user.validated = true
        userRepository.save(user)
        activationRepository.deleteById(UUID.fromString(token.provisional_id))
        return user.toDTO()
    }
}
