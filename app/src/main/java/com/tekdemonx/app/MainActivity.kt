package com.tekdemonx.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val site = "https://x.tekdemonx.workers.dev"
    private val executor = Executors.newSingleThreadExecutor()
    private val prefs by lazy { getSharedPreferences("pubguc_app", MODE_PRIVATE) }

    private lateinit var root: FrameLayout
    private var lang = "tr"
    private var visitedTikTok = false
    private var visitedYouTube = false
    private var currentWebView: WebView? = null

    private val gold = Color.rgb(255, 196, 0)
    private val green = Color.rgb(89, 255, 62)
    private val bg = Color.rgb(5, 7, 6)
    private val panel = Color.rgb(15, 18, 16)
    private val field = Color.rgb(24, 28, 25)
    private val muted = Color.rgb(165, 172, 167)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CookieManager.getInstance().setAcceptCookie(true)

        root = FrameLayout(this)
        root.setBackgroundColor(bg)
        setContentView(root)

        if (prefs.getBoolean("app_logged_in", false)) {
            checkSession()
        } else {
            showAuth()
        }
    }

    private fun checkSession() {
        showCenterText("PUBGUC")
        executor.execute {
            val ok = try {
                val conn = URL("$site/api/auth/me").openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/json")
                val cookie = CookieManager.getInstance().getCookie(site)
                if (!cookie.isNullOrBlank()) conn.setRequestProperty("Cookie", cookie)
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                conn.responseCode == 200
            } catch (_: Exception) {
                false
            }

            runOnUiThread {
                if (ok) {
                    showMainWebView(true)
                } else {
                    prefs.edit().putBoolean("app_logged_in", false).apply()
                    showAuth()
                }
            }
        }
    }

    private fun showCenterText(text: String) {
        currentWebView = null
        root.removeAllViews()

        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        wrap.addView(TextView(this).apply {
            this.text = text
            setTextColor(gold)
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
        })

        wrap.addView(TextView(this).apply {
            text = "by TEKDEMONX"
            setTextColor(green)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, dp(5), 0, 0)
        })

        root.addView(wrap, FrameLayout.LayoutParams(-1, -1))
    }

    private fun showAuth(startRegister: Boolean = false) {
        currentWebView = null
        root.removeAllViews()

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(bg)
        }

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(24), dp(18), dp(30))
        }

        scroll.addView(page)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        // Top language row
        val langRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
        }
        val trBtn = smallChoiceButton("TR", lang == "tr")
        val enBtn = smallChoiceButton("EN", lang == "en")
        langRow.addView(trBtn)
        langRow.addView(space(dp(8), 1))
        langRow.addView(enBtn)
        page.addView(langRow, full())

        // Brand
        page.addView(TextView(this).apply {
            text = "PUBGUC"
            setTextColor(gold)
            textSize = 42f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
            setPadding(0, dp(22), 0, 0)
        }, full())

        page.addView(TextView(this).apply {
            text = "by TEKDEMONX"
            setTextColor(green)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            letterSpacing = 0.10f
            setPadding(0, dp(2), 0, dp(14))
        }, full())

        val heroText = TextView(this).apply {
            text = if (lang == "tr") "UC kazanmak için giriş yap" else "Login to earn UC"
            setTextColor(Color.WHITE)
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        page.addView(heroText, full())

        val heroSub = TextView(this).apply {
            text = if (lang == "tr")
                "Hesabına giriş yap, görevleri keşfet ve topluluğa katıl."
            else
                "Sign in, discover missions and join the community."
            setTextColor(muted)
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(7), dp(10), dp(20))
        }
        page.addView(heroSub, full())

        // Mini feature chips
        val featureRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        featureRow.addView(featureChip(if (lang == "tr") "GÜVENLİ" else "SECURE"), weight())
        featureRow.addView(space(dp(8), 1))
        featureRow.addView(featureChip(if (lang == "tr") "HIZLI" else "FAST"), weight())
        featureRow.addView(space(dp(8), 1))
        featureRow.addView(featureChip("PUBGUC"), weight())
        page.addView(featureRow, full())

        page.addView(space(1, dp(18)))

        // Main premium card
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = rounded(panel, gold, 1, 22)
        }
        page.addView(card, full())

        val switchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = rounded(Color.rgb(9, 11, 10), Color.rgb(48, 54, 49), 1, 16)
            setPadding(dp(4), dp(4), dp(4), dp(4))
        }
        val loginTab = tabButton(if (lang == "tr") "GİRİŞ YAP" else "LOGIN", !startRegister)
        val regTab = tabButton(if (lang == "tr") "KAYIT OL" else "REGISTER", startRegister)
        switchRow.addView(loginTab, weight())
        switchRow.addView(regTab, weight())
        card.addView(switchRow, full())

        val formHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(14), 0, 0)
        }
        card.addView(formHost, full())

        var registerMode = startRegister

        fun refreshTabs() {
            loginTab.background = rounded(
                if (!registerMode) gold else Color.TRANSPARENT,
                Color.TRANSPARENT, 0, 12
            )
            loginTab.setTextColor(if (!registerMode) Color.BLACK else Color.WHITE)

            regTab.background = rounded(
                if (registerMode) green else Color.TRANSPARENT,
                Color.TRANSPARENT, 0, 12
            )
            regTab.setTextColor(if (registerMode) Color.BLACK else Color.WHITE)
        }

        fun renderLogin() {
            registerMode = false
            refreshTabs()
            formHost.removeAllViews()

            formHost.addView(sectionTitle(if (lang == "tr") "Hesabına giriş yap" else "Sign in to your account"))

            val user = input(if (lang == "tr") "Kullanıcı adı" else "Username")
            val pass = input(if (lang == "tr") "Şifre" else "Password", true)
            val msg = notice()
            val submit = actionButton(if (lang == "tr") "GİRİŞ YAP" else "LOGIN", gold)

            formHost.addView(user)
            formHost.addView(pass)
            formHost.addView(space(1, dp(6)))
            formHost.addView(submit, full())
            formHost.addView(msg)

            submit.setOnClickListener {
                val username = user.text.toString().trim()
                val password = pass.text.toString()

                if (username.isBlank() || password.isBlank()) {
                    msg.text = if (lang == "tr") "Kullanıcı adı ve şifreyi doldur." else "Enter username and password."
                    return@setOnClickListener
                }

                val body = JSONObject()
                    .put("username", username)
                    .put("password", password)

                postJson("/api/auth/login", body, msg) { success ->
                    if (success) {
                        prefs.edit().putBoolean("app_logged_in", true).apply()
                        showMainWebView(true)
                    }
                }
            }
        }

        fun renderRegister() {
            registerMode = true
            refreshTabs()
            formHost.removeAllViews()

            formHost.addView(sectionTitle(if (lang == "tr") "Yeni hesap oluştur" else "Create a new account"))

            val user = input(if (lang == "tr") "Kullanıcı adı" else "Username")
            val age = input(if (lang == "tr") "Yaş" else "Age").apply {
                inputType = InputType.TYPE_CLASS_NUMBER
            }
            val pass = input(if (lang == "tr") "Şifre" else "Password", true)
            val pubg = input("PUBG ID")
            val kd = input("K/D").apply {
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            val gender = Spinner(this)
            val values = if (lang == "tr")
                listOf("Cinsiyet seç", "Kadın", "Erkek")
            else
                listOf("Select gender", "Female", "Male")
            gender.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, values)
            gender.background = rounded(field, Color.rgb(60, 65, 61), 1, 14)
            gender.setPadding(dp(12), dp(4), dp(12), dp(4))
            gender.layoutParams = full().apply {
                setMargins(0, dp(6), 0, dp(6))
                height = dp(54)
            }

            val tiktokOpen = outlineButton("TikTok • @tekdemonx2017")
            val tiktokCheck = CheckBox(this).apply {
                text = if (lang == "tr") "TikTok hesabını takip ettim" else "I followed the TikTok account"
                setTextColor(Color.WHITE)
            }

            val youtubeOpen = outlineButton("YouTube • TEKDEMONX")
            val youtubeCheck = CheckBox(this).apply {
                text = if (lang == "tr") "YouTube kanalına abone oldum" else "I subscribed to the YouTube channel"
                setTextColor(Color.WHITE)
            }

            val msg = notice()
            val submit = actionButton(if (lang == "tr") "KAYIT OL" else "REGISTER", green)

            formHost.addView(user)
            formHost.addView(age)
            formHost.addView(pass)
            formHost.addView(pubg)
            formHost.addView(gender)
            formHost.addView(kd)

            formHost.addView(TextView(this).apply {
                text = if (lang == "tr")
                    "Kayıt için sosyal hesapları aç ve onayla."
                else
                    "Open and confirm the social accounts to register."
                setTextColor(muted)
                textSize = 13f
                setPadding(0, dp(10), 0, dp(4))
            })

            formHost.addView(tiktokOpen, full())
            formHost.addView(tiktokCheck)
            formHost.addView(youtubeOpen, full())
            formHost.addView(youtubeCheck)
            formHost.addView(space(1, dp(5)))
            formHost.addView(submit, full())
            formHost.addView(msg)

            tiktokOpen.setOnClickListener {
                visitedTikTok = true
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@tekdemonx2017")))
            }

            youtubeOpen.setOnClickListener {
                visitedYouTube = true
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/channel/UCXnDHZ_87UKjp9sKlfGDcgQ")))
            }

            submit.setOnClickListener {
                if (!visitedTikTok || !visitedYouTube) {
                    msg.text = if (lang == "tr")
                        "Önce TikTok ve YouTube bağlantılarını aç."
                    else
                        "Open TikTok and YouTube first."
                    return@setOnClickListener
                }

                if (!tiktokCheck.isChecked || !youtubeCheck.isChecked) {
                    msg.text = if (lang == "tr")
                        "Takip ve abonelik onaylarını işaretle."
                    else
                        "Confirm follow and subscription."
                    return@setOnClickListener
                }

                val genderValue = when (gender.selectedItemPosition) {
                    1 -> "female"
                    2 -> "male"
                    else -> ""
                }

                val body = JSONObject()
                    .put("username", user.text.toString().trim())
                    .put("age", age.text.toString().toIntOrNull() ?: 0)
                    .put("password", pass.text.toString())
                    .put("pubgId", pubg.text.toString().trim())
                    .put("gender", genderValue)
                    .put("kd", kd.text.toString().toDoubleOrNull() ?: 0.0)
                    .put("tiktokFollowed", true)
                    .put("youtubeSubscribed", true)

                postJson("/api/auth/register", body, msg) { success ->
                    if (success) {
                        Toast.makeText(
                            this,
                            if (lang == "tr")
                                "Kayıt tamamlandı. Şimdi giriş yap."
                            else
                                "Registration complete. Now log in.",
                            Toast.LENGTH_LONG
                        ).show()
                        renderLogin()
                    }
                }
            }
        }

        loginTab.setOnClickListener { renderLogin() }
        regTab.setOnClickListener { renderRegister() }

        // Browse website
        val browse = outlineButton(
            if (lang == "tr") "SİTEYE GÖZ AT" else "BROWSE WEBSITE"
        )
        page.addView(space(1, dp(14)))
        page.addView(browse, full())

        page.addView(TextView(this).apply {
            text = if (lang == "tr")
                "Giriş yapmadan siteyi inceleyebilirsin. Üyelik gerektiren bölümler yine hesabını isteyecek."
            else
                "You can browse the website without signing in. Member-only sections will still require an account."
            setTextColor(muted)
            textSize = 12f
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), 0)
        }, full())

        browse.setOnClickListener {
            showMainWebView(false)
        }

        trBtn.setOnClickListener {
            lang = "tr"
            showAuth(registerMode)
        }

        enBtn.setOnClickListener {
            lang = "en"
            showAuth(registerMode)
        }

        if (startRegister) renderRegister() else renderLogin()
    }

    private fun postJson(
        path: String,
        body: JSONObject,
        msg: TextView,
        done: (Boolean) -> Unit
    ) {
        msg.text = if (lang == "tr") "Bekle..." else "Please wait..."

        executor.execute {
            var success = false
            var message = ""

            try {
                val conn = URL(site + path).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("Accept", "application/json")
                conn.connectTimeout = 12000
                conn.readTimeout = 12000

                val existing = CookieManager.getInstance().getCookie(site)
                if (!existing.isNullOrBlank()) {
                    conn.setRequestProperty("Cookie", existing)
                }

                conn.outputStream.use {
                    it.write(body.toString().toByteArray(Charsets.UTF_8))
                }

                val setCookies = conn.headerFields["Set-Cookie"].orEmpty()
                for (cookie in setCookies) {
                    CookieManager.getInstance().setCookie(site, cookie)
                }
                CookieManager.getInstance().flush()

                val stream =
                    if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream

                val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val json = try {
                    JSONObject(text)
                } catch (_: Exception) {
                    JSONObject()
                }

                success = json.optBoolean("ok", false)

                message = if (success) {
                    if (lang == "tr") "Tamamlandı." else "Done."
                } else {
                    json.optString(
                        "error",
                        if (lang == "tr") "İşlem başarısız." else "Request failed."
                    )
                }
            } catch (e: Exception) {
                message =
                    if (lang == "tr")
                        "Bağlantı hatası: ${e.message}"
                    else
                        "Connection error: ${e.message}"
            }

            runOnUiThread {
                msg.text = message
                done(success)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showMainWebView(appMode: Boolean) {
        root.removeAllViews()

        val web = WebView(this)
        currentWebView = web

        val settings = web.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.mediaPlaybackRequiresUserGesture = true
        settings.userAgentString = settings.userAgentString + " PUBGUC-Android/1.0"

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)

        web.webChromeClient = WebChromeClient()

        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                if (appMode) injectAppMode(view)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
        }

        root.addView(web, FrameLayout.LayoutParams(-1, -1))
        web.loadUrl(site)
    }

    private fun injectAppMode(web: WebView) {
        val js = """
            (function(){
              function hideAuth(){
                ['navLoginBtn','navRegisterBtn','loginView','registerView'].forEach(function(id){
                  var el=document.getElementById(id);
                  if(el) el.style.setProperty('display','none','important');
                });
              }
              hideAuth();
              var mo=new MutationObserver(hideAuth);
              if(document.body){
                mo.observe(document.body,{
                  childList:true,
                  subtree:true,
                  attributes:true
                });
              }
              window.__PUBGUC_APP__=true;
            })();
        """.trimIndent()

        web.evaluateJavascript(js, null)
    }

    override fun onBackPressed() {
        val web = currentWebView
        if (web != null) {
            if (web.canGoBack()) {
                web.goBack()
            } else {
                showAuth()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun input(hintText: String, password: Boolean = false): EditText {
        return EditText(this).apply {
            hint = hintText
            setHintTextColor(Color.rgb(130, 137, 132))
            setTextColor(Color.WHITE)
            textSize = 15f
            singleLine = true
            background = rounded(field, Color.rgb(55, 62, 57), 1, 14)
            setPadding(dp(15), dp(13), dp(15), dp(13))

            if (password) {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            layoutParams = full().apply {
                setMargins(0, dp(6), 0, dp(6))
                height = dp(54)
            }
        }
    }

    private fun sectionTitle(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(2), 0, dp(8))
        }
    }

    private fun notice(): TextView {
        return TextView(this).apply {
            setTextColor(Color.LTGRAY)
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(2))
        }
    }

    private fun featureChip(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(green)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(7), dp(9), dp(7), dp(9))
            background = rounded(
                Color.rgb(12, 18, 13),
                Color.rgb(58, 103, 58),
                1,
                12
            )
        }
    }

    private fun smallChoiceButton(text: String, active: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(if (active) Color.BLACK else Color.WHITE)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(15), dp(9), dp(15), dp(9))
            background = rounded(
                if (active) green else Color.rgb(18, 21, 19),
                if (active) green else Color.rgb(60, 66, 61),
                1,
                22
            )
        }
    }

    private fun tabButton(text: String, active: Boolean): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(if (active) Color.BLACK else Color.WHITE)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(12), dp(8), dp(12))
            background = rounded(
                if (active) gold else Color.TRANSPARENT,
                Color.TRANSPARENT,
                0,
                12
            )
        }
    }

    private fun actionButton(text: String, buttonColor: Int): Button {
        return Button(this).apply {
            this.text = text
            setTextColor(Color.BLACK)
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            background = rounded(buttonColor, buttonColor, 0, 15)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            stateListAnimator = null
            minHeight = dp(54)
        }
    }

    private fun outlineButton(text: String): Button {
        return Button(this).apply {
            this.text = text
            setTextColor(Color.WHITE)
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            background = rounded(
                Color.rgb(13, 16, 14),
                Color.rgb(76, 86, 79),
                1,
                15
            )
            setPadding(dp(14), dp(11), dp(14), dp(11))
            stateListAnimator = null
            minHeight = dp(52)
        }
    }

    private fun rounded(
        fillColor: Int,
        strokeColor: Int,
        strokeWidthDp: Int,
        radiusDp: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fillColor)
            cornerRadius = dp(radiusDp).toFloat()
            if (strokeWidthDp > 0) {
                setStroke(dp(strokeWidthDp), strokeColor)
            }
        }
    }

    private fun full(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun weight(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
    }

    private fun space(w: Int, h: Int): Space {
        return Space(this).apply {
            layoutParams = LinearLayout.LayoutParams(w, h)
        }
    }

    private fun dp(v: Int): Int =
        (v * resources.displayMetrics.density).toInt()
}
