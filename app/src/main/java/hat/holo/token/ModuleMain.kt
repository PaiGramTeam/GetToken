package hat.holo.token

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.XResources
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.Keep
import dalvik.system.BaseDexClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import hat.holo.token.utils.*
import java.io.File
import kotlin.math.roundToInt

@Keep
class ModuleMain : IXposedHookLoadPackage, IXposedHookZygoteInit {

    private var isPatch = false
    private var modulePath = ""
    private val targetPackageName = "com.mihoyo.hyperion"

    // platform/libcore/+/refs/heads/master/dalvik/src/main/java/dalvik/system/DexPathList.java
    // platform/libcore/+/refs/heads/master/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
    @SuppressLint("DiscouragedPrivateApi")
    private fun ClassLoader.appendToClassPath(ctx: Context) {
        val zBaseDexClassLoader = BaseDexClassLoader::class.java
        val fPathList = zBaseDexClassLoader.getDeclaredField("pathList").setAccess()
        val oPathList = fPathList.get(this) // DexPathList
        val zDexPathList = oPathList.javaClass
        val mAddDexPath = zDexPathList.getDeclaredMethod("addDexPath", String::class.java, File::class.java)
        mAddDexPath.setAccess().invoke(oPathList, modulePath, ctx.cacheDir)
    }

    @SuppressLint("DiscouragedApi")
    override fun handleLoadPackage(lpparam: LoadPackageParam) = with(lpparam) pms@{
        if (packageName != targetPackageName) return
        with(classLoader) {
            val c0 = loadClass("com.mihoyo.hyperion.app.HyperionApplicationHelper")
            findAndHookMethod(c0, "initOnMainProcess", Application::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(p: MethodHookParam) {
                    AppUtils.init(classLoader)
                    AccountManager.init(classLoader)
                    val app = p.args[0] as Application
                    appendToClassPath(app.applicationContext)
                }
            })
            val u = loadClass("com.mihoyo.hyperion.user_profile.UserProfileFragment")
            findAndHookMethod(u, "onViewCreated", android.view.View::class.java, android.os.Bundle::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(p: MethodHookParam) {
                    val getBinding = u.getDeclaredMethod("getBinding")
                    getBinding.isAccessible = true
                    val bindingInstance = getBinding.invoke(p.thisObject)
                    val rng = getBinding.returnType  // FragmentUserProfileBinding.java
                    val getLinearLayout = rng.getDeclaredField("b")
                    getLinearLayout.isAccessible = true
                    val linearLayout = getLinearLayout.get(bindingInstance) as LinearLayout
                    val ctx = linearLayout.context
                    val getScanButton = rng.getDeclaredField("w")
                    getScanButton.isAccessible = true
                    val scanButton = getScanButton.get(bindingInstance) as ImageView

                    val tokenBtn = ImageView(ctx)
                    tokenBtn.id = XResources.getFakeResId("getTokenIv")
                    tokenBtn.setImageDrawable(Res.iconToken)
                    val size = Dimension.convertDpToPixel(32f, ctx).roundToInt()
                    tokenBtn.layoutParams = ViewGroup.LayoutParams(size, size)
                    tokenBtn.setPadding(10, 6, 0, 6)
                    tokenBtn.scaleType = ImageView.ScaleType.FIT_XY
                    tokenBtn.setOnClickListener {
                        if (AccountManager.isLogin) {
                            DeviceUtils.init(ctx)
                            if (isPatch) {
                                val intent = Intent(ctx, LoaderActivity::class.java)
                                intent.putExtra("accountInfo", AccountManager.accountInfo)
                                intent.putExtra("dexPath", modulePath)
                                intent.putExtra("deviceInfo", DeviceUtils.deviceInfo)
                                ctx.startActivity(intent)
                            } else {
                                val intent = Intent()
                                intent.setClassName("hat.holo.token", "hat.holo.token.TokenActivity")
                                intent.putExtra("accountInfo", AccountManager.accountInfo)
                                intent.putExtra("deviceInfo", DeviceUtils.deviceInfo)
                                ctx.startActivity(intent)
                            }
                        } else {
                            AppUtils.showToast("未登录")
                        }
                    }

                    linearLayout.addView(tokenBtn, linearLayout.indexOfChild(scanButton) + 1)
                    for (i in 0 until linearLayout.childCount) {
                        val view = linearLayout.getChildAt(i)
                        if (view.id == -1) view.id = XResources.getFakeResId("b5AaLhI6WDlkTMIrRA$i")
                    }
                }
            })
        }
        XposedBridge.log("Module initialized!")
    }

    override fun initZygote(params: IXposedHookZygoteInit.StartupParam) {
        modulePath = params.modulePath
        isPatch = modulePath.contains("lspatch")
    }
}
