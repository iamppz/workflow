package com.joyceworks.api.infrastructure.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.apache.shiro.SecurityUtils
import java.util.*

object JWTUtil {
    private const val EXPIRE_TIME = 365 * 24 * 60 * 60 * 1000L

    /**
     * 校验token是否正确
     *
     * @param token  密钥
     * @param secret 用户的密码
     * @return 是否正确
     */
    @JvmStatic
    fun verify(token: String?, userId: Long?, secret: String?): Boolean {
        return try {
            val algorithm = Algorithm.HMAC256(secret)
            val verifier = JWT.require(algorithm).withClaim("userId", userId).build()
            verifier.verify(token)
            true
        } catch (exception: Exception) {
            false
        }
    }

    @JvmStatic
    fun getUserId(token: String): Long? {
        val jwt = JWT.decode(token)
        val userId = jwt.getClaim("userId")
        return userId.asLong()
    }

    @JvmStatic
    fun getUserId(): Long {
        return getUserId(SecurityUtils.getSubject().principal.toString())!!
    }

    @JvmStatic
    val token: String
        get() = SecurityUtils.getSubject().principal.toString()

    @JvmStatic
    fun sign(userId: Long, secret: String): String {
        val date = Date(System.currentTimeMillis() + EXPIRE_TIME)
        val algorithm = Algorithm.HMAC256(secret)
        return JWT.create().withClaim("userId", userId).withExpiresAt(date).sign(algorithm)
    }
}