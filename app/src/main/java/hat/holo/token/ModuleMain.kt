package hat.holo.token

import android.annotation.SuppressLint
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.XResources
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.content.getSystemService
import dalvik.system.BaseDexClassLoader
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import hat.holo.token.models.AccountInfo
import hat.holo.token.models.DeviceInfo
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

    private fun showDialog(ctx: Context, text: String) {
        val duration = Toast.LENGTH_SHORT
        Toast.makeText(ctx, text, duration).show()
    }

    private fun copyCK(ctx: Context, accountInfo: AccountInfo, deviceInfo: DeviceInfo) = runCatching {
        val authStr = buildMap {
            put("ltuid", accountInfo.uid)
            put("ltoken", accountInfo.lToken)
            put("stuid", accountInfo.uid)
            put("mid", accountInfo.mid)
            put("stoken", accountInfo.sToken)
            put("x-rpc-device_id", deviceInfo.id)
            put("x-rpc-device_fp", deviceInfo.fingerprint)
        }.map { (k, v) -> "$k=$v" }.joinToString(";")
        val clip = ClipData.newPlainText(null, authStr)
        ctx.getSystemService<ClipboardManager>()!!.setPrimaryClip(clip)
    }.onFailure {
        showDialog(ctx, "复制失败")
    }.onSuccess {
        showDialog(ctx, "复制成功")
    }

    @SuppressLint("DiscouragedApi")
    override fun handleLoadPackage(lpparam: LoadPackageParam) = with(lpparam) pms@{
        if (packageName != targetPackageName) return
        with(classLoader) {
            val c0 = loadClass("com.mihoyo.hyperion.app.HyperionApplicationHelper")
            findAndHookMethod(c0, "initOnMainProcess", Application::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(p: MethodHookParam) {
                    val app = p.args[0] as Application
                    AppUtils.init(classLoader)
                    AccountManager.init(classLoader)
                    appendToClassPath(app.applicationContext)
                }
            })
            val fragmentKlass = loadClass("com.mihoyo.hyperion.user_profile.UserProfileFragment")
            findAndHookMethod(fragmentKlass, "onViewCreated", android.view.View::class.java, android.os.Bundle::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(p: MethodHookParam) {
                    val fragmentBinding = p.thisObject.invokeMethod<Any>("getBinding")
                    val linearLayout = fragmentBinding.visitField<LinearLayout>("b")
                    val ctx = linearLayout.context
                    val tokenBtn = ImageView(ctx)
                    tokenBtn.id = XResources.getFakeResId("getTokenIv")
                    tokenBtn.setImageDrawable(Res.iconToken)
                    val size = Dimension.convertDpToPixel(32f, ctx).roundToInt()
                    tokenBtn.layoutParams = ViewGroup.LayoutParams(size, size)
                    tokenBtn.setPadding(20, 6, 0, 6)
                    tokenBtn.scaleType = ImageView.ScaleType.FIT_XY
                    tokenBtn.setOnClickListener {
                        if (AccountManager.isLogin) {
                            DeviceManager.init(classLoader, ctx)
                            if (isPatch && false) {
                                val intent = Intent(ctx, LoaderActivity::class.java)
                                intent.putExtra("accountInfo", AccountManager.accountInfo)
                                intent.putExtra("deviceInfo", DeviceManager.deviceInfo)
                                intent.putExtra("dexPath", modulePath)
                                ctx.startActivity(intent)
                            } else {
                                val intent = Intent()
                                intent.setClassName("hat.holo.token", "hat.holo.token.TokenActivity")
                                intent.putExtra("accountInfo", AccountManager.accountInfo)
                                intent.putExtra("deviceInfo", DeviceManager.deviceInfo)
                                try {
                                    ctx.startActivity(intent)
                                } catch (_: ActivityNotFoundException) {
                                    copyCK(ctx, AccountManager.accountInfo, DeviceManager.deviceInfo)
                                }
                            }
                        } else {
                            AppUtils.showToast("未登录")
                        }
                    }
                    val scanButton = fragmentBinding.visitField<ImageView>("w")
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
