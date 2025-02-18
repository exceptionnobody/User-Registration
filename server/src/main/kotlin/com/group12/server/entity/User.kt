package com.group12.server.entity

import com.group12.server.dto.UserDTO
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    var email: String,
    @Column(nullable = false, unique = true)
    var nickname: String,
    @Column(nullable = false)
    var password: String,
    @Column(nullable = false)
    var validated: Boolean = false,
    @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST])
    @OnDelete(action = OnDeleteAction.CASCADE)
    var roles : MutableSet<RoleEntity> = mutableSetOf(),
) {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.AUTO)
    var userId: Long? = null
}
fun User.toDTO() = UserDTO(userId!!, nickname, email)
