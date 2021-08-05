package com.example.mynotez

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotez.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mynotez.viewmodel.SharedViewModel
import com.example.mynotez.viewmodel.SharedViewModel.Companion.LABEL
import com.example.mynotez.viewmodel.SharedViewModel.Companion.NOTEZ
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedSharedViewModel: SharedViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController :NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menu: Menu
    private var labelOrder = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedSharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

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

        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.appBarMain.toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
                menu.getItem(1).subMenu.clear()
                addLabelToDrawer(menu.getItem(1).subMenu)
            }
        }
        drawerLayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun addItemToNavDrawer(navView: NavigationView){
        menu = navView.menu
        val subMenu: SubMenu = menu.addSubMenu("Label")
        addLabelToDrawer(subMenu)
        menu.add(2,1,0,"Archive").setIcon(R.drawable.ic_outline_archive_24).setOnMenuItemClickListener {
            it.isEnabled = true
            val bundle = bundleOf("title" to it.title,"type" to NOTEZ)
            if (navController.previousBackStackEntry != null)
                navController.popBackStack()
            navController.navigate(R.id.nav_notes_frag,bundle)
            drawerLayout.close()
            false
        }
    }

    fun addLabelToDrawer(subMenu: SubMenu){
        val labelList = sharedSharedViewModel.getLabels()
        for (i in labelList) {
            subMenu.add(1,labelOrder,labelOrder,i.labelName).setIcon(R.drawable.ic_outline_label_24).setOnMenuItemClickListener {
                toNotesFragment(it,i.labelId)
                it.isChecked = true
                true
            }
            labelOrder++
        }
        subMenu.add("add label").setIcon(R.drawable.ic_baseline_add_24).setOnMenuItemClickListener { menuItem ->
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add new label")
            val dialogLayout = layoutInflater.inflate(R.layout.add_label,null)
            val titleEditText = dialogLayout.findViewById<TextInputEditText>(R.id.label_title_edit_text)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Add Label"){ _, _ ->
                val label = sharedSharedViewModel.addLabel(labelOrder,titleEditText.text.toString())
                subMenu.add(1,labelOrder,labelOrder,label.labelName).setIcon(R.drawable.ic_outline_label_24).setOnMenuItemClickListener {
                    toNotesFragment(it,label.labelId)
                    true
                }
                labelOrder++
            }
            builder.show()
            true
        }
    }

    private fun toNotesFragment(it:MenuItem, labelId:Int){
        val bundle = bundleOf("title" to it.title,"type" to  LABEL,"labelId" to labelId)
        if (navController.previousBackStackEntry != null)
            navController.popBackStack()
        navController.navigate(R.id.nav_notes_frag,bundle)
        drawerLayout.close()
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