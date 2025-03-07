package com.group12.server.controller

import com.group12.server.dto.ActivationDTO
import com.group12.server.dto.RegistrationDTO
import com.group12.server.dto.TokenDTO
import com.group12.server.dto.UserDTO
import com.group12.server.service.impl.UserServiceImpl
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
class UserController(val userService: UserServiceImpl) {

    @PostMapping("/user/register")
    fun register(
        @RequestBody
        @Valid
        body: RegistrationDTO,
        br: BindingResult
    ) : ResponseEntity<ActivationDTO> {

        if (br.hasErrors() ||
            !userService.isValidPwd(body.password) ||
            !userService.isValidEmail(body.email) ||
            !userService.isValidNickname(body.nickname))
            return ResponseEntity(HttpStatus.BAD_REQUEST)

        val reg = RegistrationDTO(body.nickname, body.password, body.email)
        val res = userService.userReg(reg)
        return ResponseEntity(res, HttpStatus.ACCEPTED)
    }

    @PostMapping("/user/validate")
    fun validate(@RequestBody body: TokenDTO): ResponseEntity<UserDTO> {
        val tempUserDto = userService.completedReg(TokenDTO(body.provisional_id, body.activation_code))
        return if (tempUserDto == null)
            ResponseEntity(HttpStatus.NOT_FOUND)
        else
            ResponseEntity(tempUserDto, HttpStatus.CREATED)
    }
}