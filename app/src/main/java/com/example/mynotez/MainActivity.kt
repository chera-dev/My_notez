package com.example.mynotez

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.core.view.get
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
import com.example.mynotez.data.entities.Label
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
    private lateinit var navView:NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mUserViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)

        setSupportActionBar(binding.appBarMain.toolBar)
        drawerLayout = binding.drawerLayout
        navView = binding.navView
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
            /*when(it.itemId){
                R.id.nav_notes_frag -> {
                    //it.isCheckable = true
                    //not
                    //Toast.makeText(this,"ZzZZz",Toast.LENGTH_SHORT).show()
                    menu.setGroupCheckable(R.id.group1,true,true)
                    menu.findItem(R.id.nav_notes_frag).isChecked = true
                    navController.popBackStack()
                    drawerLayout.close()
                }
            }*/
            true
        }
        //onOptionsItemSelected(menu.getItem(0))
        if (navController.previousBackStackEntry == null){
            //navView.menu.getItem(0).isChecked = true
            val checked = menu.findItem(R.id.nav_notes_frag).setChecked(true)
            menu.setGroupCheckable(R.id.group1,true,true)
            menu.setGroupCheckable(R.id.labels,true,true)
            //onMenuItemSelected(R.id.nav_notes_frag,menu.getItem(0))
            //menu.getItem(0).isChecked = true
            //Toast.makeText(this,"${navView.checkedItem?.title}    ${navView.menu.getItem(0).title}  \n${checked.isChecked}    ${menu.getItem(0).title}",Toast.LENGTH_LONG).show()
        }
        supportActionBar?.title = navView.checkedItem?.title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun addItemToNavDrawer(navView: NavigationView){
        menu = navView.menu
        aaa(emptyList())
        mUserViewModel.allLabels.observe(this, { allLabels ->
            aaa(allLabels)
        })
    }

    private fun aaa(allLabels:List<Label>){
        menu.clear()
        var order = 0
        menu.setGroupCheckable(R.id.group1,true,true)
        menu.setGroupCheckable(R.id.labels,true,true)
        menu.add(R.id.group1,R.id.nav_notes_frag,order++,"Notez").setIcon(R.drawable.ic_outline_note_24).setOnMenuItemClickListener {
            Toast.makeText(this,"XXxxxxX",Toast.LENGTH_SHORT).show()
            it.isCheckable = true
            menu.setGroupCheckable(R.id.group1,true,true)
            navController.popBackStack()
            supportActionBar?.title = it.title
            drawerLayout.close()
            true
        }
        menu.add(R.id.labels,order,order++,"Add Label").setIcon(R.drawable.ic_baseline_add_24).setOnMenuItemClickListener {
            val builder = AlertDialog.Builder(this,R.style.CustomAlertDialog)
            builder.setTitle("Add new label")
            val dialogLayout = layoutInflater.inflate(R.layout.add_label,null)
            val titleEditText = dialogLayout.findViewById<TextInputEditText>(R.id.label_title_edit_text)
            titleEditText.requestFocus()
            val imm:InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_NOT_ALWAYS)
            builder.setView(dialogLayout)
            builder.setPositiveButton("Add Label"){ _, _ ->
                val labelName = titleEditText.text.toString()
                if (labelName!= ""){
                    if (allLabels.isNotEmpty()) {
                        for (i in allLabels) {
                            if (labelName == i.labelName)
                                Toast.makeText(this, "Label Already exists", Toast.LENGTH_SHORT).show()
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
        for (i in allLabels) {
            menu.add(R.id.labels, order, order++, i.labelName)
                .setIcon(R.drawable.ic_outline_label_24).setOnMenuItemClickListener {
                    it.isCheckable = true
                    menu.setGroupCheckable(R.id.labels,true,true)
                    toNotesFragment(it, i)
                    true
                }
        }
        menu.add(R.id.group1,order,order,"Archive").setIcon(R.drawable.ic_outline_archive_24).setOnMenuItemClickListener {
            val bundle = bundleOf("title" to it.title,"type" to ARCHIVED.name)
            if (navController.previousBackStackEntry != null)
                navController.popBackStack()
            supportActionBar?.title = it.title
            it.isCheckable = true
            menu.setGroupCheckable(R.id.group1,true,true)
            navController.navigate(R.id.nav_notes_frag,bundle,getNavBuilderAnimation().build())
            drawerLayout.close()
            true
        }}

    private fun toNotesFragment(it:MenuItem, label: Label){
        val bundle = bundleOf("title" to it.title,"type" to  LABEL.name,"label" to label)
        if (navController.previousBackStackEntry != null)
            navController.popBackStack()
        supportActionBar?.title = it.title
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
        if (navController.previousBackStackEntry == null)
            supportActionBar?.title = menu[0].title
        else
            supportActionBar?.title = navView.checkedItem?.title
    }
}
