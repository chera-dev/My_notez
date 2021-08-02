package com.example.mynotez

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mynotez.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mynotez.SharedViewModel.Companion.LABEL
import com.example.mynotez.SharedViewModel.Companion.NOTEZ

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController :NavController
    private lateinit var drawerLayout: DrawerLayout

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

        appBarConfiguration = AppBarConfiguration(binding.navView.menu, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        /*navController.addOnDestinationChangedListener{ nc: NavController, nd: NavDestination, args:Bundle? ->
            if(nd.id == nc.graph.startDestination)
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            else
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }*/

        addItemtonavDrawer(navView)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_notes_frag -> {
                    Toast.makeText(this, "yaaahh ${it.title}", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                    drawerLayout.close()
                }
                R.id.nav_archive -> {
                    Toast.makeText(this, "naahh ${it.title}", Toast.LENGTH_SHORT).show()
                    val bundle = bundleOf("title" to it.title,"type" to NOTEZ)
                    if (navController.previousBackStackEntry != null)
                        navController.popBackStack()
                    navController.navigate(R.id.nav_notes_frag,bundle)
                    drawerLayout.close()
                }
            }
            true
        }
    }

    private fun addItemtonavDrawer(navView: NavigationView){
        val menu: Menu = navView.menu
        val labelList = sharedSharedViewModel.getLabels()
        val subMenu: SubMenu = menu.addSubMenu("label")
        var order = 1
        for (i in labelList) {
            subMenu.add(1,order,order++,i.labelName).setIcon(R.drawable.ic_outline_label_24).setOnMenuItemClickListener {
                toNotesFragment(it, LABEL,i.labelId)
                true
            }
        }
        subMenu.add("add label").setIcon(R.drawable.ic_baseline_add_24).setOnMenuItemClickListener {
                Toast.makeText(this, "${it.title} clicked", Toast.LENGTH_SHORT).show()
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Add new label")
                val dialogLayout = layoutInflater.inflate(R.layout.add_label,null)
                val titleEditText = dialogLayout.findViewById<EditText>(R.id.label_title_edit_text)
                builder.setView(dialogLayout)
                builder.setPositiveButton("Add Label"){ _, _ ->
                    Toast.makeText(this,"test ${titleEditText.text} added",Toast.LENGTH_SHORT).show()
                    val label:Label = sharedSharedViewModel.addLabel(titleEditText.text.toString())
                    subMenu.add(1,order,order++,label.labelName).setIcon(R.drawable.ic_outline_label_24).setOnMenuItemClickListener {
                        toNotesFragment(it, LABEL,label.labelId)
                        true
                    }
                    //labelList = sharedSharedViewModel.getLabel()
                    //recyclerAdapter.changeData(labelList)
                }
                builder.show()
            true
        }
    }

    fun toNotesFragment(it:MenuItem,type:Int,labelId:Int){
        Toast.makeText(this, "naahh ${it.title}", Toast.LENGTH_SHORT).show()
        val bundle = bundleOf("title" to it.title,"type" to type,"labelId" to labelId)
        if (navController.previousBackStackEntry != null)
            navController.popBackStack()
        navController.navigate(R.id.nav_notes_frag,bundle)
        drawerLayout.close()
    }

}