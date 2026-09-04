package com.tekdemonx.app

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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

    private lateinit var root: FrameLayout
    private var lang = "tr"
    private var visitedTikTok = false
    private var visitedYouTube = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CookieManager.getInstance().setAcceptCookie(true)
        root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(5, 5, 5))
        setContentView(root)
        showAuth()
    }

    private fun checkSession() {
        showCenterText("TEKDEMONX")
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
                if (ok) showMainWebView() else showAuth()
            }
        }
    }

    private fun showCenterText(text: String) {
        root.removeAllViews()
        val t = TextView(this)
        t.text = text
        t.setTextColor(Color.rgb(255, 215, 0))
        t.textSize = 28f
        t.gravity = Gravity.CENTER
        root.addView(t, FrameLayout.LayoutParams(-1, -1))
    }

    private fun showAuth() {
        root.removeAllViews()

        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(28), dp(24), dp(28))
        }
        scroll.addView(box)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        box.addView(title("TEKDEMONX"))

        val langRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val tr = goldButton("TR")
        val en = goldButton("EN")
        langRow.addView(tr)
        langRow.addView(en)
        box.addView(langRow)

        val switchRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val loginTab = darkButton("LOGIN")
        val regTab = darkButton("REGISTER")
        switchRow.addView(loginTab)
        switchRow.addView(regTab)
        box.addView(switchRow)

        val formHost = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(14), 0, 0)
        }
        box.addView(formHost, LinearLayout.LayoutParams(-1, -2))

        fun renderLogin() {
            formHost.removeAllViews()
            val user = input(if (lang == "tr") "Kullanıcı adı" else "Username")
            val pass = input(if (lang == "tr") "Şifre" else "Password", true)
            val msg = notice()
            val submit = goldButton(if (lang == "tr") "GİRİŞ" else "LOGIN")
            formHost.addView(user)
            formHost.addView(pass)
            formHost.addView(submit, full())
            formHost.addView(msg)

            submit.setOnClickListener {
                val body = JSONObject()
                    .put("username", user.text.toString().trim())
                    .put("password", pass.text.toString())
                postJson("/api/auth/login", body, msg) { success ->
                    if (success) showMainWebView()
                }
            }
        }

        fun renderRegister() {
            formHost.removeAllViews()
            val user = input(if (lang == "tr") "Kullanıcı adı" else "Username")
            val age = input(if (lang == "tr") "Yaş" else "Age")
            val pass = input(if (lang == "tr") "Şifre" else "Password", true)
            val pubg = input("PUBG ID")
            val kd = input("K/D")

            val gender = Spinner(this)
            val values = if (lang == "tr")
                listOf("Cinsiyet", "Kadın", "Erkek")
            else
                listOf("Gender", "Female", "Male")
            gender.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, values)

            val tiktokOpen = darkButton("TEKDEMONX TIKTOK")
            val tiktokCheck = CheckBox(this).apply {
                text = if (lang == "tr") "Takip ettim" else "I followed"
                setTextColor(Color.WHITE)
            }
            val youtubeOpen = darkButton("TEKDEMONX YOUTUBE")
            val youtubeCheck = CheckBox(this).apply {
                text = if (lang == "tr") "Abone oldum" else "I subscribed"
                setTextColor(Color.WHITE)
            }
            val msg = notice()
            val submit = goldButton(if (lang == "tr") "KAYIT OL" else "REGISTER")

            formHost.addView(user)
            formHost.addView(age)
            formHost.addView(pass)
            formHost.addView(pubg)
            formHost.addView(gender, full())
            formHost.addView(kd)
            formHost.addView(tiktokOpen, full())
            formHost.addView(tiktokCheck)
            formHost.addView(youtubeOpen, full())
            formHost.addView(youtubeCheck)
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
                            if (lang == "tr") "Kayıt tamamlandı. Şimdi giriş yap." else "Registration complete. Now log in.",
                            Toast.LENGTH_LONG
                        ).show()
                        renderLogin()
                    }
                }
            }
        }

        tr.setOnClickListener { lang = "tr"; renderLogin() }
        en.setOnClickListener { lang = "en"; renderLogin() }
        loginTab.setOnClickListener { renderLogin() }
        regTab.setOnClickListener { renderRegister() }

        renderLogin()
    }

    private fun postJson(path: String, body: JSONObject, msg: TextView, done: (Boolean) -> Unit) {
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
                if (!existing.isNullOrBlank()) conn.setRequestProperty("Cookie", existing)

                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

                val setCookies = conn.headerFields["Set-Cookie"].orEmpty()
                for (cookie in setCookies) {
                    CookieManager.getInstance().setCookie(site, cookie)
                }
                CookieManager.getInstance().flush()

                val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                val json = try { JSONObject(text) } catch (_: Exception) { JSONObject() }

                success = json.optBoolean("ok", false)
                message = if (success) {
                    if (lang == "tr") "Tamamlandı." else "Done."
                } else {
                    json.optString("error", if (lang == "tr") "İşlem başarısız." else "Request failed.")
                }
            } catch (e: Exception) {
                message = if (lang == "tr") "Bağlantı hatası: ${e.message}" else "Connection error: ${e.message}"
            }

            runOnUiThread {
                msg.text = message
                done(success)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun showMainWebView() {
        root.removeAllViews()

        val web = WebView(this)
        val settings = web.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.mediaPlaybackRequiresUserGesture = true
        settings.userAgentString = settings.userAgentString + " TEKDEMONX-Android/1.0"

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true)

        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                injectAppMode(view)
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
              if(document.body) mo.observe(document.body,{childList:true,subtree:true,attributes:true});
              window.__TEKDEMONX_APP__=true;
            })();
        """.trimIndent()
        web.evaluateJavascript(js, null)
    }

    override fun onBackPressed() {
        val web = if (root.childCount > 0) root.getChildAt(0) as? WebView else null
        if (web != null && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    private fun input(hintText: String, password: Boolean = false): EditText {
        return EditText(this).apply {
            hint = hintText
            setHintTextColor(Color.rgb(150, 150, 150))
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(24, 24, 24))
            setPadding(dp(14), dp(12), dp(14), dp(12))
            if (password) inputType = 0x00000081
            layoutParams = full().apply {
                setMargins(0, dp(6), 0, dp(6))
            }
        }
    }

    private fun title(text: String): TextView = TextView(this).apply {
        this.text = text
        setTextColor(Color.rgb(255, 215, 0))
        textSize = 30f
        gravity = Gravity.CENTER
        setPadding(0, dp(10), 0, dp(18))
    }

    private fun notice(): TextView = TextView(this).apply {
        setTextColor(Color.LTGRAY)
        textSize = 14f
        setPadding(0, dp(10), 0, dp(10))
    }

    private fun goldButton(text: String): Button = Button(this).apply {
        this.text = text
        setTextColor(Color.BLACK)
        setBackgroundColor(Color.rgb(255, 215, 0))
        setPadding(dp(16), dp(10), dp(16), dp(10))
    }

    private fun darkButton(text: String): Button = Button(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.rgb(35, 35, 35))
        setPadding(dp(16), dp(10), dp(16), dp(10))
    }

    private fun full() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
