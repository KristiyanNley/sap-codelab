package com.sap.codelab.view.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import com.sap.codelab.R
import com.sap.codelab.databinding.ActivityHomeBinding
import com.sap.codelab.model.Memo
import com.sap.codelab.utils.permission.PermissionUtils
import com.sap.codelab.view.create.CreateMemo
import com.sap.codelab.view.detail.BUNDLE_MEMO_ID
import com.sap.codelab.view.detail.ViewMemo
import kotlinx.coroutines.launch

internal class Home : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var model: HomeViewModel
    private lateinit var menuItemShowAll: MenuItem
    private lateinit var menuItemShowOpen: MenuItem

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        PermissionUtils.markRequested(this, Manifest.permission.POST_NOTIFICATIONS)
        updateNotificationBanner()
    }

    private val createMemoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            model.refreshMemos()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        model = ViewModelProvider(this)[HomeViewModel::class.java]

        binding.contentHome.notificationSettingsButton.setOnClickListener {
            PermissionUtils.openAppSettings(this)
        }

        requestNotificationPermissionIfNeeded()
        setupRecyclerView(initializeAdapter())

        binding.fab.setOnClickListener {
            createMemoLauncher.launch(Intent(this@Home, CreateMemo::class.java))
        }
        model.loadOpenMemos()
    }

    override fun onResume() {
        super.onResume()
        updateNotificationBanner()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (PermissionUtils.isGranted(this, Manifest.permission.POST_NOTIFICATIONS)) return

        if (PermissionUtils.hasBeenRequested(this, Manifest.permission.POST_NOTIFICATIONS)) {
            // Already asked before — show banner instead of re-prompting
            updateNotificationBanner()
        } else {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun updateNotificationBanner() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val shouldShow = !PermissionUtils.isGranted(this, Manifest.permission.POST_NOTIFICATIONS) &&
            PermissionUtils.hasBeenRequested(this, Manifest.permission.POST_NOTIFICATIONS)
        binding.contentHome.notificationPermissionBanner.visibility =
            if (shouldShow) View.VISIBLE else View.GONE
    }

    private fun initializeAdapter(): MemoAdapter {
        val adapter = MemoAdapter(mutableListOf(), { view ->
            showMemo((view.tag as Memo).id)
        }, { checkbox, isChecked ->
            model.updateMemo(checkbox.tag as Memo, isChecked)
            model.refreshMemos()
        })
        lifecycle.coroutineScope.launch {
            model.memos.collect { memos ->
                adapter.setItems(memos)
            }
        }
        return adapter
    }

    private fun showMemo(memoId: Long) {
        val intent = Intent(this@Home, ViewMemo::class.java)
        intent.putExtra(BUNDLE_MEMO_ID, memoId)
        startActivity(intent)
    }

    private fun setupRecyclerView(adapter: MemoAdapter) {
        binding.contentHome.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@Home, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
            addItemDecoration(DividerItemDecoration(this@Home, (layoutManager as LinearLayoutManager).orientation))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menuItemShowAll = menu.findItem(R.id.action_show_all)
        menuItemShowOpen = menu.findItem(R.id.action_show_open)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_show_all -> {
                model.loadAllMemos()
                menuItemShowAll.isVisible = false
                menuItemShowOpen.isVisible = true
                true
            }
            R.id.action_show_open -> {
                model.loadOpenMemos()
                menuItemShowOpen.isVisible = false
                menuItemShowAll.isVisible = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}