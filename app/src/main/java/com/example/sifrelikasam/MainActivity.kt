package com.example.sifrelikasam

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.sifrelikasam.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.StrictMath.abs
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database1 = Firebase.database
    private var clickCountGunlukSoru = 0
    private var count = 9
    private val handler1 = Handler(Looper.getMainLooper())
    private lateinit var viewPager2: ViewPager2
    private lateinit var handler: Handler
    private lateinit var imageList: ArrayList<Int>
    private lateinit var adapter: ImageAdapter
    private val GunlukSoruHandler: Long = 9999

    var databaseGunlukTıklama = FirebaseDatabase.getInstance().getReference("clicksGunlukSoru")
    var database = FirebaseDatabase.getInstance()
    val databaseReference = database.reference.child("GünlükSoru")

    var soru = ""
    var cevapA = ""
    var cevapB = ""
    var cevapC = ""
    var cevapD = ""
    var cevap = ""
    var sorularCount = 0
    var hangisoru = 1

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var database: FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference: DatabaseReference = database.reference.child("Users")
        val reference1: DatabaseReference = database.reference

        init()
        setUpTransformer()

        reference1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val realName: String = snapshot.child("Users").child("duyuru").value.toString()
                binding.txtDuyuru.text = realName

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        databaseGunlukTıklama.child("userClickCount").get().addOnSuccessListener {
            clickCountGunlukSoru = it.getValue(Int::class.java) ?: 0
            //textViewCount.text = "Clicks: $clickCount"
        }



        binding.btnGunluksoru.isEnabled=false
        auth = Firebase.auth
        val currentUser = auth.currentUser
        if (currentUser != null) {
            checkBanStatus(currentUser.uid)
            checkBanStatus1(currentUser.uid)
        } else {
            // Kullanıcı oturum açmamışsa oturum açma işlemini başlatın
            signInAnonymously()
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 2000)
            }
        })
    }

    private fun banUserForOneMinute(uid: String) {
        val banEndTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)
        //val banEndTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)
        val userRef = database1.getReference("usersbanGunlukSoru/$uid")
        userRef.child("banEndTime").setValue(banEndTime).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateBanStatus(uid)
            } else {
                binding.txtkalansure.text = "Günlük Sorunuz işlemi başarısız oldu."
            }
        }
    }

    private fun updateBanStatus(uid: String) {
        val userRef = database1.getReference("usersbanGunlukSoru/$uid")
        userRef.child("banEndTime").get().addOnSuccessListener { snapshot ->
            val banEndTime = snapshot.getValue(Long::class.java) ?: 0L
            if (banEndTime > System.currentTimeMillis()) {
                val remainingTime = banEndTime - System.currentTimeMillis()
                binding.btnGunluksoru.isEnabled = false  // Butonu devre dışı bırak

                /*object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsRemaining = millisUntilFinished / 1000
                        val hoursRemaining = secondsRemaining / 3600
                        val minutesRemaining = (secondsRemaining % 3600) / 60
                        val seconds = secondsRemaining % 60
                        binding.txtkalansure.text = "$hoursRemaining:$minutesRemaining:$seconds"
                    }*/
                object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsRemaining = millisUntilFinished / 1000
                        binding.txtkalansure.text = "$secondsRemaining"
                    }

                    override fun onFinish() {
                        binding.txtkalansure.text = "Günlük Sorunuz Hazır."
                        binding.btnGunluksoru.isEnabled = true  // Butonu yeniden etkinleştir
                    }
                }.start()
            } else {
                binding.txtkalansure.text = "Günlük Sorunuz Hazır."
                binding.btnGunluksoru.isEnabled = true  // Butonu etkinleştir
            }
        }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        checkBanStatus(it.uid)
                    }
                } else {
                    binding.txtkalansure.text = "Oturum açılamadı."
                    binding.txtAltin.text = "Oturum açılamadı."
                }
            }
    }

    private fun checkBanStatus(uid: String) {
        val userRef = database1.getReference("usersbanGunlukSoru/$uid")
        userRef.child("banEndTime").get().addOnSuccessListener { snapshot ->
            val banEndTime = snapshot.getValue(Long::class.java) ?: 0L
            if (banEndTime > System.currentTimeMillis()) {
                updateBanStatus(uid)
            } else {
                binding.txtkalansure.text = "Günlük Sorunuz Hazır."
                binding.btnGunluksoru.isEnabled = true
            }
        }
    }


    override fun onPause() {
        super.onPause()

        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()

        //handler.postDelayed(runnable , 2000)
    }

    private val runnable = Runnable {
        viewPager2.currentItem = viewPager2.currentItem + 1
    }

    private fun setUpTransformer(){
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(40))
        transformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }

        viewPager2.setPageTransformer(transformer)
    }

    private fun init(){
        viewPager2 = findViewById(R.id.viewPager)
        handler = Handler(Looper.myLooper()!!)
        imageList = ArrayList()

        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)
        imageList.add(R.drawable.intro1)
        imageList.add(R.drawable.intro2)
        imageList.add(R.drawable.intro3)

        adapter = ImageAdapter(imageList)
        viewPager2.adapter = adapter
        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER



        /*val imageList = listOf(
            R.drawable.intro1,
            R.drawable.intro3,
            R.drawable.intro2,

            // diğer resimler...
        )

        val adapter = ImageAdapter(imageList)
        binding.viewPager.adapter = adapter*/

        binding.btnAnasayfa1.visibility = View.VISIBLE



        binding.btnspin.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userRef = database.getReference("usersbanGunlukCark/${currentUser.uid}")
                userRef.child("banEndTime").get().addOnSuccessListener { snapshot ->
                    val banEndTime = snapshot.getValue(Long::class.java) ?: 0L
                    if (banEndTime > System.currentTimeMillis()) {
                        updateBanStatus1(currentUser.uid)
                    } else {
                        performWheelSpin(currentUser.uid)
                    }
                }
            } else {
                signInAnonymously()
            }
        }

        binding.btnProfil.setOnClickListener {
            intent = Intent(this, profilMenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.txtProfil.setOnClickListener {
            intent = Intent(this, profilMenuActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnKategori.setOnClickListener {
            intent = Intent(this, KategoriActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.txtKategori.setOnClickListener {
            intent = Intent(this, KategoriActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnGunluk.setOnClickListener {
            intent = Intent(this, GunlukBonusActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.txtGunluk.setOnClickListener {
            intent = Intent(this, GunlukBonusActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.space2.setOnClickListener {
            binding.layoutYan.visibility = View.VISIBLE
            binding.btnYolculukdag.visibility = View.VISIBLE
            binding.layoutGunluksoru.visibility = View.INVISIBLE
            binding.layoutGunlukCark.visibility = View.INVISIBLE
        }

        binding.space1.setOnClickListener {
            binding.layoutYan.visibility = View.VISIBLE
            binding.btnYolculukdag.visibility = View.VISIBLE
            binding.layoutGunluksoru.visibility = View.INVISIBLE
            binding.layoutGunlukCark.visibility = View.INVISIBLE
        }

        binding.viewPager.setOnClickListener {
            binding.layoutYan.visibility = View.VISIBLE
            binding.btnYolculukdag.visibility = View.VISIBLE
            binding.layoutGunluksoru.visibility = View.INVISIBLE
            binding.layoutGunlukCark.visibility = View.INVISIBLE
        }

        binding.txtDuyuru.setOnClickListener {
            binding.layoutYan.visibility = View.VISIBLE
            binding.btnYolculukdag.visibility = View.VISIBLE
            binding.layoutGunluksoru.visibility = View.INVISIBLE
            binding.layoutGunlukCark.visibility = View.INVISIBLE
            binding.btnGunluksoru.visibility = View.VISIBLE
        }


        binding.btnGunluksoru.setOnClickListener {
            clickCountGunlukSoru++
            //textViewCount.text = "Clicks: $clickCount"
            databaseGunlukTıklama.child("userClickCount").setValue(clickCountGunlukSoru)

            auth = Firebase.auth
            val currentUser = auth.currentUser
            if (currentUser != null) {
                checkBanStatus(currentUser.uid)
            } else {
                // Kullanıcı oturum açmamışsa oturum açma işlemini başlatın
                signInAnonymously()
            }
            currentUser?.let {
                banUserForOneMinute(it.uid)
            }

            val anim = ObjectAnimator.ofFloat(binding.layoutGunluksoru, "scaleX", 0f, 1f)
            anim.duration = 300 // Animasyon süresi (milisaniye cinsinden)
            anim.start()
            binding.layoutYan.visibility = View.INVISIBLE
            binding.btnYolculukdag.visibility = View.INVISIBLE
            binding.layoutGunluksoru.visibility = View.VISIBLE
            binding.btnGunluksoru.visibility = View.GONE
            showNumbers()
            Handler().postDelayed({
                binding.layoutYan.visibility = View.VISIBLE
                binding.btnYolculukdag.visibility = View.VISIBLE
                binding.layoutGunluksoru.visibility = View.INVISIBLE
                binding.layoutGunlukCark.visibility = View.INVISIBLE
                binding.btnGunluksoru.visibility = View.VISIBLE
            }, GunlukSoruHandler)
            gameLogic()
        }

        binding.btnYolculukdag.setOnClickListener {
            intent = Intent(this, YolculukDagActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSansCark.setOnClickListener {
            val anim = ObjectAnimator.ofFloat(binding.layoutGunlukCark, "scaleX", 0f, 1f)
            anim.duration = 300 // Animasyon süresi (milisaniye cinsinden)
            anim.start()
            binding.layoutYan.visibility = View.INVISIBLE
            binding.btnYolculukdag.visibility = View.INVISIBLE
            binding.layoutGunlukCark.visibility = View.VISIBLE
        }
    }

    private fun checkBanStatus1(uid: String) {
        val userRef = database.getReference("usersbanGunlukCark/$uid")
        userRef.child("banEndTime").get().addOnSuccessListener { snapshot ->
            val banEndTime = snapshot.getValue(Long::class.java) ?: 0L
            if (banEndTime > System.currentTimeMillis()) {
                updateBanStatus1(uid)
            } else {
                binding.txtAltin.text = "HEYCANLANDIM!"
                binding.btnspin.isEnabled = true
            }
        }
    }

    private fun updateBanStatus1(uid: String) {
        val userRef = database.getReference("usersbanGunlukCark/$uid")
        userRef.child("banEndTime").get().addOnSuccessListener { snapshot ->
            val banEndTime = snapshot.getValue(Long::class.java) ?: 0L
            if (banEndTime > System.currentTimeMillis()) {
                val remainingTime = banEndTime - System.currentTimeMillis()
                binding.btnspin.isEnabled = false  // Butonu devre dışı bırak

                object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val secondsRemaining = millisUntilFinished / 1000
                        val hoursRemaining = secondsRemaining / 3600
                        val minutesRemaining = (secondsRemaining % 3600) / 60
                        val seconds = secondsRemaining % 60
                        binding.btnspin.text = "Kalan süre: $hoursRemaining saat, $minutesRemaining dakika, $seconds saniye."
                    }

                    override fun onFinish() {
                        binding.txtAltin.text = "HEYCANLANDIM!."
                        binding.btnspin.isEnabled = true
                    }
                }.start()
            } else {
                binding.txtAltin.text = "HEYCANLANDIM!."
                binding.btnspin.isEnabled = true
            }
        }
    }

    private fun banUserForOneDay(uid: String) {
        val banEndTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24)  // 24 saatlik ban süresi
        val userRef = database.getReference("usersbanGunlukCark/$uid")
        userRef.child("banEndTime").setValue(banEndTime).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateBanStatus1(uid)
            } else {
                binding.txtAltin.text = ""
            }
        }
    }

    private fun performWheelSpin(uid: String) {
        val wheel = binding.imageView11
        // Spin at least 4 full rotations plus a random extra slice angle
        val randomAngle = Random.nextInt(1440, 2880).toFloat()
        
        val animator = ObjectAnimator.ofFloat(wheel, View.ROTATION, 0f, randomAngle)
        animator.duration = 3000 // 3 seconds of premium smooth deceleration
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        
        binding.btnspin.isEnabled = false
        binding.txtAltin.text = "Çark Dönüyor..."
        
        animator.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // Calculate pointer selection (pointer is at the very top: 0 degrees)
                val degrees = randomAngle % 360
                val pointerDegrees = (360 - degrees) % 360
                val sliceIndex = (pointerDegrees / 45).toInt()
                
                val result = when (sliceIndex) {
                    0 -> "10 Altın"
                    1 -> "Boş"
                    2 -> "25 Altın"
                    3 -> "5 Altın"
                    4 -> "Boş"
                    5 -> "50 Altın"
                    6 -> "15 Altın"
                    7 -> "100 Altın"
                    else -> "Boş"
                }
                
                binding.txtAltin.text = result
                
                if (result == "Boş") {
                    Toast.makeText(this@MainActivity, "Şansına Boş Çıktı! Yarın tekrar dene.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Tebrikler! $result Kazandınız", Toast.LENGTH_LONG).show()
                }
                
                // Ban/cooldown after successful spin
                banUserForOneDay(uid)
            }
            
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
        animator.start()
    }


    private fun showNumbers() {
        binding.timerIndicatorTextview.text = count.toString()
        // Eğer count 1'den büyükse bir sonraki sayıyı göstermek için postDelayed'i çağır
        if (count > 1) {
            handler1.postDelayed({ showNumbers() }, 1000) // 1000 milisaniye (1 saniye) gecikme
        }else{
            count = 9
        }
        count--
    }

    private fun gameLogic() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                sorularCount = snapshot.childrenCount.toInt()

                if (hangisoru <= sorularCount) {

                    soru = snapshot.child(hangisoru.toString()).child("soru").value.toString()
                    cevapA = snapshot.child(hangisoru.toString()).child("a").value.toString()
                    cevapB = snapshot.child(hangisoru.toString()).child("b").value.toString()
                    cevapC = snapshot.child(hangisoru.toString()).child("c").value.toString()
                    cevapD = snapshot.child(hangisoru.toString()).child("d").value.toString()
                    cevap = snapshot.child(hangisoru.toString()).child("cevap").value.toString()

                    binding.txtGunlukSoru.text = soru
                    binding.btnGunlukCevap1.text = cevapA
                    binding.btnGunlukCevap2.text = cevapB
                    binding.btnGunlukCevap3.text = cevapC
                    binding.btnGunlukCevap4.text = cevapD
                }

                val optionButtons = listOf(
                    binding.btnGunlukCevap1,
                    binding.btnGunlukCevap2,
                    binding.btnGunlukCevap3,
                    binding.btnGunlukCevap4
                )

                optionButtons.forEachIndexed { index, button ->
                    button.setOnClickListener {
                        val selectedText = button.text.toString()
                        val isCorrect = selectedText.equals(cevap, ignoreCase = true) ||
                                cevap.equals("a", ignoreCase = true) && index == 0 ||
                                cevap.equals("b", ignoreCase = true) && index == 1 ||
                                cevap.equals("c", ignoreCase = true) && index == 2 ||
                                cevap.equals("d", ignoreCase = true) && index == 3 ||
                                cevap.equals("1", ignoreCase = true) && index == 0 ||
                                cevap.equals("2", ignoreCase = true) && index == 1 ||
                                cevap.equals("3", ignoreCase = true) && index == 2 ||
                                cevap.equals("4", ignoreCase = true) && index == 3

                        if (isCorrect) {
                            Toast.makeText(this@MainActivity, "Doğru! +10 Puan Kazandınız", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Yanlış Cevap!", Toast.LENGTH_LONG).show()
                        }

                        // Close daily quiz modal
                        binding.layoutYan.visibility = View.VISIBLE
                        binding.btnYolculukdag.visibility = View.VISIBLE
                        binding.layoutGunluksoru.visibility = View.INVISIBLE
                        binding.btnGunluksoru.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        // Aktiviteyi yok ettiğinizde, işlemi durdurun
        handler.removeCallbacks(runnable!!)
    }

override fun onBackPressed() {
            // Kullanıcı geri tuşuna bastığında diğer Activity'e geçiş yap
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

class ImageAdapter(private val images: List<Int>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(images[position])
    }

    override fun getItemCount(): Int {
        return images.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bind(imageResId: Int) {
            imageView.setImageResource(imageResId)
        }
    }
}