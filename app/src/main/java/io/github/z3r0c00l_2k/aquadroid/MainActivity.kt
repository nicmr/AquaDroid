package io.github.z3r0c00l_2k.aquadroid

import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import io.github.z3r0c00l_2k.aquadroid.fragments.BottomSheetFragment
import io.github.z3r0c00l_2k.aquadroid.helpers.AlarmHelper
import io.github.z3r0c00l_2k.aquadroid.helpers.SqliteHelper
import io.github.z3r0c00l_2k.aquadroid.utils.AppUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main_test.amount0Text
import kotlinx.android.synthetic.main.activity_main_test.amount1Text
import kotlinx.android.synthetic.main.activity_main_test.amount2Text
import kotlinx.android.synthetic.main.activity_main_test.amount3Text
import kotlinx.android.synthetic.main.activity_main_test.amount4Text
import kotlinx.android.synthetic.main.activity_main_test.btnEdit
import kotlinx.android.synthetic.main.activity_main_test.op300ml


class MainActivity : AppCompatActivity() {

    private var totalIntake: Int = 0
    private var inTook: Int = 0
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sqliteHelper: SqliteHelper
    private lateinit var dateNow: String
    private var notificStatus: Boolean = false
    private var selectedOption: Int? = null
    private var snackbar: Snackbar? = null
    private var doubleBackToExitPressedOnce = false
    private var editMode: Boolean = false

    // TODO: consider placing these in separate class to reduce array indexing required
    private var configuredAmounts = ArrayList<Int>()
    private var amountTextViews = ArrayList<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_test)

        sharedPref = getSharedPreferences(AppUtils.USERS_SHARED_PREF, AppUtils.PRIVATE_MODE)
        sqliteHelper = SqliteHelper(this)

        totalIntake = sharedPref.getInt(AppUtils.TOTAL_INTAKE, 0)

        setAmountTextViews()
        setConfiguredAmountsDefault()

        if (sharedPref.getBoolean(AppUtils.FIRST_RUN_KEY, true)) {
            startActivity(Intent(this, WalkThroughActivity::class.java))
            finish()
        } else if (totalIntake <= 0) {
            startActivity(Intent(this, InitUserInfoActivity::class.java))
            finish()
        }

        dateNow = AppUtils.getCurrentDate()!!
    }

    private fun setConfiguredAmountsDefault() {
        this.configuredAmounts.addAll(listOf(50, 100, 150, 200, 250, 300))
    }
    private fun setAmountTextViews() {
        this.amountTextViews.addAll(
            listOf(
                amount0Text,
                amount1Text,
                amount2Text,
                amount3Text,
                amount4Text,
                amount5Text
            ))
    }

    fun updateValues() {
        totalIntake = sharedPref.getInt(AppUtils.TOTAL_INTAKE, 0)
        inTook = sqliteHelper.getIntook(dateNow)
        setWaterLevel(inTook, totalIntake)
    }

    fun addIntake(view: View, milliliters: Int) {
        // TODO: find out what this check does
        if (sqliteHelper.addIntook(dateNow, milliliters) > 0) {
            inTook += milliliters
            setWaterLevel(inTook, totalIntake)
            Snackbar.make(view, "Your water intake was saved", Snackbar.LENGTH_SHORT).show()
        }
        if (inTook >= totalIntake) {
            val mNotificationManager : NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.cancelAll()
        }
    }

    fun intakeAmountEditDialogue(view: View, fieldIndex: Int) {
//        opCustom.setOnClickListener {
//
//            op50ml.background = getDrawable(outValue.resourceId)
//            op100ml.background = getDrawable(outValue.resourceId)
//            op150ml.background = getDrawable(outValue.resourceId)
//            op200ml.background = getDrawable(outValue.resourceId)
//            op250ml.background = getDrawable(outValue.resourceId)
//            opCustom.background = getDrawable(R.drawable.option_select_bg)
//
//        }
        if (snackbar != null) {
            snackbar?.dismiss()
        }
        var li = LayoutInflater.from(this)
        val dialogView = li.inflate(R.layout.custom_input_dialog, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(dialogView)

        val userInput = dialogView.findViewById(R.id.etCustomInput) as TextInputLayout

        alertDialogBuilder.setPositiveButton("OK") { dialog, id ->
            val inputText = userInput.editText!!.text.toString() // check if there is an alternative to !! here
            if (!TextUtils.isEmpty(inputText)) {
//                tvCustom.text = "${inputText} ml"
                this.amountTextViews[fieldIndex].text = "${inputText} ml"
                configuredAmounts[fieldIndex] = inputText.toInt()
            }
        }.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        var alertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }

    override fun onStart() {
        super.onStart()

        val outValue = TypedValue()
        applicationContext.theme.resolveAttribute(
            android.R.attr.selectableItemBackground,
            outValue,
            true
        )

        notificStatus = sharedPref.getBoolean(AppUtils.NOTIFICATION_STATUS_KEY, true)
        val alarm = AlarmHelper()
        if (!alarm.checkAlarm(this) && notificStatus) {
            btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell))
            alarm.setAlarm(
                this,
                sharedPref.getInt(AppUtils.NOTIFICATION_FREQUENCY_KEY, 30).toLong()
            )
        }

        if (notificStatus) {
            btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell))
        } else {
            btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell_disabled))
        }

        sqliteHelper.addAll(dateNow, 0, totalIntake)

        updateValues()

        btnMenu.setOnClickListener {
            val bottomSheetFragment = BottomSheetFragment(this)
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
        }

//        fabAdd.setOnClickListener {
//            if (selectedOption != null) {
//                if ((inTook * 100 / totalIntake) <= 140) {
//                    if (sqliteHelper.addIntook(dateNow, selectedOption!!) > 0) {
//                        inTook += selectedOption!!
//                        setWaterLevel(inTook, totalIntake)
//
//                        Snackbar.make(it, "Your water intake was saved...!!", Snackbar.LENGTH_SHORT)
//                            .show()
//
//                    }
//                } else {
//                    Snackbar.make(it, "You already achieved the goal", Snackbar.LENGTH_SHORT).show()
//                }
//                // Change: remove selection reset on Add
////                selectedOption = null
////                tvCustom.text = "Custom"
////                op50ml.background = getDrawable(outValue.resourceId)
////                op100ml.background = getDrawable(outValue.resourceId)
////                op150ml.background = getDrawable(outValue.resourceId)
////                op200ml.background = getDrawable(outValue.resourceId)
////                op250ml.background = getDrawable(outValue.resourceId)
////                opCustom.background = getDrawable(outValue.resourceId)
//
//                // remove pending notifications
//                val mNotificationManager : NotificationManager =
//                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//                mNotificationManager.cancelAll()
//            } else {
//                YoYo.with(Techniques.Shake)
//                    .duration(700)
//                    .playOn(cardView)
//                Snackbar.make(it, "Please select an option", Snackbar.LENGTH_SHORT).show()
//            }
//        }

        btnEdit.setOnClickListener {
            editMode = !editMode
            if (editMode) {
                btnEdit.backgroundTintList = ColorStateList.valueOf(Color.BLUE)
            } else {
                btnEdit.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#41B279"))
            }
        }

        btnNotific.setOnClickListener {
            notificStatus = !notificStatus
            sharedPref.edit().putBoolean(AppUtils.NOTIFICATION_STATUS_KEY, notificStatus).apply()
            if (notificStatus) {
                btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell))
                Snackbar.make(it, "Notification Enabled..", Snackbar.LENGTH_SHORT).show()
                alarm.setAlarm(
                    this,
                    sharedPref.getInt(AppUtils.NOTIFICATION_FREQUENCY_KEY, 30).toLong()
                )
            } else {
                btnNotific.setImageDrawable(getDrawable(R.drawable.ic_bell_disabled))
                Snackbar.make(it, "Notification Disabled..", Snackbar.LENGTH_SHORT).show()
                alarm.cancelAlarm(this)
            }
        }

        btnStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }


        op50ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            if (editMode) {
                intakeAmountEditDialogue(it, 0)

            } else {
                addIntake(it, configuredAmounts[0])
            }
//            selectedOption = 50
//            op50ml.background = getDrawable(R.drawable.option_select_bg)
//            op100ml.background = getDrawable(outValue.resourceId)
//            op150ml.background = getDrawable(outValue.resourceId)
//            op200ml.background = getDrawable(outValue.resourceId)
//            op250ml.background = getDrawable(outValue.resourceId)
//            opCustom.background = getDrawable(outValue.resourceId)

        }

        op100ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            if (editMode) {
                intakeAmountEditDialogue(it, 1)
            } else {
                addIntake(it, configuredAmounts[1])
            }
        }

        op150ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            if (editMode) {
                intakeAmountEditDialogue(it, 2)
            } else {
                addIntake(it, configuredAmounts[2])
            }
        }

        op200ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            if (editMode) {
                intakeAmountEditDialogue(it, 3)
            } else {
                addIntake(it, configuredAmounts[3])
            }
        }

        op250ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            if (editMode) {
                intakeAmountEditDialogue(it, 4)
            } else {
                addIntake(it, configuredAmounts[4])
            }
        }

        op300ml.setOnClickListener {
            if (snackbar != null) {
                snackbar?.dismiss()
            }
            if (editMode) {
                intakeAmountEditDialogue(it, 5)
            } else {
                addIntake(it, configuredAmounts[5])
            }
        }
    }


    private fun setWaterLevel(inTook: Int, totalIntake: Int) {

        YoYo.with(Techniques.SlideInDown)
            .duration(500)
            .playOn(tvIntook)
        tvIntook.text = "$inTook"
        tvTotalIntake.text = "/$totalIntake ml"
        val progress = ((inTook / totalIntake.toFloat()) * 100).toInt()
        YoYo.with(Techniques.Pulse)
            .duration(500)
            .playOn(intakeProgress)
        intakeProgress.currentProgress = progress
        if ((inTook * 100 / totalIntake) > 140) {
            Snackbar.make(main_activity_parent, "You achieved the goal", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Snackbar.make(
            this.window.decorView.findViewById(android.R.id.content),
            "Please click BACK again to exit",
            Snackbar.LENGTH_SHORT
        ).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 1000)
    }

}
