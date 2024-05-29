package hat.holo.token.utils

import hat.holo.token.models.AccountInfo

@Suppress("MemberVisibilityCanBePrivate", "unused")
object AccountManagerHoyo {

    private lateinit var instPorte: Any
    private lateinit var clzPorte: Class<*>
    private lateinit var clzAccount: Class<*>
    private lateinit var clzToken: Class<*>

    fun init(cl: ClassLoader) {
        clzPorte = cl.loadClass("com.mihoyo.platform.account.oversea.sdk.manager.AccountManager")
        clzAccount = cl.loadClass("com.mihoyo.platform.account.oversea.sdk.domain.model.Account")
        clzToken = cl.loadClass("com.mihoyo.platform.account.oversea.sdk.domain.model.Token")
        instPorte = clzPorte.getDeclaredField("INSTANCE").get(null)!!
    }

    private fun loginCurrentAccount() = clzPorte.getDeclaredMethod("getCurrentAccount").invoke(instPorte)

    val isLogin get() = if (loginCurrentAccount() == null) false else !uid.isNullOrEmpty() && !sToken.isNullOrEmpty()

    val mid get() = clzAccount.getDeclaredMethod("getMid").invoke(loginCurrentAccount()) as String?
    val uid get() = clzAccount.getDeclaredMethod("getAid").invoke(loginCurrentAccount()) as String?
    val lToken get() = clzToken.getDeclaredMethod("getTokenStr").invoke(lTokenIns) as String?
    val lTokenIns: Any? get() = clzAccount.getDeclaredMethod("getLToken").invoke(loginCurrentAccount())
    val sToken get() = clzToken.getDeclaredMethod("getTokenStr").invoke(sTokenIns) as String?
    val sTokenIns: Any? get() = clzAccount.getDeclaredMethod("getSToken").invoke(loginCurrentAccount())

    val accountInfo get() = AccountInfo(mid!!, uid!!, lToken!!, sToken!!)

}
