package com.example.mynotez

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotez.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mynotez.data.Label
import com.example.mynotez.data.NoteViewModel
import com.example.mynotez.enumclass.From.LABEL
import com.example.mynotez.enumclass.From.ARCHIVED
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var mUserViewModel:NoteViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController :NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mUserViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        setSupportActionBar(binding.appBarMain.toolBar)
        drawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(setOf(R.id.nav_notes_frag), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener{ nc: NavController, nd: NavDestination, _:Bundle? ->
            if(nd.id == nc.graph.startDestination)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            else
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }

        addItemToNavDrawer(navView)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_notes_frag -> {
                    navController.popBackStack()
                    drawerLayout.close()
                }
            }
            true
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun addItemToNavDrawer(navView: NavigationView){
        menu = navView.menu
        val subMenu: SubMenu = menu.addSubMenu("Label")
        addLabelToDrawer(subMenu)
        menu.add(2,1,0,"Archive").setIcon(R.drawable.ic_outline_archive_24).setOnMenuItemClickListener {
            //it.isEnabled = true
            val bundle = bundleOf("title" to it.title,"type" to ARCHIVED.name)
            if (navController.previousBackStackEntry != null)
                navController.popBackStack()
            navController.navigate(R.id.nav_notes_frag,bundle,getNavBuilderAnimation().build())
            drawerLayout.close()
            true
        }
    }

    private fun addLabelToDrawer(subMenu: SubMenu){
        mUserViewModel.allLabels.observe(this, { allLabels ->
            subMenu.clear()
            var labelOrder = 1
            for (i in allLabels) {
                subMenu.add(1, labelOrder, labelOrder, i.labelName)
                    .setIcon(R.drawable.ic_outline_label_24).setOnMenuItemClickListener {
                    toNotesFragment(it, i)
                    it.isChecked = true
                    true
                }
                labelOrder++
            }

            subMenu.add("add label").setIcon(R.drawable.ic_baseline_add_24).setOnMenuItemClickListener {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Add new label")
                val dialogLayout = layoutInflater.inflate(R.layout.add_label,null)
                val titleEditText = dialogLayout.findViewById<TextInputEditText>(R.id.label_title_edit_text)
                titleEditText.requestFocus()
                val imm:InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.SHOW_IMPLICIT)
                builder.setView(dialogLayout)
                builder.setPositiveButton("Add Label"){ _, _ ->
                    val labelName = titleEditText.text.toString()
                    if (labelName!= ""){
                        if (allLabels.isNotEmpty()) {
                            for (i in allLabels) {
                                if (labelName == i.labelName)
                                    Toast.makeText(this, "Label Already exists", Toast.LENGTH_SHORT)
                                        .show()
                                else
                                    mUserViewModel.addLabel(labelName)
                            }
                        } else
                            mUserViewModel.addLabel(labelName)
                    }
                    imm.hideSoftInputFromWindow(titleEditText.windowToken,0)
                }
                builder.setNegativeButton("Cancel"){ _, _ ->
                    imm.hideSoftInputFromWindow(titleEditText.windowToken,0)
                }
                val alertDialog:AlertDialog = builder.show()
                titleEditText.setOnEditorActionListener(object : TextView.OnEditorActionListener{
                    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                        if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE ) {
                            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
                            return true
                        }
                        return false
                    }
                })
                true
            }
        })
    }

    private fun toNotesFragment(it:MenuItem, label: Label){
        val bundle = bundleOf("title" to it.title,"type" to  LABEL.name,"label" to label)
        if (navController.previousBackStackEntry != null)
            navController.popBackStack()
        navController.navigate(R.id.nav_notes_frag,bundle,getNavBuilderAnimation().build())
        drawerLayout.close()
    }

    private fun getNavBuilderAnimation():NavOptions.Builder{
        val navBuilder = NavOptions.Builder()
        navBuilder.setEnterAnim(R.anim.slide_in_from_left)
        navBuilder.setExitAnim(R.anim.slide_out_to_right)
        navBuilder.setPopEnterAnim(R.anim.slide_in_from_right)
        navBuilder.setPopExitAnim(R.anim.slide_out_to_left)
        return navBuilder
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }
}

//
//E/tag: 0  KeyEvent { action=ACTION_DOWN, keyCode=KEYCODE_ENTER, scanCode=28, metaState=0, flags=0x8, repeatCount=0, eventTime=50988924, downTime=50988924, deviceId=0, source=0x301, displayId=-1 }
//
//E/tag: 6  null
