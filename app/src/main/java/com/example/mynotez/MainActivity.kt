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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotez.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mynotez.SharedViewModel.Companion.LABEL
import com.example.mynotez.SharedViewModel.Companion.NOTEZ

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController :NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menu: Menu
    private var labelOrder = 1

    private lateinit var sharedSharedViewModel: SharedViewModel

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

        /*navController.addOnDestinationChangedListener{ nc: NavController, nd: NavDestination, args:Bundle? ->
            if(nd.id == nc.graph.startDestination)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            else
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }*/

        addItemToNavDrawer(navView)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_notes_frag -> {
                    Toast.makeText(this, "yaaahh ${it.title}", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                    drawerLayout.close()
                }
            }
            true
        }

        //val drawerToggle = ActionBarDrawerToggle(this,drawerLayout,"open","close")

        // Close the soft keyboard when you open or close the Drawer
        val toggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.appBarMain.toolBar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ) {
            override fun onDrawerClosed(drawerView: View) {
                // Triggered once the drawer closes
                super.onDrawerClosed(drawerView)
            }

            override fun onDrawerOpened(drawerView: View) {
                // Triggered once the drawer opens
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
            Toast.makeText(this, "naahh ${it.title}", Toast.LENGTH_SHORT).show()
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
                toNotesFragment(it,i.labelId,labelOrder)
                true
            }
            labelOrder++
        }
        subMenu.add("add label").setIcon(R.drawable.ic_baseline_add_24).setOnMenuItemClickListener { menuItem ->
            Toast.makeText(this, "${menuItem.title} clicked", Toast.LENGTH_SHORT).show()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Add new label")
            val dialogLayout = layoutInflater.inflate(R.layout.add_label,null)
            val titleEditText = dialogLayout.findViewById<EditText>(R.id.label_title_edit_text)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Add Label"){ _, _ ->
                Toast.makeText(this,"test ${titleEditText.text} added",Toast.LENGTH_SHORT).show()
                val label = sharedSharedViewModel.addLabel(labelOrder,titleEditText.text.toString())
                subMenu.add(1,labelOrder,labelOrder,label.labelName).setIcon(R.drawable.ic_outline_label_24).setOnMenuItemClickListener {
                    toNotesFragment(it,label.labelId,labelOrder)
                    true
                }
                labelOrder++
            }
            builder.show()
            true
        }
    }

    private fun toNotesFragment(it:MenuItem, labelId:Int, orderId: Int){
        Toast.makeText(this, "naahh ${it.title}", Toast.LENGTH_SHORT).show()
        val bundle = bundleOf("title" to it.title,"type" to  LABEL,"labelId" to labelId,"orderId" to orderId)
        if (navController.previousBackStackEntry != null)
            navController.popBackStack()
        navController.navigate(R.id.nav_notes_frag,bundle)
        drawerLayout.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}