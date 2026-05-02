package org.ostrya.presencepublisher.dialog

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import org.ostrya.presencepublisher.R

class LicenseDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val parent: Activity = requireActivity()
        val view = ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val libraries by produceLibraries(R.raw.aboutlibraries)
                LibrariesContainer(libraries, Modifier.fillMaxSize())
            }
        }
        val builder = AlertDialog.Builder(parent)
        builder.setTitle(R.string.bundled_licenses_title)
        builder.setView(view)
        builder.setNeutralButton(R.string.dialog_close, null)
        return builder.create()
    }
}
