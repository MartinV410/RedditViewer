package com.example.redditviewer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.redditviewer.R
import com.example.redditviewer.databinding.UserBottomSheetBinding
import com.example.redditviewer.view.UserInfoViewModel
import com.example.redditviewer.view.UserUIState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class UserInfoBottomSheetFragment: BottomSheetDialogFragment() {

    private val viewModel: UserInfoViewModel by viewModels { UserInfoViewModel.Factory}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        val binding = UserBottomSheetBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        // set style so background can be transparent

        viewModel.userUIState.observe(viewLifecycleOwner, Observer {
            it?.let {
                when(it) {
                    is UserUIState.Error -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                    UserUIState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is UserUIState.Success -> {
                        binding.user = it.user
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        })

        // navigate to log on screen
        binding.userLogoff.setOnClickListener {
            val direction = UserInfoBottomSheetFragmentDirections.actionUserInfoBottomSheetFragmentToFragmentLogin2(needsRevoke = true)
            findNavController().navigate(direction)
        }

        return binding.root
    }

    /**
     * Custom theme for transparent bg
     */
    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

}